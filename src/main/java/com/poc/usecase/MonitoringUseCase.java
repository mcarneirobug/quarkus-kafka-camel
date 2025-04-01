package com.poc.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.enums.ErrorType;
import com.poc.enums.EventType;
import com.poc.exception.*;

import com.poc.model.generated.Details;
import com.poc.model.generated.ProcessingIncident;
import com.poc.model.generated.Resolution;

import jakarta.enterprise.context.ApplicationScoped;

import jakarta.validation.ValidationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * UseCase responsible for monitoring and error tracking
 */
@ApplicationScoped
public class MonitoringUseCase {

    private static final Logger LOG = Logger.getLogger(MonitoringUseCase.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @ConfigProperty(name = "app.monitoring.incident-topic")
    String incidentTopic;

    private final Emitter<String> incidentEmitter;
    private final ObjectMapper objectMapper;

    public MonitoringUseCase(@Channel("incident-channel") Emitter<String> incidentEmitter, ObjectMapper objectMapper) {
        this.incidentEmitter = incidentEmitter;
        this.objectMapper = objectMapper;
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

    /**
     * Records metrics and logs for file processing
     */
    public void recordFileProcessingMetric(String fileType, boolean success, long processingTimeMs, String fileName, long fileSize) {
        Map<String, Object> logEvent = new HashMap<>();
        logEvent.put("eventType", EventType.FILE_PROCESSED.name());
        logEvent.put("timestamp", LocalDateTime.now().format(DATE_TIME_FORMATTER));
        logEvent.put("fileType", fileType);
        logEvent.put("fileName", fileName);
        logEvent.put("fileSize", fileSize);
        logEvent.put("success", success);
        logEvent.put("processingTimeMs", processingTimeMs);

        logAsJson(EventType.FILE_PROCESSED.name(), logEvent);
    }

    /**
     * Logs an incident in structured JSON format with type-safe error type
     */
    public void logIncident(String fileName, String fileType, String errorMessage,
                            String errorLocation, Exception exception, String correlationId) {

        String incidentId = UUID.randomUUID().toString();

        ErrorType errorType = determineErrorType(exception);

        // Create the incident log entry
        Map<String, Object> incident = new HashMap<>();
        incident.put("eventType", EventType.PROCESSING_INCIDENT.name());
        incident.put("incidentId", incidentId);
        incident.put("timestamp", LocalDateTime.now().format(DATE_TIME_FORMATTER));
        incident.put("severity", "ERROR");
        incident.put("errorType", errorType.name());
        incident.put("message", errorMessage);

        // Add details
        Map<String, Object> details = new HashMap<>();
        details.put("fileName", fileName);
        details.put("fileType", fileType);
        details.put("errorLocation", errorLocation);
        if (exception != null) {
            details.put("exceptionType", exception.getClass().getName());
            details.put("exceptionMessage", exception.getMessage());

            // Add stack trace (first 5 elements)
            StackTraceElement[] stackTrace = exception.getStackTrace();
            if (stackTrace != null && stackTrace.length > 0) {
                List<String> stackTraceLines = Arrays.stream(stackTrace)
                        .limit(5)
                        .map(StackTraceElement::toString)
                        .toList();
                details.put("stackTrace", stackTraceLines);
            }
        }
        incident.put("details", details);
        incident.put("correlationId", correlationId);

        logAsJson(EventType.PROCESSING_INCIDENT.name(), incident);
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
            type = ProcessingIncident.Type.BUSINESS_LOGIC_ERROR;
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
     * Handle exceptions from file processing
     */
    public void handleException(FileProcessingException exception, String correlationId) {
        String fileName = "unknown";
        String fileType = "UNKNOWN";
        String errorLocation = "unknown";

        logIncident(fileName, fileType, exception.getMessage(),
                errorLocation, exception, correlationId);
    }

    /**
     * Serialize and log a structured event as JSON
     */
    private void logAsJson(String eventType, Map<String, Object> event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            LOG.info(json);
        } catch (Exception e) {
            LOG.errorf("Failed to serialize %s event to JSON: %s", eventType, e.getMessage());
            LOG.info(event.toString());
        }
    }

    /**
     * Determine error type based on exception - now returns an enum
     */
    private ErrorType determineErrorType(Exception exception) {
        if (exception == null) {
            return ErrorType.UNKNOWN_ERROR;
        }

        return switch (exception) {
            case java.io.IOException ioException -> ErrorType.IO_ERROR;
            case ValidationException validationException -> ErrorType.VALIDATION_ERROR;
            case FileTransformationException fileTransformationException -> ErrorType.TRANSFORMATION_ERROR;
            case BusinessLogicException businessLogicException -> ErrorType.BUSINESS_LOGIC_ERROR;
            case org.apache.kafka.common.KafkaException kafkaException -> ErrorType.KAFKA_ERROR;
            default -> ErrorType.APP_ERROR;
        };
    }
}