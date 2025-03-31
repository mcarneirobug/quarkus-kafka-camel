
package com.poc.model.generated;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * Data from the external file
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "externalId",
    "externalName",
    "externalValue"
})
@Generated("jsonschema2pojo")
public class ExternalData {

    /**
     * External identifier
     * (Required)
     * 
     */
    @JsonProperty("externalId")
    @JsonPropertyDescription("External identifier")
    private String externalId;
    /**
     * External name
     * (Required)
     * 
     */
    @JsonProperty("externalName")
    @JsonPropertyDescription("External name")
    private String externalName;
    /**
     * External value
     * (Required)
     * 
     */
    @JsonProperty("externalValue")
    @JsonPropertyDescription("External value")
    private Double externalValue;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ExternalData() {
    }

    /**
     * 
     * @param externalName
     *     External name.
     * @param externalId
     *     External identifier.
     * @param externalValue
     *     External value.
     */
    public ExternalData(String externalId, String externalName, Double externalValue) {
        super();
        this.externalId = externalId;
        this.externalName = externalName;
        this.externalValue = externalValue;
    }

    /**
     * External identifier
     * (Required)
     * 
     */
    @JsonProperty("externalId")
    public String getExternalId() {
        return externalId;
    }

    /**
     * External identifier
     * (Required)
     * 
     */
    @JsonProperty("externalId")
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public ExternalData withExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    /**
     * External name
     * (Required)
     * 
     */
    @JsonProperty("externalName")
    public String getExternalName() {
        return externalName;
    }

    /**
     * External name
     * (Required)
     * 
     */
    @JsonProperty("externalName")
    public void setExternalName(String externalName) {
        this.externalName = externalName;
    }

    public ExternalData withExternalName(String externalName) {
        this.externalName = externalName;
        return this;
    }

    /**
     * External value
     * (Required)
     * 
     */
    @JsonProperty("externalValue")
    public Double getExternalValue() {
        return externalValue;
    }

    /**
     * External value
     * (Required)
     * 
     */
    @JsonProperty("externalValue")
    public void setExternalValue(Double externalValue) {
        this.externalValue = externalValue;
    }

    public ExternalData withExternalValue(Double externalValue) {
        this.externalValue = externalValue;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ExternalData.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("externalId");
        sb.append('=');
        sb.append(((this.externalId == null)?"<null>":this.externalId));
        sb.append(',');
        sb.append("externalName");
        sb.append('=');
        sb.append(((this.externalName == null)?"<null>":this.externalName));
        sb.append(',');
        sb.append("externalValue");
        sb.append('=');
        sb.append(((this.externalValue == null)?"<null>":this.externalValue));
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
        result = ((result* 31)+((this.externalId == null)? 0 :this.externalId.hashCode()));
        result = ((result* 31)+((this.externalName == null)? 0 :this.externalName.hashCode()));
        result = ((result* 31)+((this.externalValue == null)? 0 :this.externalValue.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExternalData) == false) {
            return false;
        }
        ExternalData rhs = ((ExternalData) other);
        return ((((this.externalId == rhs.externalId)||((this.externalId!= null)&&this.externalId.equals(rhs.externalId)))&&((this.externalName == rhs.externalName)||((this.externalName!= null)&&this.externalName.equals(rhs.externalName))))&&((this.externalValue == rhs.externalValue)||((this.externalValue!= null)&&this.externalValue.equals(rhs.externalValue))));
    }

}
