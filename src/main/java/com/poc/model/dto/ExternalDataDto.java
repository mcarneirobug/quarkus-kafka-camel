package com.poc.model.dto;

/**
 * DTO for external data
 */
public class ExternalDataDto {
    private String externalId;
    private String externalName;
    private double externalValue;

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

    public double getExternalValue() {
        return externalValue;
    }

    public void setExternalValue(double externalValue) {
        this.externalValue = externalValue;
    }
}
