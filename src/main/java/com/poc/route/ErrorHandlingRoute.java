package com.poc.route;

import com.poc.enums.FileType;
import com.poc.usecase.MonitoringUseCase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.poc.exception.FileProcessingException;

import java.util.UUID;
import java.util.regex.Pattern;

import static org.apache.camel.LoggingLevel.ERROR;

@ApplicationScoped
public class ErrorHandlingRoute extends RouteBuilder {

    private final MonitoringUseCase monitoringUseCase;

    private static final String CORRELATION_ID = "correlationId";
    private static final String ERROR_HANDLER_ROUTE = "errorHandlerRoute";

    @ConfigProperty(name = "app.input.error-directory", defaultValue = "./errors")
    String errorDirectory;

    @ConfigProperty(name = "app.input.file-patterns.external.regexp", defaultValue = "ESH_EXTERNAL.*\\.csv")
    String externalFilePattern;

    @ConfigProperty(name = "app.input.file-patterns.isin.regexp", defaultValue = "ESH_ISIN.*\\.csv")
    String isinFilePattern;

    @ConfigProperty(name = "app.input.file-patterns.internal.regexp", defaultValue = "ESH_INTERNAL.*\\.csv")
    String internalFilePattern;

    @Inject
    public ErrorHandlingRoute(MonitoringUseCase monitoringUseCase) {
        this.monitoringUseCase = monitoringUseCase;
    }

    @Override
    public void configure() {
        from("direct:processError")
                .id(ERROR_HANDLER_ROUTE)
                .log(ERROR, "Error during processing: ${exception.message}")
                .process(exchange -> {
                    var exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                    var fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, "unknown", String.class);
                    var correlationId = exchange.getIn().getHeader(CORRELATION_ID, String.class);

                    if (correlationId == null) {
                        correlationId = UUID.randomUUID().toString();
                    }

                    if (exception instanceof FileProcessingException) {
                        monitoringUseCase.handleException((FileProcessingException) exception, correlationId);
                    } else {
                        var fileType = determineFileType(fileName);

                        var incident = monitoringUseCase.createIncident(
                                fileName, fileType, exception.getMessage(),
                                "Processing error", exception, correlationId, "MOVED_TO_ERROR");

                        monitoringUseCase.registerIncident(incident);
                    }
                })
                .to("file:" + errorDirectory + "?fileName=${header.CamelFileName}");
    }

    /**
     * Determines the file type based on the file name pattern
     *
     * @param fileName The name of the file to check
     * @return The file type as String ("EXTERNAL", "ISIN", "INTERNAL" or "UNKNOWN")
     */
    private String determineFileType(String fileName) {
        if (Pattern.matches(externalFilePattern, fileName)) {
            return FileType.EXTERNAL.name();
        } else if (Pattern.matches(isinFilePattern, fileName)) {
            return FileType.ISIN.name();
        } else if (Pattern.matches(internalFilePattern, fileName)) {
            return FileType.INTERNAL.name();
        } else {
            return FileType.UNKNOWN.name();
        }
    }
}