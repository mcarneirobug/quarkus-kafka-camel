package com.poc.usecase;

import com.poc.exception.FileValidationException;
import com.poc.model.ExternalCsvRecord;
import com.poc.model.InternalCsvRecord;
import com.poc.model.IsinCsvRecord;
import com.poc.model.generated.ProcessingIncident;
import com.poc.utils.CsvUtils;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class CsvProcessingUseCase {

    private static final Logger LOG = Logger.getLogger(CsvProcessingUseCase.class);

    private final MonitoringUseCase monitoringUseCase;

    @Inject
    public CsvProcessingUseCase(MonitoringUseCase monitoringUseCase) {
        this.monitoringUseCase = monitoringUseCase;
    }

    /**
     * Processa um arquivo External CSV
     */
    public List<ExternalCsvRecord> processExternalCsv(String content, String fileName, String correlationId) {
        LOG.infof("Processando conteúdo de arquivo externo: %s", fileName);
        List<String> lines = CsvUtils.splitCsvLines(content);
        if (lines.isEmpty()) {
            throw new FileValidationException("Arquivo CSV vazio ou inválido",
                    createIncident(fileName, "EXTERNAL", "Arquivo CSV vazio", "Parsing", correlationId));
        }

        // Pula a linha de cabeçalho
        List<ExternalCsvRecord> records = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] fields = CsvUtils.splitCsvFields(line, ";");

            if (fields.length < 4) {
                throw new FileValidationException(
                        "Linha " + i + " não tem todos os campos necessários",
                        createIncident(fileName, "EXTERNAL",
                                "Linha " + i + " tem apenas " + fields.length + " campos",
                                "Parsing linha " + i, correlationId));
            }

            ExternalCsvRecord record = new ExternalCsvRecord();
            record.setExternalId(CsvUtils.getStringField(fields, 0, null));
            record.setExternalName(CsvUtils.getStringField(fields, 1, null));
            record.setExternalValue(CsvUtils.getBigDecimalField(fields, 2, null));
            record.setCorrelationKey(CsvUtils.getStringField(fields, 3, null));

            // Validação
            validateExternalRecord(record, i, fileName, correlationId);

            records.add(record);
            LOG.debugf("Processado registro externo: %s", record);
        }

        LOG.infof("Processados %d registros externos do arquivo %s", records.size(), fileName);
        return records;
    }

    public List<IsinCsvRecord> processIsinCsv(String content, String fileName, String correlationId) {
        LOG.infof("Processando conteúdo de arquivo ISIN: %s", fileName);
        List<String> lines = CsvUtils.splitCsvLines(content);
        if (lines.isEmpty()) {
            throw new FileValidationException("Arquivo CSV vazio ou inválido",
                    createIncident(fileName, "ISIN", "Arquivo CSV vazio", "Parsing", correlationId));
        }

        // Pula a linha de cabeçalho
        List<IsinCsvRecord> records = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] fields = CsvUtils.splitCsvFields(line, ";");

            if (fields.length < 4) {
                throw new FileValidationException(
                        "Linha " + i + " não tem todos os campos necessários",
                        createIncident(fileName, "ISIN",
                                "Linha " + i + " tem apenas " + fields.length + " campos",
                                "Parsing linha " + i, correlationId));
            }

            IsinCsvRecord record = new IsinCsvRecord();
            record.setIsin(CsvUtils.getStringField(fields, 0, null));
            record.setIsinDescription(CsvUtils.getStringField(fields, 1, null));
            record.setIsinCategory(CsvUtils.getStringField(fields, 2, null));
            record.setCorrelationKey(CsvUtils.getStringField(fields, 3, null));

            // Validação
            validateIsinRecord(record, i, fileName, correlationId);

            records.add(record);
            LOG.debugf("Processado registro ISIN: %s", record);
        }

        LOG.infof("Processados %d registros ISIN do arquivo %s", records.size(), fileName);
        return records;
    }

    /**
     * Processa um arquivo Internal CSV
     */
    public List<InternalCsvRecord> processInternalCsv(String content, String fileName, String correlationId) {
        LOG.infof("Processando conteúdo de arquivo interno: %s", fileName);
        List<String> lines = CsvUtils.splitCsvLines(content);
        if (lines.isEmpty()) {
            throw new FileValidationException("Arquivo CSV vazio ou inválido",
                    createIncident(fileName, "INTERNAL", "Arquivo CSV vazio", "Parsing", correlationId));
        }

        // Pula a linha de cabeçalho
        List<InternalCsvRecord> records = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] fields = CsvUtils.splitCsvFields(line, ";");

            if (fields.length < 4) {
                throw new FileValidationException(
                        "Linha " + i + " não tem todos os campos necessários",
                        createIncident(fileName, "INTERNAL",
                                "Linha " + i + " tem apenas " + fields.length + " campos",
                                "Parsing linha " + i, correlationId));
            }

            InternalCsvRecord record = new InternalCsvRecord();
            record.setInternalId(CsvUtils.getStringField(fields, 0, null));
            record.setInternalCode(CsvUtils.getStringField(fields, 1, null));
            record.setInternalAmount(CsvUtils.getBigDecimalField(fields, 2, null));
            record.setCorrelationKey(CsvUtils.getStringField(fields, 3, null));

            // Validação
            validateInternalRecord(record, i, fileName, correlationId);

            records.add(record);
            LOG.debugf("Processado registro interno: %s", record);
        }

        LOG.infof("Processados %d registros internos do arquivo %s", records.size(), fileName);
        return records;
    }

    /**
     * Valida um registro External
     */
    private void validateExternalRecord(ExternalCsvRecord record, int lineNumber, String fileName, String correlationId) {
        if (record.getExternalId() == null || record.getExternalId().isEmpty()) {
            throw new FileValidationException(
                    "ExternalId não pode ser vazio na linha " + lineNumber,
                    createIncident(fileName, "EXTERNAL",
                            "ExternalId vazio na linha " + lineNumber,
                            "Validação linha " + lineNumber, correlationId));
        }

        if (record.getExternalName() == null || record.getExternalName().isEmpty()) {
            throw new FileValidationException(
                    "ExternalName não pode ser vazio na linha " + lineNumber,
                    createIncident(fileName, "EXTERNAL",
                            "ExternalName vazio na linha " + lineNumber,
                            "Validação linha " + lineNumber, correlationId));
        }

        if (record.getExternalValue() == null) {
            throw new FileValidationException(
                    "ExternalValue não pode ser nulo na linha " + lineNumber,
                    createIncident(fileName, "EXTERNAL",
                            "ExternalValue nulo na linha " + lineNumber,
                            "Validação linha " + lineNumber, correlationId));
        }

        if (record.getCorrelationKey() == null || record.getCorrelationKey().isEmpty()) {
            throw new FileValidationException(
                    "CorrelationKey não pode ser vazio na linha " + lineNumber,
                    createIncident(fileName, "EXTERNAL",
                            "CorrelationKey vazio na linha " + lineNumber,
                            "Validação linha " + lineNumber, correlationId));
        }
    }

    /**
     * Valida um registro ISIN
     */
    private void validateIsinRecord(IsinCsvRecord record, int lineNumber, String fileName, String correlationId) {
        if (record.getIsin() == null || record.getIsin().isEmpty()) {
            throw new FileValidationException(
                    "ISIN não pode ser vazio na linha " + lineNumber,
                    createIncident(fileName, "ISIN",
                            "ISIN vazio na linha " + lineNumber,
                            "Validação linha " + lineNumber, correlationId));
        }

        if (record.getIsinDescription() == null || record.getIsinDescription().isEmpty()) {
            throw new FileValidationException(
                    "IsinDescription não pode ser vazio na linha " + lineNumber,
                    createIncident(fileName, "ISIN",
                            "IsinDescription vazio na linha " + lineNumber,
                            "Validação linha " + lineNumber, correlationId));
        }

        if (record.getIsinCategory() == null || record.getIsinCategory().isEmpty()) {
            throw new FileValidationException(
                    "IsinCategory não pode ser vazio na linha " + lineNumber,
                    createIncident(fileName, "ISIN",
                            "IsinCategory vazio na linha " + lineNumber,
                            "Validação linha " + lineNumber, correlationId));
        }

        if (record.getCorrelationKey() == null || record.getCorrelationKey().isEmpty()) {
            throw new FileValidationException(
                    "CorrelationKey não pode ser vazio na linha " + lineNumber,
                    createIncident(fileName, "ISIN",
                            "CorrelationKey vazio na linha " + lineNumber,
                            "Validação linha " + lineNumber, correlationId));
        }
    }

    /**
     * Valida um registro Internal
     */
    private void validateInternalRecord(InternalCsvRecord record, int lineNumber, String fileName, String correlationId) {
        if (record.getInternalId() == null || record.getInternalId().isEmpty()) {
            throw new FileValidationException(
                    "InternalId não pode ser vazio na linha " + lineNumber,
                    createIncident(fileName, "INTERNAL",
                            "InternalId vazio na linha " + lineNumber,
                            "Validação linha " + lineNumber, correlationId));
        }

        if (record.getInternalCode() == null || record.getInternalCode().isEmpty()) {
            throw new FileValidationException(
                    "InternalCode não pode ser vazio na linha " + lineNumber,
                    createIncident(fileName, "INTERNAL",
                            "InternalCode vazio na linha " + lineNumber,
                            "Validação linha " + lineNumber, correlationId));
        }

        if (record.getInternalAmount() == null) {
            throw new FileValidationException(
                    "InternalAmount não pode ser nulo na linha " + lineNumber,
                    createIncident(fileName, "INTERNAL",
                            "InternalAmount nulo na linha " + lineNumber,
                            "Validação linha " + lineNumber, correlationId));
        }

        if (record.getCorrelationKey() == null || record.getCorrelationKey().isEmpty()) {
            throw new FileValidationException(
                    "CorrelationKey não pode ser vazio na linha " + lineNumber,
                    createIncident(fileName, "INTERNAL",
                            "CorrelationKey vazio na linha " + lineNumber,
                            "Validação linha " + lineNumber, correlationId));
        }
    }

    /**
     * Cria um incidente para erro de processamento CSV
     */
    private ProcessingIncident createIncident(String fileName, String fileType, String message,
                                              String location, String correlationId) {
        return monitoringUseCase.createIncident(
                fileName, fileType, message, location, null, correlationId, "MOVED_TO_ERROR");
    }
}
