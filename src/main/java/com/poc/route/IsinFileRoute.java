package com.poc.route;

import com.poc.usecase.AggregationUseCase;
import com.poc.usecase.CsvProcessingUseCase;
import com.poc.usecase.MonitoringUseCase;
import com.poc.utils.RouteUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;

import com.poc.exception.FileIOException;
import com.poc.exception.FileValidationException;
import com.poc.model.generated.ProcessingIncident;
import org.apache.camel.builder.RouteBuilder;
import org.jboss.logging.Logger;

import java.io.IOException;

@ApplicationScoped
public class IsinFileRoute extends RouteBuilder {

    protected static final Logger LOG = Logger.getLogger(IsinFileRoute.class);

    private final CsvProcessingUseCase csvProcessingUseCase;
    private final AggregationUseCase aggregationUseCase;
    private final MonitoringUseCase monitoringUseCase;
    private final RouteUtils routeUtils;

    private static final String ISIN_FILE_ROUTE = "isinFileRoute";

    @Inject
    public IsinFileRoute(CsvProcessingUseCase csvProcessingUseCase, AggregationUseCase aggregationUseCase, MonitoringUseCase monitoringUseCase, RouteUtils routeUtils) {
        this.csvProcessingUseCase = csvProcessingUseCase;
        this.aggregationUseCase = aggregationUseCase;
        this.monitoringUseCase = monitoringUseCase;
        this.routeUtils = routeUtils;
    }

    @Override
    public void configure() {
        from("direct:processIsinFile")
                .id(ISIN_FILE_ROUTE)
                .log(LoggingLevel.INFO, "Processing ISIN file: ${header.CamelFileName}")
                .onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                    String fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
                    String correlationId = exchange.getIn().getHeader("correlationId", String.class);

                    if (correlationId == null) {
                        LOG.warnf("correlationId is null for file %s, using fallback", fileName);
                    }

                    LOG.errorf("Error processing ISIN file %s: %s", fileName, exception.getMessage());

                    // Record error metrics
                    monitoringUseCase.recordFileProcessingMetric("ISIN", false, 0);

                    // Create processing incident
                    ProcessingIncident incident = monitoringUseCase.createIncident(
                            fileName, "ISIN", exception.getMessage(),
                            "Parsing CSV", exception, correlationId, "MOVED_TO_ERROR");

                    if (exception instanceof IOException) {
                        throw new FileIOException("I/O error while processing ISIN file", exception, incident);
                    } else {
                        throw new FileValidationException("Validation error in ISIN file", exception, incident);
                    }
                })
                .to("direct:processError")
                .end()
                .process(exchange -> {
                    String content = exchange.getIn().getBody(String.class);
                    String fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
                    String correlationId = exchange.getIn().getHeader("correlationId", String.class);
                    LOG.debugf("CORRELATION DEBUG - ISIN processing with correlationId: %s", correlationId);

                    long startTime = System.currentTimeMillis();

                    // Process the ISIN CSV file
                    var records = csvProcessingUseCase.processIsinCsv(content, fileName, correlationId);

                    LOG.infof("ISIN file %s contains %d records", fileName, records.size());

                    // Add records for later aggregation
                    aggregationUseCase.addIsinBatch(records, correlationId);

                    // Record processing metrics
                    long processingTime = System.currentTimeMillis() - startTime;
                    monitoringUseCase.recordFileProcessingMetric("ISIN", true, processingTime);

                    // Check if all files have been received and trigger aggregation if complete
                    routeUtils.checkAndTriggerAggregation(correlationId);
                });
    }
}