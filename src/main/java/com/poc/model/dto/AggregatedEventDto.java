package com.poc.model.dto;

import java.time.LocalDateTime;

/**
 * DTO for aggregated event that combines data from all sources
 */
public class AggregatedEventDto {
    private String correlationId;
    private LocalDateTime timestamp;
    private ExternalDataDto externalData;
    private IsinDataDto isinData;
    private InternalDataDto internalData;

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public ExternalDataDto getExternalData() {
        return externalData;
    }

    public void setExternalData(ExternalDataDto externalData) {
        this.externalData = externalData;
    }

    public IsinDataDto getIsinData() {
        return isinData;
    }

    public void setIsinData(IsinDataDto isinData) {
        this.isinData = isinData;
    }

    public InternalDataDto getInternalData() {
        return internalData;
    }

    public void setInternalData(InternalDataDto internalData) {
        this.internalData = internalData;
    }
}