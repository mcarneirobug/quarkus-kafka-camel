package com.poc.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "external_data")
public class ExternalData {

    @Id
    @Column(name = "id")
    private UUID id;

    @NotEmpty
    @Column(name = "batch_id", nullable = false)
    private String batchId;

    @NotEmpty
    @Column(name = "external_id", nullable = false)
    private String externalId;

    @NotEmpty
    @Column(name = "external_name", nullable = false)
    private String externalName;

    @NotNull
    @Column(name = "external_value", nullable = false)
    private BigDecimal externalValue;

    @NotEmpty
    @Column(name = "correlation_key", nullable = false)
    private String correlationKey;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @NotEmpty
    @Column(name = "file_name", nullable = false)
    private String fileName;

    @NotNull
    @Column(name = "processed", nullable = false)
    private boolean processed;

    // Constructors
    public ExternalData() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.processed = false;
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getExternalName() {
        return externalName;
    }

    public void setExternalName(String externalName) {
        this.externalName = externalName;
    }

    public BigDecimal getExternalValue() {
        return externalValue;
    }

    public void setExternalValue(BigDecimal externalValue) {
        this.externalValue = externalValue;
    }

    public String getCorrelationKey() {
        return correlationKey;
    }

    public void setCorrelationKey(String correlationKey) {
        this.correlationKey = correlationKey;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }
}