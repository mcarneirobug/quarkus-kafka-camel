package com.poc.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "isin_data")
public class IsinData {

    @Id
    @Column(name = "id")
    private UUID id;

    @NotEmpty
    @Column(name = "batch_id", nullable = false)
    private String batchId;

    @NotEmpty
    @Column(name = "isin", nullable = false)
    private String isin;

    @NotEmpty
    @Column(name = "isin_description", nullable = false)
    private String isinDescription;

    @NotEmpty
    @Column(name = "isin_category", nullable = false)
    private String isinCategory;

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
    public IsinData() {
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

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getIsinDescription() {
        return isinDescription;
    }

    public void setIsinDescription(String isinDescription) {
        this.isinDescription = isinDescription;
    }

    public String getIsinCategory() {
        return isinCategory;
    }

    public void setIsinCategory(String isinCategory) {
        this.isinCategory = isinCategory;
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