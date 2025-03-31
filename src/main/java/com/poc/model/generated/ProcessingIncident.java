
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
 * ProcessingIncident
 * <p>
 * Schema for processing incidents during file processing
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "incidentId",
    "timestamp",
    "severity",
    "type",
    "message",
    "details",
    "correlationId",
    "resolution"
})
@Generated("jsonschema2pojo")
public class ProcessingIncident {

    /**
     * Unique identifier for the incident
     * (Required)
     * 
     */
    @JsonProperty("incidentId")
    @JsonPropertyDescription("Unique identifier for the incident")
    private String incidentId;
    /**
     * Time when the incident occurred
     * (Required)
     * 
     */
    @JsonProperty("timestamp")
    @JsonPropertyDescription("Time when the incident occurred")
    private Date timestamp;
    /**
     * Severity level of the incident
     * (Required)
     * 
     */
    @JsonProperty("severity")
    @JsonPropertyDescription("Severity level of the incident")
    private ProcessingIncident.Severity severity;
    /**
     * Type of the incident
     * (Required)
     * 
     */
    @JsonProperty("type")
    @JsonPropertyDescription("Type of the incident")
    private ProcessingIncident.Type type;
    /**
     * Descriptive message about the incident
     * (Required)
     * 
     */
    @JsonProperty("message")
    @JsonPropertyDescription("Descriptive message about the incident")
    private String message;
    /**
     * Detailed information about the incident
     * (Required)
     * 
     */
    @JsonProperty("details")
    @JsonPropertyDescription("Detailed information about the incident")
    private Details details;
    /**
     * ID to correlate incidents with processing batches
     * (Required)
     * 
     */
    @JsonProperty("correlationId")
    @JsonPropertyDescription("ID to correlate incidents with processing batches")
    private String correlationId;
    /**
     * Information about resolution steps
     * 
     */
    @JsonProperty("resolution")
    @JsonPropertyDescription("Information about resolution steps")
    private Resolution resolution;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ProcessingIncident() {
    }

    /**
     * 
     * @param severity
     *     Severity level of the incident.
     * @param details
     *     Detailed information about the incident.
     * @param correlationId
     *     ID to correlate incidents with processing batches.
     * @param incidentId
     *     Unique identifier for the incident.
     * @param type
     *     Type of the incident.
     * @param message
     *     Descriptive message about the incident.
     * @param resolution
     *     Information about resolution steps.
     * @param timestamp
     *     Time when the incident occurred.
     */
    public ProcessingIncident(String incidentId, Date timestamp, ProcessingIncident.Severity severity, ProcessingIncident.Type type, String message, Details details, String correlationId, Resolution resolution) {
        super();
        this.incidentId = incidentId;
        this.timestamp = timestamp;
        this.severity = severity;
        this.type = type;
        this.message = message;
        this.details = details;
        this.correlationId = correlationId;
        this.resolution = resolution;
    }

    /**
     * Unique identifier for the incident
     * (Required)
     * 
     */
    @JsonProperty("incidentId")
    public String getIncidentId() {
        return incidentId;
    }

    /**
     * Unique identifier for the incident
     * (Required)
     * 
     */
    @JsonProperty("incidentId")
    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
    }

    public ProcessingIncident withIncidentId(String incidentId) {
        this.incidentId = incidentId;
        return this;
    }

    /**
     * Time when the incident occurred
     * (Required)
     * 
     */
    @JsonProperty("timestamp")
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Time when the incident occurred
     * (Required)
     * 
     */
    @JsonProperty("timestamp")
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public ProcessingIncident withTimestamp(Date timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * Severity level of the incident
     * (Required)
     * 
     */
    @JsonProperty("severity")
    public ProcessingIncident.Severity getSeverity() {
        return severity;
    }

    /**
     * Severity level of the incident
     * (Required)
     * 
     */
    @JsonProperty("severity")
    public void setSeverity(ProcessingIncident.Severity severity) {
        this.severity = severity;
    }

    public ProcessingIncident withSeverity(ProcessingIncident.Severity severity) {
        this.severity = severity;
        return this;
    }

    /**
     * Type of the incident
     * (Required)
     * 
     */
    @JsonProperty("type")
    public ProcessingIncident.Type getType() {
        return type;
    }

    /**
     * Type of the incident
     * (Required)
     * 
     */
    @JsonProperty("type")
    public void setType(ProcessingIncident.Type type) {
        this.type = type;
    }

    public ProcessingIncident withType(ProcessingIncident.Type type) {
        this.type = type;
        return this;
    }

    /**
     * Descriptive message about the incident
     * (Required)
     * 
     */
    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    /**
     * Descriptive message about the incident
     * (Required)
     * 
     */
    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

    public ProcessingIncident withMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * Detailed information about the incident
     * (Required)
     * 
     */
    @JsonProperty("details")
    public Details getDetails() {
        return details;
    }

    /**
     * Detailed information about the incident
     * (Required)
     * 
     */
    @JsonProperty("details")
    public void setDetails(Details details) {
        this.details = details;
    }

    public ProcessingIncident withDetails(Details details) {
        this.details = details;
        return this;
    }

    /**
     * ID to correlate incidents with processing batches
     * (Required)
     * 
     */
    @JsonProperty("correlationId")
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * ID to correlate incidents with processing batches
     * (Required)
     * 
     */
    @JsonProperty("correlationId")
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public ProcessingIncident withCorrelationId(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    /**
     * Information about resolution steps
     * 
     */
    @JsonProperty("resolution")
    public Resolution getResolution() {
        return resolution;
    }

    /**
     * Information about resolution steps
     * 
     */
    @JsonProperty("resolution")
    public void setResolution(Resolution resolution) {
        this.resolution = resolution;
    }

    public ProcessingIncident withResolution(Resolution resolution) {
        this.resolution = resolution;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ProcessingIncident.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("incidentId");
        sb.append('=');
        sb.append(((this.incidentId == null)?"<null>":this.incidentId));
        sb.append(',');
        sb.append("timestamp");
        sb.append('=');
        sb.append(((this.timestamp == null)?"<null>":this.timestamp));
        sb.append(',');
        sb.append("severity");
        sb.append('=');
        sb.append(((this.severity == null)?"<null>":this.severity));
        sb.append(',');
        sb.append("type");
        sb.append('=');
        sb.append(((this.type == null)?"<null>":this.type));
        sb.append(',');
        sb.append("message");
        sb.append('=');
        sb.append(((this.message == null)?"<null>":this.message));
        sb.append(',');
        sb.append("details");
        sb.append('=');
        sb.append(((this.details == null)?"<null>":this.details));
        sb.append(',');
        sb.append("correlationId");
        sb.append('=');
        sb.append(((this.correlationId == null)?"<null>":this.correlationId));
        sb.append(',');
        sb.append("resolution");
        sb.append('=');
        sb.append(((this.resolution == null)?"<null>":this.resolution));
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
        result = ((result* 31)+((this.severity == null)? 0 :this.severity.hashCode()));
        result = ((result* 31)+((this.details == null)? 0 :this.details.hashCode()));
        result = ((result* 31)+((this.correlationId == null)? 0 :this.correlationId.hashCode()));
        result = ((result* 31)+((this.incidentId == null)? 0 :this.incidentId.hashCode()));
        result = ((result* 31)+((this.type == null)? 0 :this.type.hashCode()));
        result = ((result* 31)+((this.message == null)? 0 :this.message.hashCode()));
        result = ((result* 31)+((this.resolution == null)? 0 :this.resolution.hashCode()));
        result = ((result* 31)+((this.timestamp == null)? 0 :this.timestamp.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ProcessingIncident) == false) {
            return false;
        }
        ProcessingIncident rhs = ((ProcessingIncident) other);
        return (((((((((this.severity == rhs.severity)||((this.severity!= null)&&this.severity.equals(rhs.severity)))&&((this.details == rhs.details)||((this.details!= null)&&this.details.equals(rhs.details))))&&((this.correlationId == rhs.correlationId)||((this.correlationId!= null)&&this.correlationId.equals(rhs.correlationId))))&&((this.incidentId == rhs.incidentId)||((this.incidentId!= null)&&this.incidentId.equals(rhs.incidentId))))&&((this.type == rhs.type)||((this.type!= null)&&this.type.equals(rhs.type))))&&((this.message == rhs.message)||((this.message!= null)&&this.message.equals(rhs.message))))&&((this.resolution == rhs.resolution)||((this.resolution!= null)&&this.resolution.equals(rhs.resolution))))&&((this.timestamp == rhs.timestamp)||((this.timestamp!= null)&&this.timestamp.equals(rhs.timestamp))));
    }


    /**
     * Severity level of the incident
     * 
     */
    @Generated("jsonschema2pojo")
    public enum Severity {

        INFO("INFO"),
        WARNING("WARNING"),
        ERROR("ERROR"),
        CRITICAL("CRITICAL");
        private final String value;
        private final static Map<String, ProcessingIncident.Severity> CONSTANTS = new HashMap<String, ProcessingIncident.Severity>();

        static {
            for (ProcessingIncident.Severity c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        Severity(String value) {
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
        public static ProcessingIncident.Severity fromValue(String value) {
            ProcessingIncident.Severity constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }


    /**
     * Type of the incident
     * 
     */
    @Generated("jsonschema2pojo")
    public enum Type {

        IO_ERROR("IO_ERROR"),
        VALIDATION_ERROR("VALIDATION_ERROR"),
        TRANSFORMATION_ERROR("TRANSFORMATION_ERROR"),
        BUSINESS_LOGIC_ERROR("BUSINESS_LOGIC_ERROR"),
        KAFKA_ERROR("KAFKA_ERROR");
        private final String value;
        private final static Map<String, ProcessingIncident.Type> CONSTANTS = new HashMap<String, ProcessingIncident.Type>();

        static {
            for (ProcessingIncident.Type c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        Type(String value) {
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
        public static ProcessingIncident.Type fromValue(String value) {
            ProcessingIncident.Type constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
