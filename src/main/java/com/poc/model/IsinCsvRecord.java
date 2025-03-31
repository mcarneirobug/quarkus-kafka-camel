package com.poc.model;

import jakarta.validation.constraints.NotEmpty;
import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

@CsvRecord(separator = ";", skipFirstLine = true)
public class IsinCsvRecord {

    @DataField(pos = 1)
    @NotEmpty(message = "isin não pode ser vazio")
    private String isin;

    @DataField(pos = 2)
    @NotEmpty(message = "isinDescription não pode ser vazio")
    private String isinDescription;

    @DataField(pos = 3)
    @NotEmpty(message = "isinCategory não pode ser vazio")
    private String isinCategory;

    @DataField(pos = 4)
    @NotEmpty(message = "correlationKey não pode ser vazio")
    private String correlationKey;

    // Getters and Setters
    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getIsinDescription() {
        return isinDescription;
    }

    public void setIsinDescription(String isinDescription) {
        this.isinDescription = isinDescription;
    }

    public String getIsinCategory() {
        return isinCategory;
    }

    public void setIsinCategory(String isinCategory) {
        this.isinCategory = isinCategory;
    }

    public String getCorrelationKey() {
        return correlationKey;
    }

    public void setCorrelationKey(String correlationKey) {
        this.correlationKey = correlationKey;
    }

    @Override
    public String toString() {
        return "IsinCsvRecord{" +
                "isin='" + isin + '\'' +
                ", isinDescription='" + isinDescription + '\'' +
                ", isinCategory='" + isinCategory + '\'' +
                ", correlationKey='" + correlationKey + '\'' +
                '}';
    }
}