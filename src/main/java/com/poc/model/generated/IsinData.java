
package com.poc.model.generated;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Data from the ISIN file
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "isin",
    "isinDescription",
    "isinCategory"
})
@Generated("jsonschema2pojo")
public class IsinData {

    /**
     * ISIN code
     * (Required)
     * 
     */
    @JsonProperty("isin")
    @JsonPropertyDescription("ISIN code")
    private String isin;
    /**
     * ISIN description
     * (Required)
     * 
     */
    @JsonProperty("isinDescription")
    @JsonPropertyDescription("ISIN description")
    private String isinDescription;
    /**
     * ISIN category
     * (Required)
     * 
     */
    @JsonProperty("isinCategory")
    @JsonPropertyDescription("ISIN category")
    private String isinCategory;

    /**
     * No args constructor for use in serialization
     * 
     */
    public IsinData() {
    }

    /**
     * 
     * @param isinCategory
     *     ISIN category.
     * @param isin
     *     ISIN code.
     * @param isinDescription
     *     ISIN description.
     */
    public IsinData(String isin, String isinDescription, String isinCategory) {
        super();
        this.isin = isin;
        this.isinDescription = isinDescription;
        this.isinCategory = isinCategory;
    }

    /**
     * ISIN code
     * (Required)
     * 
     */
    @JsonProperty("isin")
    public String getIsin() {
        return isin;
    }

    /**
     * ISIN code
     * (Required)
     * 
     */
    @JsonProperty("isin")
    public void setIsin(String isin) {
        this.isin = isin;
    }

    public IsinData withIsin(String isin) {
        this.isin = isin;
        return this;
    }

    /**
     * ISIN description
     * (Required)
     * 
     */
    @JsonProperty("isinDescription")
    public String getIsinDescription() {
        return isinDescription;
    }

    /**
     * ISIN description
     * (Required)
     * 
     */
    @JsonProperty("isinDescription")
    public void setIsinDescription(String isinDescription) {
        this.isinDescription = isinDescription;
    }

    public IsinData withIsinDescription(String isinDescription) {
        this.isinDescription = isinDescription;
        return this;
    }

    /**
     * ISIN category
     * (Required)
     * 
     */
    @JsonProperty("isinCategory")
    public String getIsinCategory() {
        return isinCategory;
    }

    /**
     * ISIN category
     * (Required)
     * 
     */
    @JsonProperty("isinCategory")
    public void setIsinCategory(String isinCategory) {
        this.isinCategory = isinCategory;
    }

    public IsinData withIsinCategory(String isinCategory) {
        this.isinCategory = isinCategory;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(IsinData.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("isin");
        sb.append('=');
        sb.append(((this.isin == null)?"<null>":this.isin));
        sb.append(',');
        sb.append("isinDescription");
        sb.append('=');
        sb.append(((this.isinDescription == null)?"<null>":this.isinDescription));
        sb.append(',');
        sb.append("isinCategory");
        sb.append('=');
        sb.append(((this.isinCategory == null)?"<null>":this.isinCategory));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.isinCategory == null)? 0 :this.isinCategory.hashCode()));
        result = ((result* 31)+((this.isin == null)? 0 :this.isin.hashCode()));
        result = ((result* 31)+((this.isinDescription == null)? 0 :this.isinDescription.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof IsinData) == false) {
            return false;
        }
        IsinData rhs = ((IsinData) other);
        return ((((this.isinCategory == rhs.isinCategory)||((this.isinCategory!= null)&&this.isinCategory.equals(rhs.isinCategory)))&&((this.isin == rhs.isin)||((this.isin!= null)&&this.isin.equals(rhs.isin))))&&((this.isinDescription == rhs.isinDescription)||((this.isinDescription!= null)&&this.isinDescription.equals(rhs.isinDescription))));
    }

}
