package com.poc.model.dto;

/**
 * DTO for ISIN data
 */
public class IsinDataDto {
    private String isin;
    private String isinDescription;
    private String isinCategory;

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
}