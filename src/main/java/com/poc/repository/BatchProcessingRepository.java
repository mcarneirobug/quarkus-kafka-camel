package com.poc.repository;

import com.poc.model.entity.BatchProcessing;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class BatchProcessingRepository implements PanacheRepositoryBase<BatchProcessing, String> {

    public List<BatchProcessing> findReadyForProcessing() {
        return list("readyForProcessing = true and processingStatus = 'PENDING'");
    }

    public List<BatchProcessing> findByStatus(String status) {
        return list("processingStatus", status);
    }

    @Transactional
    public BatchProcessing getOrCreateBatch(String batchId) {
        BatchProcessing batchProcessing = findById(batchId);
        if (batchProcessing == null) {
            batchProcessing = new BatchProcessing(batchId);
            persist(batchProcessing);
        }
        return batchProcessing;
    }

    @Transactional
    public void markFileTypeReceived(String batchId, String fileType) {
        BatchProcessing batch = getOrCreateBatch(batchId);
        batch.markFileTypeReceived(fileType);
        batch.setLastUpdated(LocalDateTime.now());
        if (batch.hasAllFileTypes()) {
            batch.setReadyForProcessing(true);
        }
        persistAndFlush(batch);
    }

    @Transactional
    public void updateStatus(String batchId, String status) {
        BatchProcessing batch = findById(batchId);
        if (batch != null) {
            batch.setProcessingStatus(status);
            batch.setLastUpdated(LocalDateTime.now());
            persistAndFlush(batch);
        }
    }
}