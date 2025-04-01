package com.poc.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "internal_data")
public class InternalData {

    @Id
    @Column(name = "id")
    private UUID id;

    @NotEmpty
    @Column(name = "batch_id", nullable = false)
    private String batchId;

    @NotEmpty
    @Column(name = "internal_id", nullable = false)
    private String internalId;

    @NotEmpty
    @Column(name = "internal_code", nullable = false)
    private String internalCode;

    @NotNull
    @Column(name = "internal_amount", nullable = false)
    private BigDecimal internalAmount;

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
    public InternalData() {
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

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public String getInternalCode() {
        return internalCode;
    }

    public void setInternalCode(String internalCode) {
        this.internalCode = internalCode;
    }

    public BigDecimal getInternalAmount() {
        return internalAmount;
    }

    public void setInternalAmount(BigDecimal internalAmount) {
        this.internalAmount = internalAmount;
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