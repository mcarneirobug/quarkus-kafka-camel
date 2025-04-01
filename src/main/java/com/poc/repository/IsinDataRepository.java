package com.poc.repository;

import com.poc.model.entity.IsinData;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class IsinDataRepository implements PanacheRepositoryBase<IsinData, UUID> {

    public List<IsinData> findByBatchId(String batchId) {
        return list("batchId", batchId);
    }

    public List<IsinData> findByBatchIdAndCorrelationKey(String batchId, String correlationKey) {
        return list("batchId = ?1 and correlationKey = ?2", batchId, correlationKey);
    }

    public List<IsinData> findPendingByBatchId(String batchId) {
        return list("batchId = ?1 and processed = false", batchId);
    }

    public boolean existsByBatchId(String batchId) {
        return count("batchId", batchId) > 0;
    }

    public long markAllAsProcessed(String batchId) {
        return update("processed = true where batchId = ?1", batchId);
    }

    public void saveAll(List<IsinData> records) {
        for (IsinData isin : records) {
            persist(isin);
        }
    }
}