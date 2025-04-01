package com.poc.route;

import com.poc.usecase.AggregationUseCase;
import com.poc.usecase.MonitoringUseCase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import com.poc.exception.BusinessLogicException;
import com.poc.model.generated.ProcessingIncident;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.UUID;

import static org.apache.camel.LoggingLevel.INFO;

@ApplicationScoped
public class AggregationRoute extends RouteBuilder {

    protected static final Logger LOG = Logger.getLogger(AggregationRoute.class);

    private final AggregationUseCase aggregationUseCase;
    private final MonitoringUseCase monitoringUseCase;

    @ConfigProperty(name = "app.kafka-mode", defaultValue = "camel")
    String kafkaMode;

    @Inject
    public AggregationRoute(AggregationUseCase aggregationUseCase, MonitoringUseCase monitoringUseCase) {
        this.aggregationUseCase = aggregationUseCase;
        this.monitoringUseCase = monitoringUseCase;
    }

    @Override
    public void configure() {
        from("direct:aggregateAndSend")
                .id("aggregationRoute")
                .log(INFO, "Starting aggregation for batch: ${header.correlationId}")
                .onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                    String correlationId = exchange.getIn().getHeader("correlationId", String.class);

                    LOG.errorf("Error during aggregation and Kafka sending: %s", exception.getMessage());

                    // Create incident
                    ProcessingIncident incident = monitoringUseCase.createIncident(
                            "N/A", "AGGREGATION", exception.getMessage(),
                            "AggregationRoute", exception, correlationId, "MANUAL_INTERVENTION_REQUIRED");

                    throw new BusinessLogicException("Error aggregating and sending events", exception, incident);
                })
                .to("direct:processError")
                .end()
                .process(exchange -> {
                    String correlationId = exchange.getIn().getHeader("correlationId", String.class);
                    LOG.debugf("Processing aggregation with correlationId: %s", correlationId);

                    // Aggregate correlated events
                    var aggregatedEvents = aggregationUseCase.aggregateEvents(correlationId);

                    // Convert to JSON format
                    List<String> jsonEvents = aggregationUseCase.serializeEvents(aggregatedEvents);

                    exchange.getIn().setBody(jsonEvents);
                    exchange.getIn().setHeader("eventCount", jsonEvents.size());

                    LOG.infof("Aggregated events: %d for batch %s", jsonEvents.size(), correlationId);
                })
                .process(exchange -> {
                    // Add kafka mode as a header for use in the choice expression
                    exchange.getIn().setHeader("kafkaMode", kafkaMode);

                    @SuppressWarnings("unchecked")
                    List<String> events = exchange.getIn().getBody(List.class);
                    LOG.infof("Preparing to send %d events to Kafka", events.size());

                    // Prepare for Kafka sending
                    exchange.getIn().setHeader(KafkaConstants.KEY, UUID.randomUUID().toString());
                })
                // Fixed choice condition
                .choice()
                .when(simple("${header.kafkaMode} == 'camel'"))
                .log(INFO, "Routing to Camel Kafka component")
                .to("direct:sendToCamelKafka")
                .otherwise()
                .log(INFO, "Routing to Emitter")
                .to("direct:sendToEmitter")
                .end();
    }
}