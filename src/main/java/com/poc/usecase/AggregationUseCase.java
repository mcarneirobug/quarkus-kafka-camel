package com.poc.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.poc.model.dto.AggregatedEventDto;
import com.poc.model.dto.ExternalDataDto;
import com.poc.model.dto.InternalDataDto;
import com.poc.model.dto.IsinDataDto;

import com.poc.model.entity.AggregatedEvent;
import com.poc.model.entity.BatchProcessing;
import com.poc.model.entity.ExternalData;
import com.poc.model.entity.InternalData;
import com.poc.model.entity.IsinData;

import com.poc.repository.AggregatedEventRepository;
import com.poc.repository.BatchProcessingRepository;
import com.poc.repository.ExternalDataRepository;
import com.poc.repository.InternalDataRepository;
import com.poc.repository.IsinDataRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import jakarta.transaction.Transactional;

import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * UseCase for aggregating data from the three file types
 */
@ApplicationScoped
public class AggregationUseCase {

    private static final Logger LOG = Logger.getLogger(AggregationUseCase.class);

    private final ExternalDataRepository externalDataRepository;
    private final IsinDataRepository isinDataRepository;
    private final InternalDataRepository internalDataRepository;
    private final AggregatedEventRepository aggregatedEventRepository;
    private final BatchProcessingRepository batchProcessingRepository;
    private final ObjectMapper objectMapper;
    private final MonitoringUseCase monitoringUseCase;

    @Inject
    public AggregationUseCase(ExternalDataRepository externalDataRepository, IsinDataRepository isinDataRepository, InternalDataRepository internalDataRepository, AggregatedEventRepository aggregatedEventRepository, BatchProcessingRepository batchProcessingRepository, ObjectMapper objectMapper, MonitoringUseCase monitoringUseCase) {
        this.externalDataRepository = externalDataRepository;
        this.isinDataRepository = isinDataRepository;
        this.internalDataRepository = internalDataRepository;
        this.aggregatedEventRepository = aggregatedEventRepository;
        this.batchProcessingRepository = batchProcessingRepository;
        this.objectMapper = objectMapper;
        this.monitoringUseCase = monitoringUseCase;
    }

    /**
     * Process a batch by aggregating data from all three sources
     */
    @Transactional
    public List<AggregatedEvent> processBatch(String batchId) {
        LOG.infof("Processing batch aggregation: %s", batchId);

        // Set batch to processing status
        BatchProcessing batch = batchProcessingRepository.findById(batchId);
        if (batch == null) {
            LOG.errorf("Batch %s not found", batchId);
            throw new IllegalStateException("Batch not found: " + batchId);
        }

        if (!batch.hasAllFileTypes()) {
            LOG.warnf("Batch %s does not have all required file types", batchId);
            throw new IllegalStateException("Batch is missing required file types: " + batchId);
        }

        batchProcessingRepository.updateStatus(batchId, "PROCESSING");

        try {
            // Get all records for this batch
            List<ExternalData> externalRecords = externalDataRepository.findByBatchId(batchId);
            List<IsinData> isinRecords = isinDataRepository.findByBatchId(batchId);
            List<InternalData> internalRecords = internalDataRepository.findByBatchId(batchId);

            LOG.infof("Found %d external, %d ISIN, and %d internal records for batch %s",
                    externalRecords.size(), isinRecords.size(), internalRecords.size(), batchId);

            // Create maps by correlation key for faster lookup
            Map<String, ExternalData> externalMap = externalRecords.stream()
                    .collect(Collectors.toMap(ExternalData::getCorrelationKey, data -> data, (a, b) -> a));

            Map<String, IsinData> isinMap = isinRecords.stream()
                    .collect(Collectors.toMap(IsinData::getCorrelationKey, data -> data, (a, b) -> a));

            Map<String, InternalData> internalMap = internalRecords.stream()
                    .collect(Collectors.toMap(InternalData::getCorrelationKey, data -> data, (a, b) -> a));

            // Collect all correlation keys
            Map<String, Boolean> allKeys = new HashMap<>();
            externalRecords.forEach(r -> allKeys.put(r.getCorrelationKey(), true));
            isinRecords.forEach(r -> allKeys.put(r.getCorrelationKey(), true));
            internalRecords.forEach(r -> allKeys.put(r.getCorrelationKey(), true));

            List<AggregatedEvent> aggregatedEvents = new ArrayList<>();

            // Create aggregated events for each correlation key
            for (String key : allKeys.keySet()) {
                try {
                    AggregatedEventDto dto = new AggregatedEventDto();
                    dto.setTimestamp(LocalDateTime.now());
                    dto.setCorrelationId(key);

                    // Add data from each source if available
                    if (externalMap.containsKey(key)) {
                        ExternalData externalData = externalMap.get(key);
                        ExternalDataDto externalDto = new ExternalDataDto();
                        externalDto.setExternalId(externalData.getExternalId());
                        externalDto.setExternalName(externalData.getExternalName());
                        externalDto.setExternalValue(externalData.getExternalValue().doubleValue());
                        dto.setExternalData(externalDto);
                    }

                    if (isinMap.containsKey(key)) {
                        IsinData isinData = isinMap.get(key);
                        IsinDataDto isinDto = new IsinDataDto();
                        isinDto.setIsin(isinData.getIsin());
                        isinDto.setIsinDescription(isinData.getIsinDescription());
                        isinDto.setIsinCategory(isinData.getIsinCategory());
                        dto.setIsinData(isinDto);
                    }

                    if (internalMap.containsKey(key)) {
                        InternalData internalData = internalMap.get(key);
                        InternalDataDto internalDto = new InternalDataDto();
                        internalDto.setInternalId(internalData.getInternalId());
                        internalDto.setInternalCode(internalData.getInternalCode());
                        internalDto.setInternalAmount(internalData.getInternalAmount().doubleValue());
                        dto.setInternalData(internalDto);
                    }

                    // Create entity and serialize DTO to JSON
                    AggregatedEvent event = new AggregatedEvent();
                    event.setBatchId(batchId);
                    event.setCorrelationKey(key);
                    event.setEventJson(objectMapper.writeValueAsString(dto));

                    aggregatedEvents.add(event);
                } catch (JsonProcessingException e) {
                    LOG.errorf("Error serializing event for key %s: %s", key, e.getMessage());
                    monitoringUseCase.logIncident("N/A", "AGGREGATION",
                            "Error creating event: " + e.getMessage(),
                            "JSON serialization", e, batchId);
                }
            }

            // Save aggregated events
            if (!aggregatedEvents.isEmpty()) {
                aggregatedEventRepository.saveAll(aggregatedEvents);
                LOG.infof("Created %d aggregated events for batch %s", aggregatedEvents.size(), batchId);
            }

            // Mark all records as processed
            externalDataRepository.markAllAsProcessed(batchId);
            isinDataRepository.markAllAsProcessed(batchId);
            internalDataRepository.markAllAsProcessed(batchId);

            // Update batch status
            batchProcessingRepository.updateStatus(batchId, "AGGREGATED");

            LOG.infof("Batch %s processed successfully", batchId);
            return aggregatedEvents;
        } catch (Exception e) {
            LOG.errorf("Error processing batch %s: %s", batchId, e.getMessage());
            batchProcessingRepository.updateStatus(batchId, "ERROR");
            monitoringUseCase.logIncident("N/A", "AGGREGATION",
                    "Error processing batch: " + e.getMessage(),
                    "AggregationUseCase", e, batchId);
            throw e;
        }
    }

    /**
     * Checks if a batch is complete by having data for all three file types
     */
    public boolean isBatchComplete(String batchId) {
        BatchProcessing batch = batchProcessingRepository.findById(batchId);
        return batch != null && batch.hasAllFileTypes();
    }
}