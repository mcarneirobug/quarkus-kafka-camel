package com.poc.repository;

import com.poc.model.entity.ExternalData;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ExternalDataRepository implements PanacheRepositoryBase<ExternalData, UUID> {

    public List<ExternalData> findByBatchId(String batchId) {
        return list("batchId", batchId);
    }

    public List<ExternalData> findByBatchIdAndCorrelationKey(String batchId, String correlationKey) {
        return list("batchId = ?1 and correlationKey = ?2", batchId, correlationKey);
    }

    public List<ExternalData> findPendingByBatchId(String batchId) {
        return list("batchId = ?1 and processed = false", batchId);
    }

    public boolean existsByBatchId(String batchId) {
        return count("batchId", batchId) > 0;
    }

    public long markAllAsProcessed(String batchId) {
        return update("processed = true where batchId = ?1", batchId);
    }

    public void saveAll(List<ExternalData> records) {
        for (ExternalData external : records) {
            persist(external);
        }
    }
}