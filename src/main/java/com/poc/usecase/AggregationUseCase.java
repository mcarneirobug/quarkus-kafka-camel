package com.poc.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.exception.BusinessLogicException;
import com.poc.model.ExternalCsvRecord;
import com.poc.model.InternalCsvRecord;
import com.poc.model.IsinCsvRecord;
import com.poc.model.generated.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * UseCase para agregar os dados dos três tipos de arquivos
 */
@ApplicationScoped
public class AggregationUseCase {

    private static final Logger LOG = Logger.getLogger(AggregationUseCase.class);

    @Inject
    ObjectMapper objectMapper;

    @Inject
    MonitoringUseCase monitoringUseCase;

    // Armazenamento dos registros por tipo
    private final Map<String, List<ExternalCsvRecord>> externalRecords = new ConcurrentHashMap<>();
    private final Map<String, List<IsinCsvRecord>> isinRecords = new ConcurrentHashMap<>();
    private final Map<String, List<InternalCsvRecord>> internalRecords = new ConcurrentHashMap<>();

    /**
     * Armazena registros de um arquivo externo para correlação posterior
     */
    public void addExternalBatch(List<ExternalCsvRecord> records, String correlationId) {
        LOG.infof("Adicionando lote de registros externos: count=%d, correlationId=%s",
                records.size(), correlationId);
        externalRecords.put(correlationId, records);
    }

    /**
     * Armazena registros de um arquivo ISIN para correlação posterior
     */
    public void addIsinBatch(List<IsinCsvRecord> records, String correlationId) {
        LOG.infof("Adicionando lote de registros ISIN: count=%d, correlationId=%s",
                records.size(), correlationId);
        isinRecords.put(correlationId, records);
    }

    /**
     * Armazena registros de um arquivo interno para correlação posterior
     */
    public void addInternalBatch(List<InternalCsvRecord> records, String correlationId) {
        LOG.infof("Adicionando lote de registros internos: count=%d, correlationId=%s",
                records.size(), correlationId);
        internalRecords.put(correlationId, records);
    }

    /**
     * Verifica se todos os tipos de arquivos foram recebidos para um correlationId
     */
    public boolean isReadyForAggregation(String correlationId) {
        boolean hasExternal = !externalRecords.isEmpty() && externalRecords.containsKey(correlationId);
        boolean hasIsin = !isinRecords.isEmpty() && isinRecords.containsKey(correlationId);
        boolean hasInternal = !internalRecords.isEmpty() && internalRecords.containsKey(correlationId);

        boolean isReady = hasExternal && hasIsin && hasInternal;

        LOG.infof("Verificando agregação para o lote %s: external=%s, isin=%s, internal=%s, isReady=%s",
                correlationId, hasExternal, hasIsin, hasInternal, isReady);

        return isReady;
    }

    /**
     * Agrega os registros com base na chave de correlação entre registros
     */
    public List<AggregatedEvent> aggregateEvents(String batchCorrelationId) {
        LOG.infof("Iniciando agregação para o lote: %s", batchCorrelationId);

        if (!isReadyForAggregation(batchCorrelationId)) {
            ProcessingIncident incident = monitoringUseCase.createIncident(
                    "N/A", "AGGREGATION",
                    "Tentativa de agregação sem todos os arquivos necessários",
                    "AggregationUseCase", null, batchCorrelationId, "MANUAL_INTERVENTION_REQUIRED");

            throw new BusinessLogicException(
                    "Não é possível agregar eventos sem todos os tipos de arquivos", incident);
        }

        List<ExternalCsvRecord> externalBatch = externalRecords.get(batchCorrelationId);
        List<IsinCsvRecord> isinBatch = isinRecords.get(batchCorrelationId);
        List<InternalCsvRecord> internalBatch = internalRecords.get(batchCorrelationId);

        // Mapeamento dos registros por chave de correlação
        Map<String, ExternalCsvRecord> externalMap = externalBatch.stream()
                .collect(Collectors.toMap(ExternalCsvRecord::getCorrelationKey, r -> r, (r1, r2) -> r1));

        Map<String, IsinCsvRecord> isinMap = isinBatch.stream()
                .collect(Collectors.toMap(IsinCsvRecord::getCorrelationKey, r -> r, (r1, r2) -> r1));

        Map<String, InternalCsvRecord> internalMap = internalBatch.stream()
                .collect(Collectors.toMap(InternalCsvRecord::getCorrelationKey, r -> r, (r1, r2) -> r1));

        // Construir conjunto único de chaves de correlação
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(externalMap.keySet());
        allKeys.addAll(isinMap.keySet());
        allKeys.addAll(internalMap.keySet());

        List<Exception> errors = new ArrayList<>();
        List<AggregatedEvent> aggregatedEvents = new ArrayList<>();

        for (String key : allKeys) {
            try {
                AggregatedEvent event = new AggregatedEvent();
                event.setEventId(UUID.randomUUID().toString());
                event.setTimestamp(new Date());
                event.setCorrelationId(key);

                // Dados de processamento
                ProcessingInfo processingInfo = new ProcessingInfo();
                processingInfo.setProcessedAt(new Date());
                processingInfo.setSourceBatch(batchCorrelationId);

                // Determinar se é completo ou parcial
                boolean hasAllData = externalMap.containsKey(key) &&
                        isinMap.containsKey(key) &&
                        internalMap.containsKey(key);

                processingInfo.setStatus(hasAllData ?
                        ProcessingInfo.Status.COMPLETE :
                        ProcessingInfo.Status.PARTIAL);
                event.setProcessingInfo(processingInfo);

                // Adicionar dados externos se disponíveis
                if (externalMap.containsKey(key)) {
                    ExternalCsvRecord externalRecord = externalMap.get(key);
                    ExternalData externalData = new ExternalData();
                    externalData.setExternalId(externalRecord.getExternalId());
                    externalData.setExternalName(externalRecord.getExternalName());
                    externalData.setExternalValue(externalRecord.getExternalValue().doubleValue());
                    event.setExternalData(externalData);
                }

                // Adicionar dados ISIN se disponíveis
                if (isinMap.containsKey(key)) {
                    IsinCsvRecord isinRecord = isinMap.get(key);
                    IsinData isinData = new IsinData();
                    isinData.setIsin(isinRecord.getIsin());
                    isinData.setIsinDescription(isinRecord.getIsinDescription());
                    isinData.setIsinCategory(isinRecord.getIsinCategory());
                    event.setIsinData(isinData);
                }

                // Adicionar dados internos se disponíveis
                if (internalMap.containsKey(key)) {
                    InternalCsvRecord internalRecord = internalMap.get(key);
                    InternalData internalData = new InternalData();
                    internalData.setInternalId(internalRecord.getInternalId());
                    internalData.setInternalCode(internalRecord.getInternalCode());
                    internalData.setInternalAmount(internalRecord.getInternalAmount().doubleValue());
                    event.setInternalData(internalData);
                }

                aggregatedEvents.add(event);
            } catch (Exception e) {
                LOG.errorf("Erro ao agregar evento para a chave %s: %s", key, e.getMessage());
                ProcessingIncident incident = monitoringUseCase.createIncident(
                        "N/A", "AGGREGATION",
                        "Erro ao agregar evento para chave: " + key,
                        "AggregationUseCase", e, batchCorrelationId, "MANUAL_INTERVENTION_REQUIRED");

                monitoringUseCase.registerIncident(incident);
                errors.add(e);
            }
        }

        // verifica se houve errors
        if (!errors.isEmpty()) {
            // Se houve erros, criar um incidente e não retornar nada
            ProcessingIncident incident = monitoringUseCase.createIncident(
                    "N/A", "AGGREGATION",
                    "Erros durante a agregação de dados. Total de erros: " + errors.size(),
                    "AggregationUseCase", errors.getFirst(), batchCorrelationId, "MANUAL_INTERVENTION_REQUIRED");

            throw new BusinessLogicException(
                    "Erros durante a agregação de dados", errors.getFirst(), incident);
        }

        LOG.infof("Agregação concluída: lote=%s, eventos=%d", batchCorrelationId, aggregatedEvents.size());

        // Limpar dados de memória após a agregação
        cleanupBatch(batchCorrelationId);

        return aggregatedEvents;
    }

    /**
     * Limpa os dados do lote da memória após processamento
     */
    private void cleanupBatch(String batchCorrelationId) {
        externalRecords.remove(batchCorrelationId);
        isinRecords.remove(batchCorrelationId);
        internalRecords.remove(batchCorrelationId);
        LOG.infof("Dados do lote %s removidos da memória", batchCorrelationId);
    }

    /**
     * Serializa eventos agregados para JSON
     */
    public List<String> serializeEvents(List<AggregatedEvent> events) {
        return events.stream()
                .map(event -> {
                    try {
                        return objectMapper.writeValueAsString(event);
                    } catch (Exception e) {
                        LOG.errorf("Erro ao serializar evento %s: %s", event.getEventId(), e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }
}