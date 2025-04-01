package com.poc.route;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class FileWatcherRoute extends RouteBuilder {

    private static final String FILE_WATCHER_ROUTE = "fileWatcherRoute";
    // Using a consistent prefix - no "ESH_" to match what other routes are using
    private static final String BATCH_PREFIX = "BATCH_";
    private static final String DATE_PATTERN = "\\d{8}"; // Pattern for YYYYMMDD
    private static final String CORRELATION_ID_HEADER = "correlationId";

    protected static final Logger LOG = Logger.getLogger(FileWatcherRoute.class);

    @ConfigProperty(name = "app.input.directory", defaultValue = "./input")
    String inputDirectory;

    @ConfigProperty(name = "app.input.processed-directory", defaultValue = "./processed")
    String processedDirectory;

    @ConfigProperty(name = "app.input.error-directory", defaultValue = "./errors")
    String errorDirectory;

    @ConfigProperty(name = "app.input.file-patterns.external.regexp", defaultValue = "ESH_EXTERNAL.*\\.csv")
    String externalFilePattern;

    @ConfigProperty(name = "app.input.file-patterns.isin.regexp", defaultValue = "ESH_ISIN.*\\.csv")
    String isinFilePattern;

    @ConfigProperty(name = "app.input.file-patterns.internal.regexp", defaultValue = "ESH_INTERNAL.*\\.csv")
    String internalFilePattern;

    @Override
    public void configure() {
        from("file:" + inputDirectory + "?idempotent=true" + "&noop=false&moveFailed=" + errorDirectory +
                "&move=" + processedDirectory + "&include=.*\\.csv&readLock=changed&readLockMinAge=1000&initialDelay=5000")
                .id(FILE_WATCHER_ROUTE)
                .log(LoggingLevel.INFO, "File detected: ${header.CamelFileName}")
                .process(this::setupCorrelationId)
                .choice()
                .when(header(Exchange.FILE_NAME).regex(externalFilePattern))
                .log(LoggingLevel.DEBUG, "Routing external file with correlationId=${header.correlationId}")
                .to("direct:processExternalFile")
                .when(header(Exchange.FILE_NAME).regex(isinFilePattern))
                .log(LoggingLevel.DEBUG, "Routing ISIN file with correlationId=${header.correlationId}")
                .to("direct:processIsinFile")
                .when(header(Exchange.FILE_NAME).regex(internalFilePattern))
                .log(LoggingLevel.DEBUG, "Routing internal file with correlationId=${header.correlationId}")
                .to("direct:processInternalFile")
                .otherwise()
                .log(LoggingLevel.WARN, "Unrecognized file: ${header.CamelFileName}")
                .to("file:" + errorDirectory)
                .end();
    }

    /**
     * Sets up a correlation ID based on the file name or current date
     * @param exchange The Camel exchange containing the file information
     */
    private void setupCorrelationId(Exchange exchange) {
        String fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);

        // Extract the common part from file names (date)
        Pattern pattern = Pattern.compile(DATE_PATTERN);
        Matcher matcher = pattern.matcher(fileName);

        String correlationId;
        if (matcher.find()) {
            // Use date from filename if found
            correlationId = BATCH_PREFIX + matcher.group();
            LOG.infof("Set correlationId based on file date: %s for file %s", correlationId, fileName);
        } else {
            // Fallback - use current date
            correlationId = BATCH_PREFIX + new SimpleDateFormat("yyyyMMdd").format(new Date());
            LOG.infof("Set fallback correlationId: %s for file %s", correlationId, fileName);
        }

        exchange.getIn().setHeader(CORRELATION_ID_HEADER, correlationId);
    }
}