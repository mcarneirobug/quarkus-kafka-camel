//package com.poc.route;
//
//import com.poc.exception.BusinessLogicException;
//import com.poc.exception.FileIOException;
//import com.poc.exception.FileProcessingException;
//import com.poc.exception.FileValidationException;
//import com.poc.model.ExternalCsvRecord;
//import com.poc.model.InternalCsvRecord;
//import com.poc.model.IsinCsvRecord;
//import com.poc.model.generated.ProcessingIncident;
//import com.poc.usecase.AggregationUseCase;
//import com.poc.usecase.CsvProcessingUseCase;
//import com.poc.usecase.KafkaEmitterUseCase;
//import com.poc.usecase.MonitoringUseCase;
//import jakarta.enterprise.context.ApplicationScoped;
//import jakarta.inject.Inject;
//import org.apache.camel.Exchange;
//import org.apache.camel.LoggingLevel;
//import org.apache.camel.builder.RouteBuilder;
//import org.apache.camel.component.kafka.KafkaConstants;
//import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
//import org.apache.camel.model.dataformat.JsonLibrary;
//import org.eclipse.microprofile.config.inject.ConfigProperty;
//import org.jboss.logging.Logger;
//
//import java.io.IOException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.List;
//import java.util.UUID;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
///**
// * Camel RouteBuilder for processing CSV files and sending data to Kafka.
// * <p>
// * This route is responsible for:
// * • Monitoring a directory for CSV files.
// * • Routing the files based on file patterns.
// * • Processing (unmarshalling/validating) CSV content.
// * • Aggregating events before sending them to Kafka.
// * </p>
// */
//@ApplicationScoped
//public class FileProcessingRoute extends RouteBuilder {
//
//    private static final Logger LOG = Logger.getLogger(FileProcessingRoute.class);
//
//    private final AggregationUseCase aggregationUseCase;
//    private final MonitoringUseCase monitoringUseCase;
//    private final KafkaEmitterUseCase kafkaEmitterUseCase;
//    private final CsvProcessingUseCase csvProcessingUseCase;
//
//    @ConfigProperty(name = "app.input.directory")
//    String inputDirectory;
//
//    @ConfigProperty(name = "app.input.processed-directory")
//    String processedDirectory;
//
//    @ConfigProperty(name = "app.input.error-directory")
//    String errorDirectory;
//
//    @ConfigProperty(name = "app.input.file-patterns.external.regexp")
//    String externalFilePattern;
//
//    @ConfigProperty(name = "app.input.file-patterns.isin.regexp")
//    String isinFilePattern;
//
//    @ConfigProperty(name = "app.input.file-patterns.internal.regexp")
//    String internalFilePattern;
//
//    @ConfigProperty(name = "app.output.kafka.topic")
//    String kafkaTopic;
//
//    @ConfigProperty(name = "app.output.kafka.batch-size")
//    int batchSize;
//
//    @ConfigProperty(name = "app.kafka-mode", defaultValue = "camel")
//    String kafkaMode;
//
//    @Inject
//    public FileProcessingRoute(AggregationUseCase aggregationUseCase, MonitoringUseCase monitoringUseCase, KafkaEmitterUseCase kafkaEmitterUseCase, CsvProcessingUseCase csvProcessingUseCase) {
//        this.aggregationUseCase = aggregationUseCase;
//        this.monitoringUseCase = monitoringUseCase;
//        this.kafkaEmitterUseCase = kafkaEmitterUseCase;
//        this.csvProcessingUseCase = csvProcessingUseCase;
//    }
//
//    @Override
//    public void configure() {
//        // Define CSV formats for each record
//        BindyCsvDataFormat externalCsvFormat = new BindyCsvDataFormat(ExternalCsvRecord.class);
//        BindyCsvDataFormat isinCsvFormat = new BindyCsvDataFormat(IsinCsvRecord.class);
//        BindyCsvDataFormat internalCsvFormat = new BindyCsvDataFormat(InternalCsvRecord.class);
//
//        // Tratamento de erros global
//        errorHandler(deadLetterChannel("direct:processError")
//                .useOriginalMessage()
//                .maximumRedeliveries(3)
//                .redeliveryDelay(1000)
//                .backOffMultiplier(2)
//                .useExponentialBackOff()
//                .retryAttemptedLogLevel(LoggingLevel.WARN));
//
//        // Rota para tratamento de erros
//        from("direct:processError")
//                .id("errorHandlerRoute")
//                .log(LoggingLevel.ERROR, "Erro no processamento: ${exception.message}")
//                .process(exchange -> {
//                    Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
//                    String fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, "unknown", String.class);
//                    String correlationId = exchange.getIn().getHeader("correlationId", String.class);
//
//                    if (correlationId == null) {
//                        correlationId = UUID.randomUUID().toString();
//                    }
//
//                    if (exception instanceof FileProcessingException) {
//                        monitoringUseCase.handleException((FileProcessingException) exception, correlationId);
//                    } else {
//                        // Determina o tipo de arquivo
//                        String fileType = determineFileType(fileName);
//
//                        // Cria um incidente para o erro
//                        ProcessingIncident incident = monitoringUseCase.createIncident(
//                                fileName, fileType, exception.getMessage(),
//                                "Erro de processamento", exception, correlationId, "MOVED_TO_ERROR");
//
//                        monitoringUseCase.registerIncident(incident);
//                    }
//                })
//                .to("file:" + errorDirectory + "?fileName=${header.CamelFileName}");
//
//        // Rota para monitoramento de diretório e detecção de arquivos
//        from("file:" + inputDirectory + "?noop=false&moveFailed=" + errorDirectory +
//                "&move=" + processedDirectory + "&include=.*\\.csv&readLock=changed&readLockMinAge=1000&initialDelay=5000")
//                .id("fileWatcherRoute")
//                .log(LoggingLevel.INFO, "Arquivo detectado: ${header.CamelFileName}")
//                .choice()
//                .when(header(Exchange.FILE_NAME).regex(externalFilePattern))
//                .to("direct:processExternalFile")
//                .when(header(Exchange.FILE_NAME).regex(isinFilePattern))
//                .to("direct:processIsinFile")
//                .when(header(Exchange.FILE_NAME).regex(internalFilePattern))
//                .to("direct:processInternalFile")
//                .otherwise()
//                .log(LoggingLevel.WARN, "Arquivo não reconhecido: ${header.CamelFileName}")
//                .to("file:" + errorDirectory)
//                .end();
//
//        // Rota para processar arquivos externos
//        from("direct:processExternalFile")
//                .id("externalFileRoute")
//                .log(LoggingLevel.INFO, "Processando arquivo externo: ${header.CamelFileName}")
//                .process(this::setupCorrelationId)
//                .doTry()
//                .process(exchange -> {
//                    String content = exchange.getIn().getBody(String.class);
//                    String fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
//                    String correlationId = exchange.getIn().getHeader("correlationId", String.class);
//
//                    long startTime = System.currentTimeMillis();
//
//                    // Usar o CsvProcessingUseCase em vez do Bindy
//                    List<ExternalCsvRecord> records = csvProcessingUseCase.processExternalCsv(
//                            content, fileName, correlationId);
//
//                    exchange.getIn().setBody(records);
//
//                    LOG.infof("Arquivo externo %s contém %d registros", fileName, records.size());
//
//                    // Adiciona os registros para agregação posterior
//                    aggregationUseCase.addExternalBatch(records, correlationId);
//
//                    // Registra métricas de processamento
//                    long processingTime = System.currentTimeMillis() - startTime;
//                    monitoringUseCase.recordFileProcessingMetric("EXTERNAL", true, processingTime);
//
//                    // Verifica se todos os arquivos foram recebidos
//                    checkAndTriggerAggregation(exchange, correlationId);
//                })
//                .doCatch(IOException.class, Exception.class)
//                .process(exchange -> {
//                    Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
//                    String fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
//                    String correlationId = exchange.getIn().getHeader("correlationId", String.class);
//
//                    LOG.errorf("Erro ao processar arquivo externo %s: %s", fileName, exception.getMessage());
//
//                    // Registra métricas de erro
//                    monitoringUseCase.recordFileProcessingMetric("EXTERNAL", false, 0);
//
//                    // Criar incidente - os valores de string devem corresponder aos enums
//                    ProcessingIncident incident = monitoringUseCase.createIncident(
//                            fileName, "EXTERNAL", exception.getMessage(),
//                            "Parsing CSV", exception, correlationId, "MOVED_TO_ERROR");
//
//                    if (exception instanceof IOException) {
//                        throw new FileIOException("Erro de I/O ao processar arquivo externo", exception, incident);
//                    } else {
//                        throw new FileValidationException("Erro de validação no arquivo externo", exception, incident);
//                    }
//                })
//                .end();
//
//        // Rota para processar arquivos ISIN
//        from("direct:processIsinFile")
//                .id("isinFileRoute")
//                .log(LoggingLevel.INFO, "Processando arquivo ISIN: ${header.CamelFileName}")
//                .process(this::setupCorrelationId)
//                .doTry()
//                .process(exchange -> {
//                    String content = exchange.getIn().getBody(String.class);
//                    String fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
//                    String correlationId = exchange.getIn().getHeader("correlationId", String.class);
//
//                    long startTime = System.currentTimeMillis();
//
//                    // Usar o CsvProcessingUseCase para processar o arquivo ISIN
//                    List<IsinCsvRecord> records = csvProcessingUseCase.processIsinCsv(
//                            content, fileName, correlationId);
//
//                    exchange.getIn().setBody(records);
//
//                    LOG.infof("Arquivo ISIN %s contém %d registros", fileName, records.size());
//
//                    // Adiciona os registros para agregação posterior
//                    aggregationUseCase.addIsinBatch(records, correlationId);
//
//                    // Registra métricas de processamento
//                    long processingTime = System.currentTimeMillis() - startTime;
//                    monitoringUseCase.recordFileProcessingMetric("ISIN", true, processingTime);
//
//                    // Verifica se todos os arquivos foram recebidos
//                    checkAndTriggerAggregation(exchange, correlationId);
//                })
//                .doCatch(IOException.class, Exception.class)
//                .process(exchange -> {
//                    Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
//                    String fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
//                    String correlationId = exchange.getIn().getHeader("correlationId", String.class);
//
//                    LOG.errorf("Erro ao processar arquivo ISIN %s: %s", fileName, exception.getMessage());
//
//                    // Registra métricas de erro
//                    monitoringUseCase.recordFileProcessingMetric("ISIN", false, 0);
//
//                    // Criar incidente
//                    ProcessingIncident incident = monitoringUseCase.createIncident(
//                            fileName, "ISIN", exception.getMessage(),
//                            "Parsing CSV", exception, correlationId, "MOVED_TO_ERROR");
//
//                    if (exception instanceof IOException) {
//                        throw new FileIOException("Erro de I/O ao processar arquivo ISIN", exception, incident);
//                    } else {
//                        throw new FileValidationException("Erro de validação no arquivo ISIN", exception, incident);
//                    }
//                })
//                .end();
//
//        // Rota para processar arquivos internos
//        from("direct:processInternalFile")
//                .id("internalFileRoute")
//                .log(LoggingLevel.INFO, "Processando arquivo interno: ${header.CamelFileName}")
//                .process(this::setupCorrelationId)
//                .doTry()
//                .process(exchange -> {
//                    String content = exchange.getIn().getBody(String.class);
//                    String fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
//                    String correlationId = exchange.getIn().getHeader("correlationId", String.class);
//
//                    long startTime = System.currentTimeMillis();
//
//                    // Usar o CsvProcessingUseCase para processar o arquivo interno
//                    List<InternalCsvRecord> records = csvProcessingUseCase.processInternalCsv(
//                            content, fileName, correlationId);
//
//                    exchange.getIn().setBody(records);
//
//                    LOG.infof("Arquivo interno %s contém %d registros", fileName, records.size());
//
//                    // Adiciona os registros para agregação posterior
//                    aggregationUseCase.addInternalBatch(records, correlationId);
//
//                    // Registra métricas de processamento
//                    long processingTime = System.currentTimeMillis() - startTime;
//                    monitoringUseCase.recordFileProcessingMetric("INTERNAL", true, processingTime);
//
//                    // Verifica se todos os arquivos foram recebidos
//                    checkAndTriggerAggregation(exchange, correlationId);
//                })
//                .doCatch(IOException.class)
//                .process(exchange -> {
//                    Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
//                    String fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
//                    String correlationId = exchange.getIn().getHeader("correlationId", String.class);
//
//                    LOG.errorf("Erro de IO ao processar arquivo interno %s: %s", fileName, exception.getMessage());
//
//                    // Registra métricas de erro
//                    monitoringUseCase.recordFileProcessingMetric("INTERNAL", false, 0);
//
//                    // Criar incidente
//                    ProcessingIncident incident = monitoringUseCase.createIncident(
//                            fileName, "INTERNAL", exception.getMessage(),
//                            "Parsing CSV - IO Error", exception, correlationId, "MOVED_TO_ERROR");
//
//                    throw new FileIOException("Erro de I/O ao processar arquivo interno", exception, incident);
//                })
//                .doCatch(Exception.class)
//                .process(exchange -> {
//                    Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
//                    String fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
//                    String correlationId = exchange.getIn().getHeader("correlationId", String.class);
//
//                    LOG.errorf("Erro ao processar arquivo interno %s: %s", fileName, exception.getMessage());
//
//                    // Registra métricas de erro
//                    monitoringUseCase.recordFileProcessingMetric("INTERNAL", false, 0);
//
//                    // Criar incidente
//                    ProcessingIncident incident = monitoringUseCase.createIncident(
//                            fileName, "INTERNAL", exception.getMessage(),
//                            "Parsing CSV - Validation Error", exception, correlationId, "MOVED_TO_ERROR");
//
//                    throw new FileValidationException("Erro de validação no arquivo interno", exception, incident);
//                })
//                .end();
//
//        // Rota para agregar e enviar para o Kafka
//        from("direct:aggregateAndSend")
//                .id("aggregationRoute")
//                .log(LoggingLevel.INFO, "Iniciando agregação para o lote: ${header.correlationId}")
//                .onException(Exception.class)
//                .process(exchange -> {
//                    Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
//                    String correlationId = exchange.getIn().getHeader("correlationId", String.class);
//
//                    LOG.errorf("Erro durante a agregação e envio para o Kafka: %s", exception.getMessage());
//
//                    // Criar incidente
//                    ProcessingIncident incident = monitoringUseCase.createIncident(
//                            "N/A", "AGGREGATION", exception.getMessage(),
//                            "AggregationRoute", exception, correlationId, "MANUAL_INTERVENTION_REQUIRED");
//
//                    throw new BusinessLogicException("Erro ao agregar e enviar eventos", exception, incident);
//                })
//                .handled(true)
//                .end()
//                .process(exchange -> {
//                    String correlationId = exchange.getIn().getHeader("correlationId", String.class);
//
//                    // Agrega eventos correlacionados
//                    var aggregatedEvents = aggregationUseCase.aggregateEvents(correlationId);
//
//                    // Converte para JSON
//                    List<String> jsonEvents = aggregationUseCase.serializeEvents(aggregatedEvents);
//
//                    exchange.getIn().setBody(jsonEvents);
//                    exchange.getIn().setHeader("eventCount", jsonEvents.size());
//
//                    LOG.infof("Eventos agregados: %d para o lote %s", jsonEvents.size(), correlationId);
//                })
//                .process(exchange -> {
//                    List<String> events = exchange.getIn().getBody(List.class);
//                    LOG.infof("Preparando para enviar %d eventos para o Kafka", events.size());
//
//                    // Prepara para envio ao Kafka
//                    exchange.getIn().setHeader(KafkaConstants.KEY, UUID.randomUUID().toString());
//                })
//                .choice()
//                .when(simple("${properties:app.kafka-mode} == 'camel'"))
//                .to("direct:sendToCamelKafka")
//                .otherwise()
//                .to("direct:sendToEmitter")
//                .end();
//
//// Rota para enviar eventos para o Kafka usando componente Camel Kafka
//        from("direct:sendToCamelKafka")
//                .id("camelKafkaRoute")
//                .log(LoggingLevel.INFO, "Enviando eventos para o Kafka usando componente Camel: ${header.eventCount} eventos")
//                .marshal().json(JsonLibrary.Jackson)
//                .to("kafka:" + kafkaTopic + "?brokers={{camel.component.kafka.brokers}}")
//                .log(LoggingLevel.INFO, "Eventos enviados com sucesso para o tópico ${header.CamelKafkaTopic}");
//
//// Rota para enviar eventos para o Kafka usando SmallRye Reactive Messaging Emitter
//        from("direct:sendToEmitter")
//                .id("emitterKafkaRoute")
//                .log(LoggingLevel.INFO, "Enviando eventos para o Kafka usando Emitter: ${header.eventCount} eventos")
//                .process(exchange -> {
//                    @SuppressWarnings("unchecked")
//                    List<String> events = exchange.getIn().getBody(List.class);
//                    String correlationId = exchange.getIn().getHeader("correlationId", String.class);
//
//                    // Isso presumivelmente chama seu KafkaEmitterUseCase
//                    kafkaEmitterUseCase.sendEvents(events, correlationId)
//                            .toCompletableFuture()
//                            .join(); // Bloqueia até que o envio seja concluído
//                })
//                .log(LoggingLevel.INFO, "Eventos enviados com sucesso para o tópico " + kafkaTopic);
//    }
//
//    private void validateInternalRecords(List<InternalCsvRecord> records, String fileName, String correlationId) {
//        // Exemplo de validação: verificar se há registros vazios ou inválidos
//        for (int i = 0; i < records.size(); i++) {
//            InternalCsvRecord record = records.get(i);
//
//            if (record.getInternalId() == null || record.getInternalId().trim().isEmpty()) {
//                ProcessingIncident incident = monitoringUseCase.createIncident(
//                        fileName, "INTERNAL",
//                        "Registro com ID interno vazio na linha " + (i + 2), // +2 porque índice 0 + cabeçalho
//                        "Linha " + (i + 2), null, correlationId, "MOVED_TO_ERROR");
//
//                throw new FileValidationException("Dados inválidos no arquivo interno: internalId vazio", incident);
//            }
//
//            if (record.getInternalCode() == null || record.getInternalCode().trim().isEmpty()) {
//                ProcessingIncident incident = monitoringUseCase.createIncident(
//                        fileName, "INTERNAL",
//                        "Registro com código interno vazio na linha " + (i + 2),
//                        "Linha " + (i + 2), null, correlationId, "MOVED_TO_ERROR");
//
//                throw new FileValidationException("Dados inválidos no arquivo interno: internalCode vazio", incident);
//            }
//
//            // Verificar valor negativo
//            if (record.getInternalAmount() != null && record.getInternalAmount().doubleValue() < 0) {
//                ProcessingIncident incident = monitoringUseCase.createIncident(
//                        fileName, "INTERNAL",
//                        "Valor interno negativo na linha " + (i + 2) + ": " + record.getInternalAmount(),
//                        "Linha " + (i + 2), null, correlationId, "MOVED_TO_ERROR");
//
//                throw new FileValidationException("Dados inválidos no arquivo interno: valor negativo", incident);
//            }
//
//            // Verificar se a chave de correlação está preenchida
//            if (record.getCorrelationKey() == null || record.getCorrelationKey().trim().isEmpty()) {
//                ProcessingIncident incident = monitoringUseCase.createIncident(
//                        fileName, "INTERNAL",
//                        "Registro sem chave de correlação na linha " + (i + 2),
//                        "Linha " + (i + 2), null, correlationId, "MOVED_TO_ERROR");
//
//                throw new FileValidationException("Dados inválidos no arquivo interno: correlationKey vazia", incident);
//            }
//        }
//    }
//
//    /**
//     * Configura o ID de correlação para o lote
//     */
//    private void setupCorrelationId(Exchange exchange) {
//        String fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
//
//        // Extrair a data do nome do arquivo (assumindo que a data está após o último _ e antes do .csv)
//        String datePattern = "_(\\d{8})";
//        Pattern pattern = Pattern.compile(datePattern);
//        Matcher matcher = pattern.matcher(fileName);
//
//        if (matcher.find()) {
//            String dateStr = matcher.group(1); // Extrai 20250330 do nome do arquivo
//            String correlationId = "ESH_BATCH_" + dateStr;
//            exchange.getIn().setHeader("correlationId", correlationId);
//            LOG.infof("Configurado correlationId: %s para o arquivo %s", correlationId, fileName);
//        } else {
//            // Fallback - usa o nome do arquivo sem extensão
//            String correlationId = "BATCH_" + new SimpleDateFormat("yyyyMMdd").format(new Date());
//            exchange.getIn().setHeader("correlationId", correlationId);
//            LOG.infof("Configurado correlationId fallback: %s para o arquivo %s", correlationId, fileName);
//        }
//    }
//
//    /**
//     * Verifica se todos os arquivos foram recebidos e dispara a agregação
//     */
//    private void checkAndTriggerAggregation(Exchange exchange, String correlationId) {
//        if (aggregationUseCase.isReadyForAggregation(correlationId)) {
//            LOG.infof("Todos os arquivos recebidos para o lote %s, iniciando agregação", correlationId);
//
//            // Envia para a rota de agregação
//            exchange.getContext().createProducerTemplate()
//                    .send("direct:aggregateAndSend", exc -> exc.getIn().setHeader("correlationId", correlationId));
//        } else {
//            LOG.infof("Aguardando outros arquivos para o lote %s", correlationId);
//        }
//    }
//
//    /**
//     * Determina o tipo de arquivo com base no nome
//     */
//    private String determineFileType(String fileName) {
//        if (Pattern.matches(externalFilePattern, fileName)) {
//            return "EXTERNAL";
//        } else if (Pattern.matches(isinFilePattern, fileName)) {
//            return "ISIN";
//        } else if (Pattern.matches(internalFilePattern, fileName)) {
//            return "INTERNAL";
//        } else {
//            return "UNKNOWN";
//        }
//    }
//}