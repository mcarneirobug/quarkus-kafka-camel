package com.poc.route;

import com.poc.usecase.AggregationUseCase;
import com.poc.usecase.CsvProcessingUseCase;
import com.poc.usecase.MonitoringUseCase;

import com.poc.utils.RouteUtils;
import jakarta.enterprise.context.ApplicationScoped;

import jakarta.inject.Inject;

import org.apache.camel.Exchange;

import com.poc.exception.FileIOException;
import com.poc.exception.FileValidationException;

import com.poc.model.generated.ProcessingIncident;

import org.apache.camel.builder.RouteBuilder;
import org.jboss.logging.Logger;

import java.io.IOException;

import static org.apache.camel.LoggingLevel.INFO;

@ApplicationScoped
public class InternalFileRoute extends RouteBuilder {

    protected static final Logger LOG = Logger.getLogger(InternalFileRoute.class);

    private final CsvProcessingUseCase csvProcessingUseCase;
    private final MonitoringUseCase monitoringUseCase;
    private final RouteUtils routeUtils;

    @Inject
    public InternalFileRoute(CsvProcessingUseCase csvProcessingUseCase, MonitoringUseCase monitoringUseCase, RouteUtils routeUtils) {
        this.csvProcessingUseCase = csvProcessingUseCase;
        this.monitoringUseCase = monitoringUseCase;
        this.routeUtils = routeUtils;
    }

    @Override
    public void configure() {
        from("direct:processInternalFile")
                .id("internalFileRoute")
                .log(INFO, "Processing internal file: ${header.CamelFileName}")
                .onException(Exception.class)
                    .handled(true)
                    .process(exchange -> {
                        var exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                        var fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
                        var correlationId = exchange.getIn().getHeader("correlationId", String.class);
                        var fileSize = exchange.getIn().getHeader(Exchange.FILE_LENGTH, 0L, Long.class);

                        LOG.errorf("Error processing internal file %s: %s", fileName, exception.getMessage());

                        // Registra métricas de erro
                        monitoringUseCase.logIncident(fileName, "INTERNAL", exception.getMessage(), "Processing internal file", exception, correlationId);
                        monitoringUseCase.recordFileProcessingMetric("INTERNAL", false, 0, fileName, fileSize);

                        // Criar incidente
                        ProcessingIncident incident = monitoringUseCase.createIncident(
                                fileName, "INTERNAL", exception.getMessage(),
                                "Parsing CSV", exception, correlationId, "MOVED_TO_ERROR");

                        if (exception instanceof IOException) {
                            throw new FileIOException("I/O error while processing internal file", exception, incident);
                        } else {
                            throw new FileValidationException("Validation error in internal file", exception, incident);
                        }
                    })
                    .to("direct:processError")
                .end()
                .process(exchange -> {
                    var content = exchange.getIn().getBody(String.class);
                    var fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
                    var correlationId = exchange.getIn().getHeader("correlationId", String.class);
                    var fileSize = exchange.getIn().getHeader(Exchange.FILE_LENGTH, 0L, Long.class);

                    LOG.debugf("CORRELATION DEBUG - Internal processing with correlationId: %s", correlationId);

                    long startTime = System.currentTimeMillis();

                    // Process the CSV file using CsvProcessingUseCase
                    var records = csvProcessingUseCase.processInternalCsv(content, fileName, correlationId);

                    LOG.infof("Internal file %s contains %d records", fileName, records.size());

                    // Add records for later aggregation
//                    aggregationUseCase.addInternalBatch(records, correlationId);

                    // Record processing metrics
                    long processingTime = System.currentTimeMillis() - startTime;
                    monitoringUseCase.recordFileProcessingMetric("INTERNAL", true, processingTime, fileName, fileSize);

                    // Check if all files have been received and trigger aggregation if complete
                    routeUtils.checkAndTriggerAggregation(correlationId);
                });
    }
}