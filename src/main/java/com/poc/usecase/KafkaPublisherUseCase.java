package com.poc.usecase;

import com.poc.model.entity.AggregatedEvent;
import com.poc.repository.AggregatedEventRepository;

import jakarta.enterprise.context.ApplicationScoped;

import jakarta.inject.Inject;

import jakarta.transaction.Transactional;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;

import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class KafkaPublisherUseCase {

    private static final Logger LOG = Logger.getLogger(KafkaPublisherUseCase.class);

    private final AggregatedEventRepository aggregatedEventRepository;
    private final MonitoringUseCase monitoringUseCase;
    private final Emitter<String> eventEmitter;

    @ConfigProperty(name = "app.processing.batch-size", defaultValue = "100")
    int batchSize;

    @ConfigProperty(name = "app.output.kafka.topic")
    String kafkaTopic;

    @Inject
    public KafkaPublisherUseCase(AggregatedEventRepository aggregatedEventRepository, MonitoringUseCase monitoringUseCase, @Channel("aggregated-events") Emitter<String> eventEmitter) {
        this.aggregatedEventRepository = aggregatedEventRepository;
        this.monitoringUseCase = monitoringUseCase;
        this.eventEmitter = eventEmitter;
    }

    /**
     * Send aggregated events to Kafka for a specific batch
     *
     * @param batchId The batch ID
     * @return CompletionStage that completes when all messages are sent
     */
    public CompletionStage<Void> sendEventsForBatch(String batchId) {
        LOG.infof("Sending events for batch %s to Kafka topic %s", batchId, kafkaTopic);

        List<AggregatedEvent> events = aggregatedEventRepository.list("batchId = ?1 and sentToKafka = false", batchId);

        if (events.isEmpty()) {
            LOG.infof("No pending events found for batch %s", batchId);
            return CompletableFuture.completedFuture(null);
        }

        LOG.infof("Found %d events to send for batch %s", events.size(), batchId);

        List<String> payloads = events.stream()
                .map(AggregatedEvent::getEventJson)
                .toList();

        return sendEvents(payloads, batchId)
                .thenAccept(v -> markEventsAsSent(events));
    }

    /**
     * Send a list of event payloads to Kafka
     *
     * @param events The event payloads to send
     * @param correlationId The correlation ID for monitoring
     * @return CompletionStage that completes when all messages are sent
     */
    public CompletionStage<Void> sendEvents(List<String> events, String correlationId) {
        LOG.infof("Sending %d events to Kafka topic %s", events.size(), kafkaTopic);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String payload : events) {
            CompletableFuture<Void> future = new CompletableFuture<>();

            try {
                Message<String> message = Message.of(payload, () -> {
                    future.complete(null);
                    return CompletableFuture.completedFuture(null);
                });

                eventEmitter.send(message);
            } catch (Exception e) {
                LOG.errorf("Error sending message to Kafka: %s", e.getMessage());
                monitoringUseCase.logIncident("N/A", "KAFKA",
                        "Error sending message: " + e.getMessage(),
                        "KafkaEmitterUseCase", e, correlationId);

                future.completeExceptionally(e);
            }

            futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    /**
     * Mark events as sent to Kafka
     */
    @Transactional
    public void markEventsAsSent(List<AggregatedEvent> events) {
        List<UUID> ids = events.stream()
                .map(AggregatedEvent::getId)
                .toList();

        aggregatedEventRepository.markAllAsSent(ids);
        LOG.infof("Marked %d events as sent to Kafka", events.size());
    }

    /**
     * Send a single event to Kafka
     */
    @Transactional
    public CompletionStage<Void> sendEvent(AggregatedEvent event) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        try {
            Message<String> message = Message.of(event.getEventJson(), () -> {
                try {
                    event.markAsSent();
                    aggregatedEventRepository.persistAndFlush(event);
                    LOG.infof("Event %s sent and marked as sent", event.getId());
                    future.complete(null);
                } catch (Exception e) {
                    LOG.errorf("Error updating event status: %s", e.getMessage());
                    future.completeExceptionally(e);
                }
                return CompletableFuture.completedFuture(null);
            });

            eventEmitter.send(message);
            LOG.infof("Event %s sent to Kafka topic %s", event.getId(), kafkaTopic);
        } catch (Exception e) {
            LOG.errorf("Error sending event to Kafka: %s", e.getMessage());
            monitoringUseCase.logIncident("N/A", "KAFKA",
                    "Error sending event: " + e.getMessage(),
                    "KafkaEmitterUseCase", e, event.getBatchId());

            future.completeExceptionally(e);
        }

        return future;
    }
}