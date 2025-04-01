package com.poc.model.dto;

/**
 * DTO for internal data
 */
public class InternalDataDto {
    private String internalId;
    private String internalCode;
    private double internalAmount;

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

    public double getInternalAmount() {
        return internalAmount;
    }

    public void setInternalAmount(double internalAmount) {
        this.internalAmount = internalAmount;
    }
}