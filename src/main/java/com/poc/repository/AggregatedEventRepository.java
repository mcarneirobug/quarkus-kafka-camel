package com.poc.repository;

import com.poc.model.entity.AggregatedEvent;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class AggregatedEventRepository implements PanacheRepositoryBase<AggregatedEvent, UUID> {

    public List<AggregatedEvent> findPendingEvents() {
        return list("sentToKafka = false");
    }

    public List<AggregatedEvent> findByBatchId(String batchId) {
        return list("batchId", batchId);
    }

    public List<AggregatedEvent> findByBatchIdAndCorrelationKey(String batchId, String correlationKey) {
        return list("batchId = ?1 and correlationKey = ?2", batchId, correlationKey);
    }

    @Transactional
    public void markAsSent(UUID id) {
        AggregatedEvent event = findById(id);
        if (event != null) {
            event.markAsSent();
            persistAndFlush(event);
        }
    }

    @Transactional
    public void markAllAsSent(List<UUID> ids) {
        update("sentToKafka = true, sentAt = ?1 where id in ?2",
                LocalDateTime.now(), ids);
    }

    @Transactional
    public void saveAll(List<AggregatedEvent> events) {
        for (AggregatedEvent event : events) {
            persist(event);
        }
    }
}