
package com.poc.model.generated;

import java.util.Date;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * AggregatedEvent
 * <p>
 * Schema for the aggregated event from multiple CSV files
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "eventId",
    "timestamp",
    "externalData",
    "isinData",
    "internalData",
    "correlationId",
    "processingInfo"
})
@Generated("jsonschema2pojo")
public class AggregatedEvent {

    /**
     * Unique identifier for the event
     * (Required)
     * 
     */
    @JsonProperty("eventId")
    @JsonPropertyDescription("Unique identifier for the event")
    private String eventId;
    /**
     * Time when the event was processed
     * (Required)
     * 
     */
    @JsonProperty("timestamp")
    @JsonPropertyDescription("Time when the event was processed")
    private Date timestamp;
    /**
     * Data from the external file
     * 
     */
    @JsonProperty("externalData")
    @JsonPropertyDescription("Data from the external file")
    private ExternalData externalData;
    /**
     * Data from the ISIN file
     * 
     */
    @JsonProperty("isinData")
    @JsonPropertyDescription("Data from the ISIN file")
    private IsinData isinData;
    /**
     * Data from the internal file
     * 
     */
    @JsonProperty("internalData")
    @JsonPropertyDescription("Data from the internal file")
    private InternalData internalData;
    /**
     * ID used to correlate records from different files
     * (Required)
     * 
     */
    @JsonProperty("correlationId")
    @JsonPropertyDescription("ID used to correlate records from different files")
    private String correlationId;
    /**
     * Meta information about the processing
     * (Required)
     * 
     */
    @JsonProperty("processingInfo")
    @JsonPropertyDescription("Meta information about the processing")
    private ProcessingInfo processingInfo;

    /**
     * No args constructor for use in serialization
     * 
     */
    public AggregatedEvent() {
    }

    /**
     * 
     * @param eventId
     *     Unique identifier for the event.
     * @param internalData
     *     Data from the internal file.
     * @param processingInfo
     *     Meta information about the processing.
     * @param externalData
     *     Data from the external file.
     * @param isinData
     *     Data from the ISIN file.
     * @param correlationId
     *     ID used to correlate records from different files.
     * @param timestamp
     *     Time when the event was processed.
     */
    public AggregatedEvent(String eventId, Date timestamp, ExternalData externalData, IsinData isinData, InternalData internalData, String correlationId, ProcessingInfo processingInfo) {
        super();
        this.eventId = eventId;
        this.timestamp = timestamp;
        this.externalData = externalData;
        this.isinData = isinData;
        this.internalData = internalData;
        this.correlationId = correlationId;
        this.processingInfo = processingInfo;
    }

    /**
     * Unique identifier for the event
     * (Required)
     * 
     */
    @JsonProperty("eventId")
    public String getEventId() {
        return eventId;
    }

    /**
     * Unique identifier for the event
     * (Required)
     * 
     */
    @JsonProperty("eventId")
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public AggregatedEvent withEventId(String eventId) {
        this.eventId = eventId;
        return this;
    }

    /**
     * Time when the event was processed
     * (Required)
     * 
     */
    @JsonProperty("timestamp")
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Time when the event was processed
     * (Required)
     * 
     */
    @JsonProperty("timestamp")
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public AggregatedEvent withTimestamp(Date timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * Data from the external file
     * 
     */
    @JsonProperty("externalData")
    public ExternalData getExternalData() {
        return externalData;
    }

    /**
     * Data from the external file
     * 
     */
    @JsonProperty("externalData")
    public void setExternalData(ExternalData externalData) {
        this.externalData = externalData;
    }

    public AggregatedEvent withExternalData(ExternalData externalData) {
        this.externalData = externalData;
        return this;
    }

    /**
     * Data from the ISIN file
     * 
     */
    @JsonProperty("isinData")
    public IsinData getIsinData() {
        return isinData;
    }

    /**
     * Data from the ISIN file
     * 
     */
    @JsonProperty("isinData")
    public void setIsinData(IsinData isinData) {
        this.isinData = isinData;
    }

    public AggregatedEvent withIsinData(IsinData isinData) {
        this.isinData = isinData;
        return this;
    }

    /**
     * Data from the internal file
     * 
     */
    @JsonProperty("internalData")
    public InternalData getInternalData() {
        return internalData;
    }

    /**
     * Data from the internal file
     * 
     */
    @JsonProperty("internalData")
    public void setInternalData(InternalData internalData) {
        this.internalData = internalData;
    }

    public AggregatedEvent withInternalData(InternalData internalData) {
        this.internalData = internalData;
        return this;
    }

    /**
     * ID used to correlate records from different files
     * (Required)
     * 
     */
    @JsonProperty("correlationId")
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * ID used to correlate records from different files
     * (Required)
     * 
     */
    @JsonProperty("correlationId")
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public AggregatedEvent withCorrelationId(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    /**
     * Meta information about the processing
     * (Required)
     * 
     */
    @JsonProperty("processingInfo")
    public ProcessingInfo getProcessingInfo() {
        return processingInfo;
    }

    /**
     * Meta information about the processing
     * (Required)
     * 
     */
    @JsonProperty("processingInfo")
    public void setProcessingInfo(ProcessingInfo processingInfo) {
        this.processingInfo = processingInfo;
    }

    public AggregatedEvent withProcessingInfo(ProcessingInfo processingInfo) {
        this.processingInfo = processingInfo;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(AggregatedEvent.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("eventId");
        sb.append('=');
        sb.append(((this.eventId == null)?"<null>":this.eventId));
        sb.append(',');
        sb.append("timestamp");
        sb.append('=');
        sb.append(((this.timestamp == null)?"<null>":this.timestamp));
        sb.append(',');
        sb.append("externalData");
        sb.append('=');
        sb.append(((this.externalData == null)?"<null>":this.externalData));
        sb.append(',');
        sb.append("isinData");
        sb.append('=');
        sb.append(((this.isinData == null)?"<null>":this.isinData));
        sb.append(',');
        sb.append("internalData");
        sb.append('=');
        sb.append(((this.internalData == null)?"<null>":this.internalData));
        sb.append(',');
        sb.append("correlationId");
        sb.append('=');
        sb.append(((this.correlationId == null)?"<null>":this.correlationId));
        sb.append(',');
        sb.append("processingInfo");
        sb.append('=');
        sb.append(((this.processingInfo == null)?"<null>":this.processingInfo));
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
        result = ((result* 31)+((this.eventId == null)? 0 :this.eventId.hashCode()));
        result = ((result* 31)+((this.internalData == null)? 0 :this.internalData.hashCode()));
        result = ((result* 31)+((this.processingInfo == null)? 0 :this.processingInfo.hashCode()));
        result = ((result* 31)+((this.externalData == null)? 0 :this.externalData.hashCode()));
        result = ((result* 31)+((this.isinData == null)? 0 :this.isinData.hashCode()));
        result = ((result* 31)+((this.correlationId == null)? 0 :this.correlationId.hashCode()));
        result = ((result* 31)+((this.timestamp == null)? 0 :this.timestamp.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AggregatedEvent) == false) {
            return false;
        }
        AggregatedEvent rhs = ((AggregatedEvent) other);
        return ((((((((this.eventId == rhs.eventId)||((this.eventId!= null)&&this.eventId.equals(rhs.eventId)))&&((this.internalData == rhs.internalData)||((this.internalData!= null)&&this.internalData.equals(rhs.internalData))))&&((this.processingInfo == rhs.processingInfo)||((this.processingInfo!= null)&&this.processingInfo.equals(rhs.processingInfo))))&&((this.externalData == rhs.externalData)||((this.externalData!= null)&&this.externalData.equals(rhs.externalData))))&&((this.isinData == rhs.isinData)||((this.isinData!= null)&&this.isinData.equals(rhs.isinData))))&&((this.correlationId == rhs.correlationId)||((this.correlationId!= null)&&this.correlationId.equals(rhs.correlationId))))&&((this.timestamp == rhs.timestamp)||((this.timestamp!= null)&&this.timestamp.equals(rhs.timestamp))));
    }

}
