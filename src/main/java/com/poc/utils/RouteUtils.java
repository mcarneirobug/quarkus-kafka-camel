package com.poc.utils;

import com.poc.exception.BusinessLogicException;
import com.poc.model.generated.ProcessingIncident;
import com.poc.usecase.AggregationUseCase;
import com.poc.usecase.MonitoringUseCase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.jboss.logging.Logger;

@ApplicationScoped
public class RouteUtils {
    private static final Logger LOG = Logger.getLogger(RouteUtils.class);

    private final AggregationUseCase aggregationUseCase;
    private final MonitoringUseCase monitoringUseCase;
    private final CamelContext camelContext;

    @Inject
    public RouteUtils(
            AggregationUseCase aggregationUseCase,
            MonitoringUseCase monitoringUseCase,
            CamelContext camelContext) {
        this.aggregationUseCase = aggregationUseCase;
        this.monitoringUseCase = monitoringUseCase;
        this.camelContext = camelContext;
    }

    /**
     * Checks if all files have been received and triggers aggregation if ready
     */
    public void checkAndTriggerAggregation(String correlationId) {
        LOG.debugf("Checking if ready for aggregation with ID: %s", correlationId);

        if (aggregationUseCase.isReadyForAggregation(correlationId)) {
            LOG.infof("All files received for batch %s, initiating aggregation", correlationId);

            // Using try-with-resources to ensure ProducerTemplate is properly closed
            try (ProducerTemplate producerTemplate = camelContext.createProducerTemplate()) {
                producerTemplate.send("direct:aggregateAndSend", exc -> {
                    exc.getIn().setHeader("correlationId", correlationId);
                });
            } catch (Exception e) {
                LOG.errorf("Error triggering aggregation: %s", e.getMessage());

                // Create incident for monitoring
                ProcessingIncident incident = monitoringUseCase.createIncident(
                        "N/A", "AGGREGATION",
                        "Failed to trigger aggregation process",
                        "RouteUtils", e, correlationId, "MANUAL_INTERVENTION_REQUIRED");

                // Throw custom exception
                throw new BusinessLogicException("Failed to trigger aggregation process", e, incident);
            }
        } else {
            LOG.infof("Waiting for other files for batch %s", correlationId);
        }
    }
}