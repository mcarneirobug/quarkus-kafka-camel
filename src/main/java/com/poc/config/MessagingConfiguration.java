package com.poc.config;

import com.poc.usecase.MonitoringUseCase;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.jboss.logging.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Configuração do SmallRye Reactive Messaging para Kafka
 */
@ApplicationScoped
public class MessagingConfiguration {

    private static final Logger LOG = Logger.getLogger(MessagingConfiguration.class);

    private final MonitoringUseCase monitoringUseCase;

    @ConfigProperty(name = "app.output.kafka.topic")
    String kafkaTopic;

    @ConfigProperty(name = "app.monitoring.incident-topic")
    String incidentTopic;

    @Inject
    public MessagingConfiguration(MonitoringUseCase monitoringUseCase) {
        this.monitoringUseCase = monitoringUseCase;
    }

    /**
     * Canal para envio de eventos agregados para o Kafka
     */
    @Outgoing("kafka-events")
    @Incoming("aggregated-events")
    @Broadcast
    public CompletionStage<String> processKafkaEvents(String event) {
        LOG.infof("Processando evento para envio ao Kafka (tópico: %s): %s",
                kafkaTopic, event.substring(0, Math.min(100, event.length())) + "...");

        // Aqui poderíamos usar o monitoringUseCase para registrar métricas se necessário
        // Por exemplo: monitoringUseCase.recordKafkaEventMetric(event);

        return CompletableFuture.completedFuture(event);
    }

    /**
     * Canal para envio de incidentes para o Kafka
     */
    @Outgoing("kafka-incidents")
    @Incoming("incident-channel")
    @Broadcast
    public CompletionStage<String> processIncidents(String incident) {
        LOG.infof("Processando incidente para envio ao Kafka (tópico: %s): %s",
                incidentTopic, incident.substring(0, Math.min(100, incident.length())) + "...");

        // Aqui poderíamos usar o monitoringUseCase para registrar métricas se necessário
        // Por exemplo: monitoringUseCase.recordIncidentEventMetric(incident);

        return CompletableFuture.completedFuture(incident);
    }

    /**
     * Método para processamento assíncrono de eventos
     * Pode ser usado para lógica mais complexa se necessário
     */
    private CompletionStage<String> processEventAsync(String event) {
        return CompletableFuture.supplyAsync(() -> {
            // Processamento opcional aqui
            return event;
        });
    }
}