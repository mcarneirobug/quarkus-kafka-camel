package com.poc.utils;

import com.poc.model.entity.BatchProcessing;
import com.poc.repository.BatchProcessingRepository;

import jakarta.enterprise.context.ApplicationScoped;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;

import org.jboss.logging.Logger;

@ApplicationScoped
public class RouteUtils {
    private static final Logger LOG = Logger.getLogger(RouteUtils.class);

    private final BatchProcessingRepository batchProcessingRepository;
    private final CamelContext camelContext;

    @Inject
    public RouteUtils(BatchProcessingRepository batchProcessingRepository,
            CamelContext camelContext) {
        this.batchProcessingRepository = batchProcessingRepository;
        this.camelContext = camelContext;
    }

    /**
     * Checks if all required file types have been received and triggers aggregation if complete
     *
     * @param correlationId The correlation ID (batch ID) to check
     */
    @Transactional
    public void checkAndTriggerAggregation(String correlationId) {
        LOG.infof("Checking if batch %s is ready for aggregation", correlationId);

        // Get batch status
        BatchProcessing batch = batchProcessingRepository.findById(correlationId);
        if (batch == null) {
            LOG.warnf("Batch %s not found, cannot check aggregation readiness", correlationId);
            return;
        }

        // Check if batch is ready
        if (batch.isReadyForProcessing() && batch.hasAllFileTypes() &&
                "PENDING".equals(batch.getProcessingStatus())) {
            LOG.infof("Batch %s is ready for aggregation, triggering processing", correlationId);

            try {
                // Use the producer template to trigger aggregation
                ProducerTemplate producer = camelContext.createProducerTemplate();
                producer.sendBodyAndHeader("direct:triggerAggregation", null, "batchId", correlationId);

                LOG.infof("Successfully triggered aggregation for batch %s", correlationId);
            } catch (Exception e) {
                LOG.errorf("Failed to trigger aggregation for batch %s: %s", correlationId, e.getMessage());
            }
        } else {
            LOG.infof("Batch %s is not yet ready for aggregation (ready=%s, hasAllTypes=%s, status=%s)",
                    correlationId, batch.isReadyForProcessing(), batch.hasAllFileTypes(),
                    batch.getProcessingStatus());
        }
    }
}