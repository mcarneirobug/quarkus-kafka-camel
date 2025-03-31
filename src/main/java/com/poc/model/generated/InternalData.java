
package com.poc.model.generated;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Data from the internal file
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "internalId",
    "internalCode",
    "internalAmount"
})
@Generated("jsonschema2pojo")
public class InternalData {

    /**
     * Internal identifier
     * (Required)
     * 
     */
    @JsonProperty("internalId")
    @JsonPropertyDescription("Internal identifier")
    private String internalId;
    /**
     * Internal code
     * (Required)
     * 
     */
    @JsonProperty("internalCode")
    @JsonPropertyDescription("Internal code")
    private String internalCode;
    /**
     * Internal amount
     * (Required)
     * 
     */
    @JsonProperty("internalAmount")
    @JsonPropertyDescription("Internal amount")
    private Double internalAmount;

    /**
     * No args constructor for use in serialization
     * 
     */
    public InternalData() {
    }

    /**
     * 
     * @param internalId
     *     Internal identifier.
     * @param internalAmount
     *     Internal amount.
     * @param internalCode
     *     Internal code.
     */
    public InternalData(String internalId, String internalCode, Double internalAmount) {
        super();
        this.internalId = internalId;
        this.internalCode = internalCode;
        this.internalAmount = internalAmount;
    }

    /**
     * Internal identifier
     * (Required)
     * 
     */
    @JsonProperty("internalId")
    public String getInternalId() {
        return internalId;
    }

    /**
     * Internal identifier
     * (Required)
     * 
     */
    @JsonProperty("internalId")
    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public InternalData withInternalId(String internalId) {
        this.internalId = internalId;
        return this;
    }

    /**
     * Internal code
     * (Required)
     * 
     */
    @JsonProperty("internalCode")
    public String getInternalCode() {
        return internalCode;
    }

    /**
     * Internal code
     * (Required)
     * 
     */
    @JsonProperty("internalCode")
    public void setInternalCode(String internalCode) {
        this.internalCode = internalCode;
    }

    public InternalData withInternalCode(String internalCode) {
        this.internalCode = internalCode;
        return this;
    }

    /**
     * Internal amount
     * (Required)
     * 
     */
    @JsonProperty("internalAmount")
    public Double getInternalAmount() {
        return internalAmount;
    }

    /**
     * Internal amount
     * (Required)
     * 
     */
    @JsonProperty("internalAmount")
    public void setInternalAmount(Double internalAmount) {
        this.internalAmount = internalAmount;
    }

    public InternalData withInternalAmount(Double internalAmount) {
        this.internalAmount = internalAmount;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(InternalData.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("internalId");
        sb.append('=');
        sb.append(((this.internalId == null)?"<null>":this.internalId));
        sb.append(',');
        sb.append("internalCode");
        sb.append('=');
        sb.append(((this.internalCode == null)?"<null>":this.internalCode));
        sb.append(',');
        sb.append("internalAmount");
        sb.append('=');
        sb.append(((this.internalAmount == null)?"<null>":this.internalAmount));
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
        result = ((result* 31)+((this.internalId == null)? 0 :this.internalId.hashCode()));
        result = ((result* 31)+((this.internalAmount == null)? 0 :this.internalAmount.hashCode()));
        result = ((result* 31)+((this.internalCode == null)? 0 :this.internalCode.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof InternalData) == false) {
            return false;
        }
        InternalData rhs = ((InternalData) other);
        return ((((this.internalId == rhs.internalId)||((this.internalId!= null)&&this.internalId.equals(rhs.internalId)))&&((this.internalAmount == rhs.internalAmount)||((this.internalAmount!= null)&&this.internalAmount.equals(rhs.internalAmount))))&&((this.internalCode == rhs.internalCode)||((this.internalCode!= null)&&this.internalCode.equals(rhs.internalCode))));
    }

}
