package com.poc.model;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@CsvRecord(separator = ";", skipFirstLine = true)
public class ExternalCsvRecord {

    @DataField(pos = 1)
    @NotEmpty(message = "externalId não pode ser vazio")
    private String externalId;

    @DataField(pos = 2)
    @NotEmpty(message = "externalName não pode ser vazio")
    private String externalName;

    @DataField(pos = 3)
    @NotNull(message = "externalValue não pode ser nulo")
    private BigDecimal externalValue;

    // Key for correlation with other records
    @DataField(pos = 4)
    @NotEmpty(message = "correlationKey não pode ser vazio")
    private String correlationKey;

    // Getters and Setters
    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getExternalName() {
        return externalName;
    }

    public void setExternalName(String externalName) {
        this.externalName = externalName;
    }

    public BigDecimal getExternalValue() {
        return externalValue;
    }

    public void setExternalValue(BigDecimal externalValue) {
        this.externalValue = externalValue;
    }

    public String getCorrelationKey() {
        return correlationKey;
    }

    public void setCorrelationKey(String correlationKey) {
        this.correlationKey = correlationKey;
    }

    @Override
    public String toString() {
        return "ExternalCsvRecord{" +
                "externalId='" + externalId + '\'' +
                ", externalName='" + externalName + '\'' +
                ", externalValue=" + externalValue +
                ", correlationKey='" + correlationKey + '\'' +
                '}';
    }
}