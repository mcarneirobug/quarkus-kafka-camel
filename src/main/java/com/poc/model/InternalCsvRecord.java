package com.poc.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

import java.math.BigDecimal;

@CsvRecord(separator = ";", skipFirstLine = true)
public class InternalCsvRecord {

    @DataField(pos = 1)
    @NotEmpty(message = "internalId não pode ser vazio")
    private String internalId;

    @DataField(pos = 2)
    @NotEmpty(message = "internalCode não pode ser vazio")
    private String internalCode;

    @DataField(pos = 3)
    @NotNull(message = "internalAmount não pode ser nulo")
    private BigDecimal internalAmount;

    @DataField(pos = 4)
    @NotEmpty(message = "correlationKey não pode ser vazio")
    private String correlationKey;

    // Getters and Setters
    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public String getInternalCode() {
        return internalCode;
    }

    public void setInternalCode(String internalCode) {
        this.internalCode = internalCode;
    }

    public BigDecimal getInternalAmount() {
        return internalAmount;
    }

    public void setInternalAmount(BigDecimal internalAmount) {
        this.internalAmount = internalAmount;
    }

    public String getCorrelationKey() {
        return correlationKey;
    }

    public void setCorrelationKey(String correlationKey) {
        this.correlationKey = correlationKey;
    }

    @Override
    public String toString() {
        return "InternalCsvRecord{" +
                "internalId='" + internalId + '\'' +
                ", internalCode='" + internalCode + '\'' +
                ", internalAmount=" + internalAmount +
                ", correlationKey='" + correlationKey + '\'' +
                '}';
    }
}