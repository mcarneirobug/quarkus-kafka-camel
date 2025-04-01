package com.poc.usecase;

import com.poc.enums.FileType;
import com.poc.model.entity.BatchProcessing;
import com.poc.model.entity.ExternalData;
import com.poc.model.entity.InternalData;
import com.poc.model.entity.IsinData;
import com.poc.repository.BatchProcessingRepository;
import com.poc.repository.ExternalDataRepository;
import com.poc.repository.InternalDataRepository;
import com.poc.repository.IsinDataRepository;
import com.poc.utils.CsvUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CsvProcessingUseCase {

    private static final Logger LOG = Logger.getLogger(CsvProcessingUseCase.class);

    private static final String EXTERNAL_CSV_HEADERS = "externalId;externalName;externalValue;correlationKey";
    private static final String ISIN_CSV_HEADERS = "isin;isinDescription;isinCategory;correlationKey";
    private static final String INTERNAL_CSV_HEADERS = "internalId;internalCode;internalAmount;correlationKey";
    private static final String FILE_NAME = "fileName";
    public static final String EMPTY_CSV_FILE_MESSAGE = "Empty CSV file";
    public static final String CSV_PROCESSING_LOCATION = "CSV Processing";

    private final BatchProcessingRepository batchProcessingRepository;
    private final ExternalDataRepository externalDataRepository;
    private final IsinDataRepository isinDataRepository;
    private final InternalDataRepository internalDataRepository;
    private final MonitoringUseCase monitoringUseCase;

    @Inject
    public CsvProcessingUseCase(
            BatchProcessingRepository batchProcessingRepository,
            ExternalDataRepository externalDataRepository,
            IsinDataRepository isinDataRepository,
            InternalDataRepository internalDataRepository,
            MonitoringUseCase monitoringUseCase) {
        this.batchProcessingRepository = batchProcessingRepository;
        this.externalDataRepository = externalDataRepository;
        this.isinDataRepository = isinDataRepository;
        this.internalDataRepository = internalDataRepository;
        this.monitoringUseCase = monitoringUseCase;
    }

    /**
     * Process external CSV content, store in database, and update batch status
     */
    @Transactional
    public List<ExternalData> processExternalCsv(final String content, final String fileName, final String batchId) {
        LOG.infof("Processing external file: %s for batch %s", fileName, batchId);

        if (externalDataRepository.count(FILE_NAME, fileName) > 0) {
            LOG.infof("File %s has already been processed, skipping", fileName);
            return List.of();
        }

        var lines = CsvUtils.splitCsvLines(content);
        if (lines.isEmpty()) {
            monitoringUseCase.logIncident(fileName, FileType.EXTERNAL.name(), EMPTY_CSV_FILE_MESSAGE,
                    CSV_PROCESSING_LOCATION, null, batchId);
            return List.of();
        }

        validateHeader(lines.getFirst(), EXTERNAL_CSV_HEADERS, fileName, FileType.EXTERNAL.name(), batchId);

        List<ExternalData> records = new ArrayList<>();

        // Skip header
        for (int i = 1; i < lines.size(); i++) {
            try {
                String line = lines.get(i);
                String[] fields = CsvUtils.splitCsvFields(line, ";");

                if (fields.length < 4) {
                    monitoringUseCase.logIncident(fileName, "EXTERNAL",
                            "Line " + i + " has insufficient fields: " + fields.length,
                            CSV_PROCESSING_LOCATION, null, batchId);
                    continue;
                }

                // Check if record already exists
                List<ExternalData> existing = externalDataRepository.list(
                        "batchId = ?1 and externalId = ?2 and correlationKey = ?3",
                        batchId, fields[0].trim(), fields[3].trim());

                if (!existing.isEmpty()) {
                    LOG.infof("Record already exists for externalId=%s, correlationKey=%s in batch %s, skipping",
                            fields[0].trim(), fields[3].trim(), batchId);
                    continue;
                }

                ExternalData record = new ExternalData();
                record.setId(UUID.randomUUID());
                record.setBatchId(batchId);
                record.setFileName(fileName);
                record.setExternalId(fields[0].trim());
                record.setExternalName(fields[1].trim());
                record.setExternalValue(new BigDecimal(fields[2].trim()));
                record.setCorrelationKey(fields[3].trim());

                records.add(record);
            } catch (Exception e) {
                monitoringUseCase.logIncident(fileName, "EXTERNAL",
                        "Error processing line " + i + ": " + e.getMessage(),
                        CSV_PROCESSING_LOCATION, e, batchId);
            }
        }

        // Save records to database
        if (!records.isEmpty()) {
            for (ExternalData external : records) {
                externalDataRepository.persist(external);
            }
            LOG.infof("Saved %d external records to database", records.size());
        }

        // Update batch status
        updateBatchStatus(batchId, FileType.EXTERNAL.name());

        LOG.infof("Processed external file %s: extracted %d records", fileName, records.size());
        return records;
    }

    /**
     * Process ISIN CSV content, store in database, and update batch status
     */
    @Transactional
    public List<IsinData> processIsinCsv(String content, String fileName, String batchId) {
        LOG.infof("Processing ISIN file: %s for batch %s", fileName, batchId);

        // Check if this file has already been processed
        if (isinDataRepository.count(FILE_NAME, fileName) > 0) {
            LOG.infof("ISIN file %s has already been processed, skipping", fileName);
            return List.of();
        }

        List<String> lines = CsvUtils.splitCsvLines(content);

        if (lines.isEmpty()) {
            monitoringUseCase.logIncident(fileName, FileType.ISIN.name(), EMPTY_CSV_FILE_MESSAGE,
                    CSV_PROCESSING_LOCATION, null, batchId);
            return List.of();
        }

        // Validate header
        String headerLine = lines.getFirst();
        if (!headerLine.trim().equalsIgnoreCase(ISIN_CSV_HEADERS)) {
            LOG.warnf("ISIN CSV has unexpected header: %s", headerLine);
            monitoringUseCase.logIncident(fileName, "ISIN",
                    "Invalid header: " + headerLine,
                    CSV_PROCESSING_LOCATION, null, batchId);
        }

        List<IsinData> records = new ArrayList<>();

        // Skip header
        for (int i = 1; i < lines.size(); i++) {
            try {
                String line = lines.get(i);
                String[] fields = CsvUtils.splitCsvFields(line, ";");

                if (fields.length < 4) {
                    monitoringUseCase.logIncident(fileName, FileType.ISIN.name(),
                            "Line " + i + " has insufficient fields: " + fields.length,
                            CSV_PROCESSING_LOCATION, null, batchId);
                    continue;
                }

                IsinData record = new IsinData();
                record.setId(UUID.randomUUID());
                record.setBatchId(batchId);
                record.setFileName(fileName);
                record.setIsin(fields[0].trim());
                record.setIsinDescription(fields[1].trim());
                record.setIsinCategory(fields[2].trim());
                record.setCorrelationKey(fields[3].trim());

                records.add(record);
            } catch (Exception e) {
                monitoringUseCase.logIncident(fileName, FileType.ISIN.name(),
                        "Error processing line " + i + ": " + e.getMessage(),
                        CSV_PROCESSING_LOCATION, e, batchId);
            }
        }

        // Save records to database
        if (!records.isEmpty()) {
            for (IsinData isin : records) {
                isinDataRepository.persist(isin);
            }
            LOG.infof("Saved %d ISIN records to database", records.size());
        }

        // Update batch status
        updateBatchStatus(batchId, FileType.ISIN.name());

        LOG.infof("Processed ISIN file %s: extracted %d records", fileName, records.size());
        return records;
    }

    /**
     * Process internal CSV content, store in database, and update batch status
     */
    @Transactional
    public List<InternalData> processInternalCsv(String content, String fileName, String batchId) {
        LOG.infof("Processing internal file: %s for batch %s", fileName, batchId);

        if (internalDataRepository.count(FILE_NAME, fileName) > 0) {
            LOG.infof("Internal file %s has already been processed, skipping", fileName);
            return List.of();
        }

        List<String> lines = CsvUtils.splitCsvLines(content);

        if (lines.isEmpty()) {
            monitoringUseCase.logIncident(fileName, FileType.INTERNAL.name(), EMPTY_CSV_FILE_MESSAGE,
                    CSV_PROCESSING_LOCATION, null, batchId);
            return List.of();
        }

        // Validate header
        String headerLine = lines.getFirst();
        if (!headerLine.trim().equalsIgnoreCase(INTERNAL_CSV_HEADERS)) {
            LOG.warnf("Internal CSV has unexpected header: %s", headerLine);
            monitoringUseCase.logIncident(fileName, FileType.INTERNAL.name(),
                    "Invalid header: " + headerLine,
                    CSV_PROCESSING_LOCATION, null, batchId);
        }

        List<InternalData> records = new ArrayList<>();

        // Skip header
        for (int i = 1; i < lines.size(); i++) {
            try {
                String line = lines.get(i);
                String[] fields = CsvUtils.splitCsvFields(line, ";");

                if (fields.length < 4) {
                    monitoringUseCase.logIncident(fileName, FileType.INTERNAL.name(),
                            "Line " + i + " has insufficient fields: " + fields.length,
                            CSV_PROCESSING_LOCATION, null, batchId);
                    continue;
                }

                InternalData internalData = new InternalData();
                internalData.setId(UUID.randomUUID());
                internalData.setBatchId(batchId);
                internalData.setFileName(fileName);
                internalData.setInternalId(fields[0].trim());
                internalData.setInternalCode(fields[1].trim());
                internalData.setInternalAmount(new BigDecimal(fields[2].trim()));
                internalData.setCorrelationKey(fields[3].trim());

                records.add(internalData);
            } catch (Exception e) {
                monitoringUseCase.logIncident(fileName, FileType.INTERNAL.name(),
                        "Error processing line " + i + ": " + e.getMessage(),
                        CSV_PROCESSING_LOCATION, e, batchId);
            }
        }

        // Save records to database
        if (!records.isEmpty()) {
            for (InternalData internal : records) {
                internalDataRepository.persist(internal);
            }
            LOG.infof("Saved %d internal records to database", records.size());
        }

        // Update batch status
        updateBatchStatus(batchId, FileType.INTERNAL.name());

        LOG.infof("Processed internal file %s: extracted %d records", fileName, records.size());
        return records;
    }

    /**
     * Update batch status and check if ready for processing
     */
    @Transactional
    protected void updateBatchStatus(String batchId, String fileType) {
        LOG.infof("Updating batch status for %s: adding file type %s", batchId, fileType);

        // Get or create batch record
        BatchProcessing batch = batchProcessingRepository.findById(batchId);
        if (batch == null) {
            batch = new BatchProcessing(batchId);
            batchProcessingRepository.persist(batch);
        }

        // Mark file type as received
        batch.markFileTypeReceived(fileType);

        // Check if batch is now ready for processing
        if (batch.hasAllFileTypes()) {
            LOG.infof("Batch %s has all required file types, marking as ready for processing", batchId);
            batch.setReadyForProcessing(true);
        }

        batchProcessingRepository.persistAndFlush(batch);
        LOG.infof("Batch status updated: %s", batch.getProcessingStatus());
    }

    /**
     * Validates the header line. Logs a warning and registers an incident if the header is unexpected.
     *
     * @param headerLine     The header line read from the CSV.
     * @param expectedHeader The expected CSV header.
     * @param fileName       The file name.
     * @param fileType       The CSV file type ("EXTERNAL", "ISIN" or "INTERNAL").
     * @param batchId        The batch identifier.
     * @return true if header matches the expected header; false otherwise.
     */
    private boolean validateHeader(String headerLine, String expectedHeader, String fileName, String fileType, String batchId) {
        if (!headerLine.trim().equalsIgnoreCase(expectedHeader)) {
            LOG.warnf("%s CSV has unexpected header: %s", fileType, headerLine);
            monitoringUseCase.logIncident(fileName, fileType, "Invalid header: " + headerLine,
                    CSV_PROCESSING_LOCATION, null, batchId);
            return false;
        }
        return true;
    }
}