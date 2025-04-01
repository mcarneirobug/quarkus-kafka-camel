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
public class ExternalFileRoute extends RouteBuilder {

    protected static final Logger LOG = Logger.getLogger(ExternalFileRoute.class);

    private final CsvProcessingUseCase csvProcessingUseCase;
    private final AggregationUseCase aggregationUseCase;
    private final MonitoringUseCase monitoringUseCase;
    private final RouteUtils routeUtils;

    @Inject
    public ExternalFileRoute(CsvProcessingUseCase csvProcessingUseCase, AggregationUseCase aggregationUseCase, MonitoringUseCase monitoringUseCase, RouteUtils routeUtils) {
        this.csvProcessingUseCase = csvProcessingUseCase;
        this.aggregationUseCase = aggregationUseCase;
        this.monitoringUseCase = monitoringUseCase;
        this.routeUtils = routeUtils;
    }

    @Override
    public void configure() throws Exception {
        from("direct:processExternalFile")
                .id("externalFileRoute")
                .log(INFO, "Processing external file: ${header.CamelFileName}")
                .onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                    String fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
                    String correlationId = exchange.getIn().getHeader("correlationId", String.class);

                    LOG.errorf("Error processing external file %s: %s", fileName, exception.getMessage());

                    // Registra métricas de erro
                    monitoringUseCase.recordFileProcessingMetric("EXTERNAL", false, 0);

                    // Criar incidente
                    ProcessingIncident incident = monitoringUseCase.createIncident(
                            fileName, "EXTERNAL", exception.getMessage(),
                            "Parsing CSV", exception, correlationId, "MOVED_TO_ERROR");

                    if (exception instanceof IOException) {
                        throw new FileIOException("I/O error while processing external file", exception, incident);
                    } else {
                        throw new FileValidationException("Validation error in external file", exception, incident);
                    }
                })
                .to("direct:processError")
                .end()
                .process(exchange -> {
                    String content = exchange.getIn().getBody(String.class);
                    String fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
                    String correlationId = exchange.getIn().getHeader("correlationId", String.class);
                    LOG.debugf("CORRELATION DEBUG - External processing with correlationId: %s", correlationId);

                    long startTime = System.currentTimeMillis();

                    // Process the external CSV file
                    var records = csvProcessingUseCase.processExternalCsv(content, fileName, correlationId);

                    LOG.infof("External file %s contains %d records", fileName, records.size());

                    // Add records for later aggregation
                    aggregationUseCase.addExternalBatch(records, correlationId);

                    // Record processing metrics
                    long processingTime = System.currentTimeMillis() - startTime;
                    monitoringUseCase.recordFileProcessingMetric("EXTERNAL", true, processingTime);

                    // Check if all files have been received and trigger aggregation if complete
                    routeUtils.checkAndTriggerAggregation(correlationId);
                });
    }
}