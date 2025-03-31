package com.poc.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.exception.*;

import com.poc.model.generated.Details;
import com.poc.model.generated.ProcessingIncident;
import com.poc.model.generated.Resolution;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import org.jboss.logging.Logger;

import java.util.Date;
import java.util.UUID;

/**
 * UseCase responsável pelo monitoramento e criação de incidentes
 */
@ApplicationScoped
public class MonitoringUseCase {

    private static final Logger LOG = Logger.getLogger(MonitoringUseCase.class);

    @ConfigProperty(name = "app.monitoring.incident-topic")
    String incidentTopic;

    private final Emitter<String> incidentEmitter;
    private final ObjectMapper objectMapper;

    public MonitoringUseCase(@Channel("incident-channel") Emitter<String> incidentEmitter, ObjectMapper objectMapper) {
        this.incidentEmitter = incidentEmitter;
        this.objectMapper = objectMapper;
    }

    /**
     * Registra métricas e logs para o processamento de arquivos
     */
    public void recordFileProcessingMetric(String fileType, boolean success, long processingTimeMs) {
        LOG.infof("Arquivo %s processado: sucesso=%s, tempo=%dms", fileType, success, processingTimeMs);
    }

    /**
     * Cria um incidente para um erro no processamento de arquivos
     */
    public ProcessingIncident createIncident(String fileName, String fileTypeStr, String errorMessage, String errorLocation,
                                             Exception exception, String correlationId, String actionStr) {

        ProcessingIncident incident = new ProcessingIncident();
        incident.setIncidentId(UUID.randomUUID().toString());
        incident.setTimestamp(new Date());
        incident.setSeverity(ProcessingIncident.Severity.ERROR);
        incident.setCorrelationId(correlationId);
        incident.setMessage(errorMessage);

        incident.setType(getType(exception));

        // Detalhes do incidente
        Details details = new Details();
        details.setFilename(fileName);

        Details.FileType fileType;
        try {
            fileType = Details.FileType.valueOf(fileTypeStr);
        } catch (IllegalArgumentException e) {
            // Fallback para UNKNOWN se o valor não corresponder ao enum
            fileType = null;
            LOG.warnf("Tipo de arquivo desconhecido: %s, usando UNKNOWN", fileTypeStr);
        }
        details.setFileType(fileType);
        details.setErrorLocation(errorLocation);
        details.setException(exception != null ? exception.toString() : "Unknown");
        incident.setDetails(details);

        // Resolução
        Resolution resolution = new Resolution();
        Resolution.Action action;
        try {
            action = Resolution.Action.valueOf(actionStr);
        } catch (IllegalArgumentException e) {
            // Fallback para MANUAL_INTERVENTION_REQUIRED se o valor não corresponder ao enum
            action = Resolution.Action.MANUAL_INTERVENTION_REQUIRED;
            LOG.warnf("Ação desconhecida: %s, usando MANUAL_INTERVENTION_REQUIRED", actionStr);
        }
        resolution.setAction(action);

        if ("MOVED_TO_ERROR".equals(action)) {
            resolution.setErrorFilePath("./errors/" + fileName);
        }
        incident.setResolution(resolution);

        return incident;
    }

    private static ProcessingIncident.Type getType(Exception exception) {
        ProcessingIncident.Type type;

        if (exception instanceof FileIOException) {
            type = ProcessingIncident.Type.IO_ERROR;
        } else if (exception instanceof FileValidationException) {
            type = ProcessingIncident.Type.VALIDATION_ERROR;
        } else if (exception instanceof FileTransformationException) {
            type = ProcessingIncident.Type.TRANSFORMATION_ERROR;
        } else if (exception instanceof BusinessLogicException) {
            type = ProcessingIncident.Type.BUSINESS_LOGIC_ERROR;
        } else if (exception instanceof KafkaException) {
            type = ProcessingIncident.Type.KAFKA_ERROR;
        } else {
            type = ProcessingIncident.Type.BUSINESS_LOGIC_ERROR; // Padrão
        }
        return type;
    }

    /**
     * Registra o incidente em logs, métricas e envia para o Kafka
     */
    public void registerIncident(ProcessingIncident incident) {
        try {
            // Log detalhado do incidente
            LOG.errorf("INCIDENTE [%s]: %s - %s. Arquivo: %s, Correlação: %s",
                    incident.getIncidentId(),
                    incident.getType(),
                    incident.getMessage(),
                    incident.getDetails().getFilename(),
                    incident.getCorrelationId());

            // Enviar para o Kafka via Emitter
            String incidentJson = objectMapper.writeValueAsString(incident);
            incidentEmitter.send(incidentJson);

            LOG.info("Incidente enviado para o tópico: " + incidentTopic);
        } catch (Exception e) {
            LOG.error("Falha ao registrar incidente", e);
        }
    }

    /**
     * Método completo para criar e registrar um incidente a partir de uma exceção
     */
    public void handleException(FileProcessingException exception, String correlationId) {
        ProcessingIncident incident;

        // Se já houver um incidente na exceção, utiliza ele
        if (exception.getIncident() != null) {
            incident = exception.getIncident();
        } else {
            // Extrai informações da exceção
            String fileName = "unknown";
            String fileType = "UNKNOWN";
            String errorLocation = "unknown";

            incident = createIncident(
                    fileName, fileType, exception.getMessage(),
                    errorLocation, exception, correlationId, "MANUAL_INTERVENTION_REQUIRED"
            );
        }

        registerIncident(incident);
    }
}