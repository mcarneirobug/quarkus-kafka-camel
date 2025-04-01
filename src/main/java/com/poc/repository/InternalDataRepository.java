package com.poc.repository;

import com.poc.model.entity.InternalData;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class InternalDataRepository implements PanacheRepositoryBase<InternalData, UUID> {

    public List<InternalData> findByBatchId(String batchId) {
        return list("batchId", batchId);
    }

    public List<InternalData> findByBatchIdAndCorrelationKey(String batchId, String correlationKey) {
        return list("batchId = ?1 and correlationKey = ?2", batchId, correlationKey);
    }

    public List<InternalData> findPendingByBatchId(String batchId) {
        return list("batchId = ?1 and processed = false", batchId);
    }

    public boolean existsByBatchId(String batchId) {
        return count("batchId", batchId) > 0;
    }

    public long markAllAsProcessed(String batchId) {
        return update("processed = true where batchId = ?1", batchId);
    }

    public void saveAll(List<InternalData> records) {
        for (InternalData internal : records) {
            persist(internal);
        }
    }
}