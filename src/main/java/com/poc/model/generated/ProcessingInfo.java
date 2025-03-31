
package com.poc.model.generated;

import java.util.Date;
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
 * Meta information about the processing
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "processedAt",
    "sourceBatch",
    "status"
})
@Generated("jsonschema2pojo")
public class ProcessingInfo {

    /**
     * Time of processing
     * (Required)
     * 
     */
    @JsonProperty("processedAt")
    @JsonPropertyDescription("Time of processing")
    private Date processedAt;
    /**
     * Identifier of the source batch
     * (Required)
     * 
     */
    @JsonProperty("sourceBatch")
    @JsonPropertyDescription("Identifier of the source batch")
    private String sourceBatch;
    /**
     * Status of the aggregated event
     * (Required)
     * 
     */
    @JsonProperty("status")
    @JsonPropertyDescription("Status of the aggregated event")
    private ProcessingInfo.Status status;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ProcessingInfo() {
    }

    /**
     * 
     * @param processedAt
     *     Time of processing.
     * @param sourceBatch
     *     Identifier of the source batch.
     * @param status
     *     Status of the aggregated event.
     */
    public ProcessingInfo(Date processedAt, String sourceBatch, ProcessingInfo.Status status) {
        super();
        this.processedAt = processedAt;
        this.sourceBatch = sourceBatch;
        this.status = status;
    }

    /**
     * Time of processing
     * (Required)
     * 
     */
    @JsonProperty("processedAt")
    public Date getProcessedAt() {
        return processedAt;
    }

    /**
     * Time of processing
     * (Required)
     * 
     */
    @JsonProperty("processedAt")
    public void setProcessedAt(Date processedAt) {
        this.processedAt = processedAt;
    }

    public ProcessingInfo withProcessedAt(Date processedAt) {
        this.processedAt = processedAt;
        return this;
    }

    /**
     * Identifier of the source batch
     * (Required)
     * 
     */
    @JsonProperty("sourceBatch")
    public String getSourceBatch() {
        return sourceBatch;
    }

    /**
     * Identifier of the source batch
     * (Required)
     * 
     */
    @JsonProperty("sourceBatch")
    public void setSourceBatch(String sourceBatch) {
        this.sourceBatch = sourceBatch;
    }

    public ProcessingInfo withSourceBatch(String sourceBatch) {
        this.sourceBatch = sourceBatch;
        return this;
    }

    /**
     * Status of the aggregated event
     * (Required)
     * 
     */
    @JsonProperty("status")
    public ProcessingInfo.Status getStatus() {
        return status;
    }

    /**
     * Status of the aggregated event
     * (Required)
     * 
     */
    @JsonProperty("status")
    public void setStatus(ProcessingInfo.Status status) {
        this.status = status;
    }

    public ProcessingInfo withStatus(ProcessingInfo.Status status) {
        this.status = status;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ProcessingInfo.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("processedAt");
        sb.append('=');
        sb.append(((this.processedAt == null)?"<null>":this.processedAt));
        sb.append(',');
        sb.append("sourceBatch");
        sb.append('=');
        sb.append(((this.sourceBatch == null)?"<null>":this.sourceBatch));
        sb.append(',');
        sb.append("status");
        sb.append('=');
        sb.append(((this.status == null)?"<null>":this.status));
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
        result = ((result* 31)+((this.processedAt == null)? 0 :this.processedAt.hashCode()));
        result = ((result* 31)+((this.sourceBatch == null)? 0 :this.sourceBatch.hashCode()));
        result = ((result* 31)+((this.status == null)? 0 :this.status.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ProcessingInfo) == false) {
            return false;
        }
        ProcessingInfo rhs = ((ProcessingInfo) other);
        return ((((this.processedAt == rhs.processedAt)||((this.processedAt!= null)&&this.processedAt.equals(rhs.processedAt)))&&((this.sourceBatch == rhs.sourceBatch)||((this.sourceBatch!= null)&&this.sourceBatch.equals(rhs.sourceBatch))))&&((this.status == rhs.status)||((this.status!= null)&&this.status.equals(rhs.status))));
    }


    /**
     * Status of the aggregated event
     * 
     */
    @Generated("jsonschema2pojo")
    public enum Status {

        COMPLETE("COMPLETE"),
        PARTIAL("PARTIAL");
        private final String value;
        private final static Map<String, ProcessingInfo.Status> CONSTANTS = new HashMap<String, ProcessingInfo.Status>();

        static {
            for (ProcessingInfo.Status c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        Status(String value) {
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
        public static ProcessingInfo.Status fromValue(String value) {
            ProcessingInfo.Status constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
