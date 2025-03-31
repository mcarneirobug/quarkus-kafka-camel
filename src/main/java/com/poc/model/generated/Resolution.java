
package com.poc.model.generated;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;


/**
 * Information about resolution steps
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "action",
    "errorFilePath",
    "retryCount"
})
@Generated("jsonschema2pojo")
public class Resolution {

    /**
     * Action taken to resolve the incident
     * (Required)
     * 
     */
    @JsonProperty("action")
    @JsonPropertyDescription("Action taken to resolve the incident")
    private Resolution.Action action;
    /**
     * Path where the error file was moved if applicable
     * 
     */
    @JsonProperty("errorFilePath")
    @JsonPropertyDescription("Path where the error file was moved if applicable")
    private String errorFilePath;
    /**
     * Number of retry attempts if applicable
     * 
     */
    @JsonProperty("retryCount")
    @JsonPropertyDescription("Number of retry attempts if applicable")
    private Integer retryCount;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Resolution() {
    }

    /**
     * 
     * @param retryCount
     *     Number of retry attempts if applicable.
     * @param action
     *     Action taken to resolve the incident.
     * @param errorFilePath
     *     Path where the error file was moved if applicable.
     */
    public Resolution(Resolution.Action action, String errorFilePath, Integer retryCount) {
        super();
        this.action = action;
        this.errorFilePath = errorFilePath;
        this.retryCount = retryCount;
    }

    /**
     * Action taken to resolve the incident
     * (Required)
     * 
     */
    @JsonProperty("action")
    public Resolution.Action getAction() {
        return action;
    }

    /**
     * Action taken to resolve the incident
     * (Required)
     * 
     */
    @JsonProperty("action")
    public void setAction(Resolution.Action action) {
        this.action = action;
    }

    public Resolution withAction(Resolution.Action action) {
        this.action = action;
        return this;
    }

    /**
     * Path where the error file was moved if applicable
     * 
     */
    @JsonProperty("errorFilePath")
    public String getErrorFilePath() {
        return errorFilePath;
    }

    /**
     * Path where the error file was moved if applicable
     * 
     */
    @JsonProperty("errorFilePath")
    public void setErrorFilePath(String errorFilePath) {
        this.errorFilePath = errorFilePath;
    }

    public Resolution withErrorFilePath(String errorFilePath) {
        this.errorFilePath = errorFilePath;
        return this;
    }

    /**
     * Number of retry attempts if applicable
     * 
     */
    @JsonProperty("retryCount")
    public Integer getRetryCount() {
        return retryCount;
    }

    /**
     * Number of retry attempts if applicable
     * 
     */
    @JsonProperty("retryCount")
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Resolution withRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Resolution.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("action");
        sb.append('=');
        sb.append(((this.action == null)?"<null>":this.action));
        sb.append(',');
        sb.append("errorFilePath");
        sb.append('=');
        sb.append(((this.errorFilePath == null)?"<null>":this.errorFilePath));
        sb.append(',');
        sb.append("retryCount");
        sb.append('=');
        sb.append(((this.retryCount == null)?"<null>":this.retryCount));
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
        result = ((result* 31)+((this.action == null)? 0 :this.action.hashCode()));
        result = ((result* 31)+((this.errorFilePath == null)? 0 :this.errorFilePath.hashCode()));
        result = ((result* 31)+((this.retryCount == null)? 0 :this.retryCount.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Resolution) == false) {
            return false;
        }
        Resolution rhs = ((Resolution) other);
        return ((((this.action == rhs.action)||((this.action!= null)&&this.action.equals(rhs.action)))&&((this.errorFilePath == rhs.errorFilePath)||((this.errorFilePath!= null)&&this.errorFilePath.equals(rhs.errorFilePath))))&&((this.retryCount == rhs.retryCount)||((this.retryCount!= null)&&this.retryCount.equals(rhs.retryCount))));
    }


    /**
     * Action taken to resolve the incident
     * 
     */
    @Generated("jsonschema2pojo")
    public enum Action {

        MOVED_TO_ERROR("MOVED_TO_ERROR"),
        RETRY("RETRY"),
        IGNORED("IGNORED"),
        MANUAL_INTERVENTION_REQUIRED("MANUAL_INTERVENTION_REQUIRED");
        private final String value;
        private final static Map<String, Resolution.Action> CONSTANTS = new HashMap<String, Resolution.Action>();

        static {
            for (Resolution.Action c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        Action(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static Resolution.Action fromValue(String value) {
            Resolution.Action constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
