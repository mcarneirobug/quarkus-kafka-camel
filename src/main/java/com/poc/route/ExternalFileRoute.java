package com.poc.route;

import com.poc.enums.FileType;
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

    private static final String EXTERNAL_FILE_ROUTE = "externalFileRoute";
    private static final String CORRELATION_ID = "correlationId";
    private static final String EXTERNAL_FILE_CONTAIN_RECORD = "External file %s contains %d records";
    private static final String CORRELATION_DEBUG = "CORRELATION DEBUG - External processing with correlationId: %s";
    private static final String ERROR_PROCESSING_EXTERNAL_FILE = "Error processing external file %s: %s";

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
                .id(EXTERNAL_FILE_ROUTE)
                .log(INFO, "Processing external file: ${header.CamelFileName}")
                .onException(Exception.class)
                    .handled(true)
                    .process(exchange -> {
                        var exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                        var fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
                        var correlationId = exchange.getIn().getHeader(CORRELATION_ID, String.class);
                        var fileSize = exchange.getIn().getHeader(Exchange.FILE_LENGTH, 0L, Long.class);

                        LOG.errorf(ERROR_PROCESSING_EXTERNAL_FILE, fileName, exception.getMessage());

                        // Log the incident with file details
                        monitoringUseCase.logIncident(fileName, FileType.EXTERNAL.name(), exception.getMessage(), "Processing external file", exception, correlationId);
                        monitoringUseCase.recordFileProcessingMetric(FileType.EXTERNAL.name(), false, 0, fileName, fileSize);

                        // TODO see this after
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
                    var content = exchange.getIn().getBody(String.class);
                    var fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
                    var correlationId = exchange.getIn().getHeader(CORRELATION_ID, String.class);
                    var fileSize = exchange.getIn().getHeader(Exchange.FILE_LENGTH, 0L, Long.class);

                    LOG.debugf(CORRELATION_DEBUG, correlationId);

                    long startTime = System.currentTimeMillis();

                    // Process the external CSV file & save to DB
                    var records = csvProcessingUseCase.processExternalCsv(content, fileName, correlationId);

                    LOG.infof(EXTERNAL_FILE_CONTAIN_RECORD, fileName, records.size());

                    // Add records for later aggregation
//                    aggregationUseCase.addExternalBatch(records, correlationId);

                    // Record processing metrics
                    long processingTime = System.currentTimeMillis() - startTime;
                    monitoringUseCase.recordFileProcessingMetric(FileType.EXTERNAL.name(), true, processingTime, fileName, fileSize);

                    // Check if all files have been received and trigger aggregation if complete
                    routeUtils.checkAndTriggerAggregation(correlationId);
                });
    }
}