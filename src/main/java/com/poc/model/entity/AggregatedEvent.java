package com.poc.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "aggregated_events")
public class AggregatedEvent {

    @Id
    @Column(name = "id")
    private UUID id;

    @NotEmpty
    @Column(name = "batch_id", nullable = false)
    private String batchId;

    @NotEmpty
    @Column(name = "correlation_key", nullable = false)
    private String correlationKey;

    @NotEmpty
    @Column(name = "event_json", nullable = false)
    private String eventJson;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @NotNull
    @Column(name = "sent_to_kafka", nullable = false)
    private boolean sentToKafka;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    // Constructors
    public AggregatedEvent() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.sentToKafka = false;
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

    public String getCorrelationKey() {
        return correlationKey;
    }

    public void setCorrelationKey(String correlationKey) {
        this.correlationKey = correlationKey;
    }

    public String getEventJson() {
        return eventJson;
    }

    public void setEventJson(String eventJson) {
        this.eventJson = eventJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isSentToKafka() {
        return sentToKafka;
    }

    public void setSentToKafka(boolean sentToKafka) {
        this.sentToKafka = sentToKafka;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    /**
     * Mark event as sent to Kafka
     */
    public void markAsSent() {
        this.sentToKafka = true;
        this.sentAt = LocalDateTime.now();
    }
}