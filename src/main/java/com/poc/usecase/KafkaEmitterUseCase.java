package com.poc.usecase;

import com.poc.exception.KafkaException;
import com.poc.model.generated.ProcessingIncident;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * UseCase para enviar mensagens para o Kafka usando SmallRye Reactive Messaging Emitter
 * Esta é uma implementação alternativa ao componente Camel Kafka
 */
@ApplicationScoped
public class KafkaEmitterUseCase {

    private static final Logger LOG = Logger.getLogger(KafkaEmitterUseCase.class);

    private final Emitter<String> kafkaEmitter;
    private final Emitter<String> incidentEmitter;
    private final MonitoringUseCase monitoringUseCase;

    @ConfigProperty(name = "app.output.kafka.topic")
    String kafkaTopic;

    @ConfigProperty(name = "app.monitoring.incident-topic")
    String incidentTopic;

    public KafkaEmitterUseCase(@Channel("aggregated-events") Emitter<String> kafkaEmitter, @Channel("incident-channel") Emitter<String> incidentEmitter, MonitoringUseCase monitoringUseCase) {
        this.kafkaEmitter = kafkaEmitter;
        this.incidentEmitter = incidentEmitter;
        this.monitoringUseCase = monitoringUseCase;
    }

    /**
     * Envia uma lista de eventos para o Kafka
     */
    public CompletionStage<Void> sendEvents(List<String> events, String correlationId) {
        LOG.infof("Enviando %d eventos para o Kafka via Emitter", events.size());

        try {
            CompletableFuture<Void> allSent = CompletableFuture.completedFuture(null);

            for (String event : events) {
                // Combina cada envio em uma única CompletableFuture
                CompletionStage<Void> sent = kafkaEmitter.send(event)
                        .thenAccept(v -> LOG.debugf("Evento enviado com sucesso para o tópico %s", kafkaTopic))
                        .exceptionally(ex -> {
                            LOG.errorf("Erro ao enviar evento para o Kafka: %s", ex.getMessage());
                            return null;
                        });

                allSent = CompletableFuture.allOf(allSent, sent.toCompletableFuture());
            }

            return allSent.thenAccept(v ->
                    LOG.infof("Todos os %d eventos enviados com sucesso para o tópico %s",
                            events.size(), kafkaTopic));

        } catch (Exception e) {
            LOG.errorf("Falha ao enviar eventos para o Kafka: %s", e.getMessage());

            // Criar incidente
            ProcessingIncident incident = monitoringUseCase.createIncident(
                    "N/A", "KAFKA",
                    "Falha ao enviar eventos para o Kafka: " + e.getMessage(),
                    "KafkaEmitterUseCase", e,
                    correlationId, "MANUAL_INTERVENTION_REQUIRED");

            throw new KafkaException("Falha ao enviar eventos para o Kafka", e, incident);
        }
    }

    /**
     * Envia um único evento para o Kafka
     */
    public CompletionStage<Void> sendEvent(String event, String correlationId) {
        LOG.infof("Enviando evento para o Kafka via Emitter");

        try {
            return kafkaEmitter.send(event)
                    .thenAccept(v -> LOG.infof("Evento enviado com sucesso para o tópico %s", kafkaTopic))
                    .exceptionally(ex -> {
                        LOG.errorf("Erro ao enviar evento para o Kafka: %s", ex.getMessage());

                        // Criar incidente
                        ProcessingIncident incident = monitoringUseCase.createIncident(
                                "N/A", "KAFKA",
                                "Falha ao enviar evento para o Kafka: " + ex.getMessage(),
                                "KafkaEmitterUseCase", (Exception) ex,
                                correlationId, "MANUAL_INTERVENTION_REQUIRED");

                        monitoringUseCase.registerIncident(incident);
                        return null;
                    });
        } catch (Exception e) {
            LOG.errorf("Falha ao enviar evento para o Kafka: %s", e.getMessage());

            // Criar incidente
            ProcessingIncident incident = monitoringUseCase.createIncident(
                    "N/A", "KAFKA",
                    "Falha ao enviar evento para o Kafka: " + e.getMessage(),
                    "KafkaEmitterUseCase", e,
                    correlationId, "MANUAL_INTERVENTION_REQUIRED");

            throw new KafkaException("Falha ao enviar evento para o Kafka", e, incident);
        }
    }

    public CompletionStage<Void> sendIncident(String incidentJson, String correlationId) {
        LOG.infof("Enviando incidente para o Kafka via Emitter para o tópico %s", incidentTopic);

        try {
            return incidentEmitter.send(incidentJson)
                    .thenAccept(v -> LOG.infof("Incidente enviado com sucesso para o tópico %s", incidentTopic))
                    .exceptionally(ex -> {
                        LOG.errorf("Erro ao enviar incidente para o Kafka: %s", ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            LOG.errorf("Falha ao enviar incidente para o Kafka: %s", e.getMessage());

            // Aqui não criamos um novo incidente para evitar loop infinito
            return CompletableFuture.completedFuture(null);
        }
    }
}