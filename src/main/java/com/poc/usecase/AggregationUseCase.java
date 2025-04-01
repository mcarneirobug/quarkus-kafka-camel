package com.poc.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.exception.BusinessLogicException;
import com.poc.model.ExternalCsvRecord;
import com.poc.model.InternalCsvRecord;
import com.poc.model.IsinCsvRecord;
import com.poc.model.generated.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * UseCase for aggregating data from the three file types
 */
@ApplicationScoped
public class AggregationUseCase {

    private static final Logger LOG = Logger.getLogger(AggregationUseCase.class);
    private static final String ESH_PREFIX = "ESH_";

    @Inject
    ObjectMapper objectMapper;

    @Inject
    MonitoringUseCase monitoringUseCase;

    // Record storage by type
    private final Map<String, List<ExternalCsvRecord>> externalRecords = new ConcurrentHashMap<>();
    private final Map<String, List<IsinCsvRecord>> isinRecords = new ConcurrentHashMap<>();
    private final Map<String, List<InternalCsvRecord>> internalRecords = new ConcurrentHashMap<>();

    /**
     * Standardizes the correlation ID to ensure consistent format
     * Removes "ESH_" prefix if present
     */
    private String standardizeCorrelationId(String correlationId) {
        if (correlationId == null) {
            LOG.warn("Null correlationId provided to standardization method");
            return null;
        }

        String standardized = correlationId;
        if (standardized.startsWith(ESH_PREFIX)) {
            standardized = standardized.substring(ESH_PREFIX.length());
            LOG.debugf("Standardized correlationId from %s to %s", correlationId, standardized);
        }

        return standardized;
    }

    /**
     * Stores external file records for later correlation
     */
    public void addExternalBatch(List<ExternalCsvRecord> records, String correlationId) {
        String standardId = standardizeCorrelationId(correlationId);
        LOG.infof("Adding external records batch: count=%d, correlationId=%s",
                records.size(), standardId);
        externalRecords.put(standardId, records);
    }

    /**
     * Stores ISIN file records for later correlation
     */
    public void addIsinBatch(List<IsinCsvRecord> records, String correlationId) {
        String standardId = standardizeCorrelationId(correlationId);
        LOG.infof("Adding ISIN records batch: count=%d, correlationId=%s",
                records.size(), standardId);
        isinRecords.put(standardId, records);
    }

    /**
     * Stores internal file records for later correlation
     */
    public void addInternalBatch(List<InternalCsvRecord> records, String correlationId) {
        String standardId = standardizeCorrelationId(correlationId);
        LOG.infof("Adding internal records batch: count=%d, correlationId=%s",
                records.size(), standardId);
        internalRecords.put(standardId, records);
    }

    /**
     * Checks if all types of files have been received for a correlationId
     */
    public boolean isReadyForAggregation(String correlationId) {
        String standardId = standardizeCorrelationId(correlationId);

        boolean hasExternal = !externalRecords.isEmpty() && externalRecords.containsKey(standardId);
        boolean hasIsin = !isinRecords.isEmpty() && isinRecords.containsKey(standardId);
        boolean hasInternal = !internalRecords.isEmpty() && internalRecords.containsKey(standardId);

        boolean isReady = hasExternal && hasIsin && hasInternal;

        LOG.infof("Checking aggregation for batch %s: external=%s, isin=%s, internal=%s, isReady=%s",
                standardId, hasExternal, hasIsin, hasInternal, isReady);

        return isReady;
    }

    /**
     * Aggregates records based on the correlation key between records
     */
    public List<AggregatedEvent> aggregateEvents(String batchCorrelationId) {
        String standardId = standardizeCorrelationId(batchCorrelationId);
        LOG.infof("Starting aggregation for batch: %s", standardId);

        if (!isReadyForAggregation(standardId)) {
            ProcessingIncident incident = monitoringUseCase.createIncident(
                    "N/A", "AGGREGATION",
                    "Attempted aggregation without all necessary files",
                    "AggregationUseCase", null, standardId, "MANUAL_INTERVENTION_REQUIRED");

            throw new BusinessLogicException(
                    "Cannot aggregate events without all file types", incident);
        }

        List<ExternalCsvRecord> externalBatch = externalRecords.get(standardId);
        List<IsinCsvRecord> isinBatch = isinRecords.get(standardId);
        List<InternalCsvRecord> internalBatch = internalRecords.get(standardId);

        // Map records by correlation key
        Map<String, ExternalCsvRecord> externalMap = externalBatch.stream()
                .collect(Collectors.toMap(ExternalCsvRecord::getCorrelationKey, r -> r, (r1, r2) -> r1));

        Map<String, IsinCsvRecord> isinMap = isinBatch.stream()
                .collect(Collectors.toMap(IsinCsvRecord::getCorrelationKey, r -> r, (r1, r2) -> r1));

        Map<String, InternalCsvRecord> internalMap = internalBatch.stream()
                .collect(Collectors.toMap(InternalCsvRecord::getCorrelationKey, r -> r, (r1, r2) -> r1));

        // Build unique set of correlation keys
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(externalMap.keySet());
        allKeys.addAll(isinMap.keySet());
        allKeys.addAll(internalMap.keySet());

        List<Exception> errors = new ArrayList<>();
        List<AggregatedEvent> aggregatedEvents = new ArrayList<>();

        for (String key : allKeys) {
            try {
                AggregatedEvent event = new AggregatedEvent();
                event.setEventId(UUID.randomUUID().toString());
                event.setTimestamp(new Date());
                event.setCorrelationId(key);

                // Processing information
                ProcessingInfo processingInfo = new ProcessingInfo();
                processingInfo.setProcessedAt(new Date());
                processingInfo.setSourceBatch(standardId);

                // Determine if complete or partial
                boolean hasAllData = externalMap.containsKey(key) &&
                        isinMap.containsKey(key) &&
                        internalMap.containsKey(key);

                processingInfo.setStatus(hasAllData ?
                        ProcessingInfo.Status.COMPLETE :
                        ProcessingInfo.Status.PARTIAL);
                event.setProcessingInfo(processingInfo);

                // Add external data if available
                if (externalMap.containsKey(key)) {
                    ExternalCsvRecord externalRecord = externalMap.get(key);
                    ExternalData externalData = new ExternalData();
                    externalData.setExternalId(externalRecord.getExternalId());
                    externalData.setExternalName(externalRecord.getExternalName());
                    externalData.setExternalValue(externalRecord.getExternalValue().doubleValue());
                    event.setExternalData(externalData);
                }

                // Add ISIN data if available
                if (isinMap.containsKey(key)) {
                    IsinCsvRecord isinRecord = isinMap.get(key);
                    IsinData isinData = new IsinData();
                    isinData.setIsin(isinRecord.getIsin());
                    isinData.setIsinDescription(isinRecord.getIsinDescription());
                    isinData.setIsinCategory(isinRecord.getIsinCategory());
                    event.setIsinData(isinData);
                }

                // Add internal data if available
                if (internalMap.containsKey(key)) {
                    InternalCsvRecord internalRecord = internalMap.get(key);
                    InternalData internalData = new InternalData();
                    internalData.setInternalId(internalRecord.getInternalId());
                    internalData.setInternalCode(internalRecord.getInternalCode());
                    internalData.setInternalAmount(internalRecord.getInternalAmount().doubleValue());
                    event.setInternalData(internalData);
                }

                aggregatedEvents.add(event);
            } catch (Exception e) {
                LOG.errorf("Error aggregating event for key %s: %s", key, e.getMessage());
                ProcessingIncident incident = monitoringUseCase.createIncident(
                        "N/A", "AGGREGATION",
                        "Error aggregating event for key: " + key,
                        "AggregationUseCase", e, standardId, "MANUAL_INTERVENTION_REQUIRED");

                monitoringUseCase.registerIncident(incident);
                errors.add(e);
            }
        }

        // Check for errors
        if (!errors.isEmpty()) {
            // If there were errors, create an incident and don't return anything
            ProcessingIncident incident = monitoringUseCase.createIncident(
                    "N/A", "AGGREGATION",
                    "Errors during data aggregation. Total errors: " + errors.size(),
                    "AggregationUseCase", errors.get(0), standardId, "MANUAL_INTERVENTION_REQUIRED");

            throw new BusinessLogicException(
                    "Errors during data aggregation", errors.get(0), incident);
        }

        LOG.infof("Aggregation complete: batch=%s, events=%d", standardId, aggregatedEvents.size());

        // Clean up batch data from memory after aggregation
        cleanupBatch(standardId);

        return aggregatedEvents;
    }

    /**
     * Cleans up batch data from memory after processing
     */
    private void cleanupBatch(String batchCorrelationId) {
        String standardId = standardizeCorrelationId(batchCorrelationId);
        externalRecords.remove(standardId);
        isinRecords.remove(standardId);
        internalRecords.remove(standardId);
        LOG.infof("Batch data %s removed from memory", standardId);
    }

    /**
     * Serializes aggregated events to JSON
     */
    public List<String> serializeEvents(List<AggregatedEvent> events) {
        return events.stream()
                .map(event -> {
                    try {
                        return objectMapper.writeValueAsString(event);
                    } catch (Exception e) {
                        LOG.errorf("Error serializing event %s: %s", event.getEventId(), e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}