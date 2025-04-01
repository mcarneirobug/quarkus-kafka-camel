package com.poc.route;

import com.poc.model.entity.AggregatedEvent;
import com.poc.model.entity.BatchProcessing;

import com.poc.repository.BatchProcessingRepository;

import com.poc.usecase.AggregationUseCase;
import com.poc.usecase.KafkaPublisherUseCase;
import com.poc.usecase.MonitoringUseCase;

import jakarta.enterprise.context.ApplicationScoped;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import org.jboss.logging.Logger;

import java.util.List;
import java.util.concurrent.CompletionStage;

import static org.apache.camel.LoggingLevel.INFO;
import static org.apache.camel.LoggingLevel.WARN;

@ApplicationScoped
public class BatchAggregationRoute extends RouteBuilder {

    protected static final Logger LOG = Logger.getLogger(BatchAggregationRoute.class);

    private static final String BATCH_AGGREGATION_ROUTE = "batchAggregationRoute";
    private static final String SEND_EVENTS_ROUTE = "sendEventsRoute";
    private static final String BATCH_POLLING_ROUTE = "batchPollingRoute";
    private static final String BATCH_PROCESSING_ROUTE_LOCATION = "BatchProcessingRoute";
    private static final String BATCH_ID_HEADER = "batchId";
    private static final String AGGREGATION_STATUS_HEADER = "aggregationStatus";
    private static final String EVENT_COUNT_HEADER = "eventCount";
    private static final String COMPLETED_STATUS = "COMPLETED";
    private static final String ERROR_STATUS = "ERROR";
    private static final String DIRECT_TRIGGER_AGGREGATION = "direct:triggerAggregation";

    private final AggregationUseCase aggregationUseCase;
    private final KafkaPublisherUseCase kafkaEmitterUseCase;
    private final BatchProcessingRepository batchProcessingRepository;
    private final MonitoringUseCase monitoringUseCase;
    private final CamelContext camelContext;

    @ConfigProperty(name = "app.processing.polling-interval-ms", defaultValue = "60000")
    long pollingIntervalMs;

    @Inject
    public BatchAggregationRoute(
            AggregationUseCase aggregationUseCase,
            KafkaPublisherUseCase kafkaEmitterUseCase,
            BatchProcessingRepository batchProcessingRepository,
            MonitoringUseCase monitoringUseCase,
            CamelContext camelContext) {
        this.aggregationUseCase = aggregationUseCase;
        this.kafkaEmitterUseCase = kafkaEmitterUseCase;
        this.batchProcessingRepository = batchProcessingRepository;
        this.monitoringUseCase = monitoringUseCase;
        this.camelContext = camelContext;
    }

    @Override
    public void configure() {
        // Route to manually trigger aggregation for a batch
        from(DIRECT_TRIGGER_AGGREGATION)
                .id(BATCH_AGGREGATION_ROUTE)
                .log(INFO, "Triggering aggregation for batch: ${header.batchId}")
                .process(exchange -> {
                    String batchId = exchange.getIn().getHeader(BATCH_ID_HEADER, String.class);

                    if (batchId == null || batchId.isEmpty()) {
                        throw new IllegalArgumentException("batchId header is required");
                    }

                    // Check if batch is ready
                    if (!aggregationUseCase.isBatchComplete(batchId)) {
                        LOG.warnf("Batch %s is not complete, cannot trigger aggregation", batchId);
                        exchange.getIn().setHeader(AGGREGATION_STATUS_HEADER, "INCOMPLETE");
                        return;
                    }

                    // Process the batch
                    try {
                        List<AggregatedEvent> events = aggregationUseCase.processBatch(batchId);
                        exchange.getIn().setBody(events);
                        exchange.getIn().setHeader(AGGREGATION_STATUS_HEADER, "SUCCESS");
                        exchange.getIn().setHeader(EVENT_COUNT_HEADER, events.size());
                        LOG.infof("Aggregation successful for batch %s, created %d events",
                                batchId, events.size());
                    } catch (Exception e) {
                        LOG.errorf("Error during aggregation for batch %s: %s", batchId, e.getMessage());
                        exchange.getIn().setHeader(AGGREGATION_STATUS_HEADER, ERROR_STATUS);
                        throw e;
                    }
                })
                .choice()
                    .when(header(AGGREGATION_STATUS_HEADER).isEqualTo("SUCCESS"))
                        .to("direct:sendEvents")
                .otherwise()
                    .log(WARN, "Skipping event sending due to aggregation status: ${header.aggregationStatus}")
                .end();

        // Route to send aggregated events to Kafka
        from("direct:sendEvents")
                .id(SEND_EVENTS_ROUTE)
                .log(INFO, "Sending ${header.eventCount} events to Kafka for batch: ${header.batchId}")
                .process(exchange -> {
                    String batchId = exchange.getIn().getHeader(BATCH_ID_HEADER, String.class);
                    @SuppressWarnings("unchecked")
                    List<AggregatedEvent> events = exchange.getIn().getBody(List.class);

                    if (events == null || events.isEmpty()) {
                        LOG.info("No events to send, skipping Kafka publishing");
                        return;
                    }

                    // Extract the JSON payloads
                    List<String> payloads = events.stream()
                            .map(AggregatedEvent::getEventJson)
                            .toList();

                    // Set up for Kafka route
                    exchange.getIn().setBody(payloads);
                    exchange.getIn().setHeader(EVENT_COUNT_HEADER, payloads.size());

                    try {
                        // Send events using emitter (this will block until completed)
                        CompletionStage<Void> result = kafkaEmitterUseCase.sendEvents(payloads, batchId);
                        result.toCompletableFuture().join();

                        // Mark events as sent
                        kafkaEmitterUseCase.markEventsAsSent(events);

                        // Update batch status
                        batchProcessingRepository.updateStatus(batchId, COMPLETED_STATUS);

                        LOG.infof("Successfully sent %d events to Kafka for batch %s",
                                payloads.size(), batchId);
                    } catch (Exception e) {
                        LOG.errorf("Error sending events to Kafka: %s", e.getMessage());
                        monitoringUseCase.logIncident("N/A", "KAFKA",
                                "Error sending events: " + e.getMessage(),
                                BATCH_PROCESSING_ROUTE_LOCATION, e, batchId);

                        // Update batch status
                        batchProcessingRepository.updateStatus(batchId, ERROR_STATUS);

                        throw e;
                    }
                })
                .log(INFO, "Kafka sending complete for batch: ${header.batchId}");

        // Timer route that handles batch processing in a single processor
        from("timer:batchChecker?period=" + pollingIntervalMs)
                .routeId(BATCH_POLLING_ROUTE)
                .log(INFO, "Checking for batches ready for processing")
                .process(this::processPendingBatches);
    }

    /**
     * Process that handles checking for pending batches and triggering aggregation
     * This avoids using complex Camel expressions by handling the logic directly
     */
    @Transactional
    protected void processPendingBatches(Exchange exchange) {
        try {
            // Query for batches that are ready for processing
            List<BatchProcessing> readyBatches = batchProcessingRepository.findReadyForProcessing();
            LOG.infof("Found %d batches ready for processing", readyBatches.size());

            if (readyBatches.isEmpty()) {
                LOG.debug("No batches ready for processing");
                return;
            }

            // Get a producer template to send to the aggregation route
            ProducerTemplate producer = camelContext.createProducerTemplate();

            // Process each batch one by one
            for (BatchProcessing batch : readyBatches) {
                String batchId = batch.getBatchId();
                LOG.infof("Sending batch %s for aggregation", batchId);

                try {
                    // Create a new exchange with the batch ID header
                    Exchange batchExchange = camelContext.getEndpoint(DIRECT_TRIGGER_AGGREGATION)
                            .createExchange();
                    batchExchange.getIn().setHeader(BATCH_ID_HEADER, batchId);

                    // Send to the aggregation route
                    producer.send(DIRECT_TRIGGER_AGGREGATION, batchExchange);

                    LOG.infof("Batch %s sent for aggregation", batchId);
                } catch (Exception e) {
                    LOG.errorf("Error processing batch %s: %s", batchId, e.getMessage());
                    monitoringUseCase.logIncident("N/A", "BATCH_PROCESSING",
                            "Error in batch polling: " + e.getMessage(),
                            BATCH_PROCESSING_ROUTE_LOCATION, e, batchId);
                }
            }
        } catch (Exception e) {
            LOG.errorf("Error in batch polling process: %s", e.getMessage());
            monitoringUseCase.logIncident("N/A", "BATCH_PROCESSING",
                    "Error in batch polling: " + e.getMessage(),
                    BATCH_PROCESSING_ROUTE_LOCATION, e, "POLLING");
        }
    }
}