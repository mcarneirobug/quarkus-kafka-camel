package com.poc.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "batch_processing")
public class BatchProcessing {

    @Id
    @Column(name = "batch_id")
    private String batchId;

    @NotNull
    @Column(name = "has_external", nullable = false)
    private boolean hasExternal;

    @NotNull
    @Column(name = "has_isin", nullable = false)
    private boolean hasIsin;

    @NotNull
    @Column(name = "has_internal", nullable = false)
    private boolean hasInternal;

    @NotNull
    @Column(name = "ready_for_processing", nullable = false)
    private boolean readyForProcessing;

    @NotEmpty
    @Column(name = "processing_status", nullable = false)
    private String processingStatus;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @NotNull
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    public BatchProcessing() {
        this.createdAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.processingStatus = "PENDING";
    }

    public BatchProcessing(String batchId) {
        this();
        this.batchId = batchId;
    }

    // Getters and setters
    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public boolean isHasExternal() {
        return hasExternal;
    }

    public void setHasExternal(boolean hasExternal) {
        this.hasExternal = hasExternal;
    }

    public boolean isHasIsin() {
        return hasIsin;
    }

    public void setHasIsin(boolean hasIsin) {
        this.hasIsin = hasIsin;
    }

    public boolean isHasInternal() {
        return hasInternal;
    }

    public void setHasInternal(boolean hasInternal) {
        this.hasInternal = hasInternal;
    }

    public boolean isReadyForProcessing() {
        return readyForProcessing;
    }

    public void setReadyForProcessing(boolean readyForProcessing) {
        this.readyForProcessing = readyForProcessing;
    }

    public String getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(String processingStatus) {
        this.processingStatus = processingStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * Updates the processing status and last updated timestamp
     */
    public void updateStatus(String status) {
        this.processingStatus = status;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Checks if all required file types have been received
     */
    public boolean hasAllFileTypes() {
        return hasExternal && hasIsin && hasInternal;
    }

    /**
     * Sets a file type as received and updates the ready status
     */
    public void markFileTypeReceived(String fileType) {
        switch (fileType) {
            case "EXTERNAL":
                this.hasExternal = true;
                break;
            case "ISIN":
                this.hasIsin = true;
                break;
            case "INTERNAL":
                this.hasInternal = true;
                break;
        }

        this.readyForProcessing = hasAllFileTypes();
        this.lastUpdated = LocalDateTime.now();
    }
}