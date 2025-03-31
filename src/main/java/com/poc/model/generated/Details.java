
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
 * Detailed information about the incident
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "filename",
    "fileType",
    "errorLocation",
    "exception"
})
@Generated("jsonschema2pojo")
public class Details {

    /**
     * Name of the file being processed
     * (Required)
     * 
     */
    @JsonProperty("filename")
    @JsonPropertyDescription("Name of the file being processed")
    private String filename;
    /**
     * Type of the file being processed
     * (Required)
     * 
     */
    @JsonProperty("fileType")
    @JsonPropertyDescription("Type of the file being processed")
    private Details.FileType fileType;
    /**
     * Where the error occurred (e.g., line number, field)
     * 
     */
    @JsonProperty("errorLocation")
    @JsonPropertyDescription("Where the error occurred (e.g., line number, field)")
    private String errorLocation;
    /**
     * Exception details if available
     * 
     */
    @JsonProperty("exception")
    @JsonPropertyDescription("Exception details if available")
    private String exception;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Details() {
    }

    /**
     * 
     * @param exception
     *     Exception details if available.
     * @param filename
     *     Name of the file being processed.
     * @param errorLocation
     *     Where the error occurred (e.g., line number, field).
     * @param fileType
     *     Type of the file being processed.
     */
    public Details(String filename, Details.FileType fileType, String errorLocation, String exception) {
        super();
        this.filename = filename;
        this.fileType = fileType;
        this.errorLocation = errorLocation;
        this.exception = exception;
    }

    /**
     * Name of the file being processed
     * (Required)
     * 
     */
    @JsonProperty("filename")
    public String getFilename() {
        return filename;
    }

    /**
     * Name of the file being processed
     * (Required)
     * 
     */
    @JsonProperty("filename")
    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Details withFilename(String filename) {
        this.filename = filename;
        return this;
    }

    /**
     * Type of the file being processed
     * (Required)
     * 
     */
    @JsonProperty("fileType")
    public Details.FileType getFileType() {
        return fileType;
    }

    /**
     * Type of the file being processed
     * (Required)
     * 
     */
    @JsonProperty("fileType")
    public void setFileType(Details.FileType fileType) {
        this.fileType = fileType;
    }

    public Details withFileType(Details.FileType fileType) {
        this.fileType = fileType;
        return this;
    }

    /**
     * Where the error occurred (e.g., line number, field)
     * 
     */
    @JsonProperty("errorLocation")
    public String getErrorLocation() {
        return errorLocation;
    }

    /**
     * Where the error occurred (e.g., line number, field)
     * 
     */
    @JsonProperty("errorLocation")
    public void setErrorLocation(String errorLocation) {
        this.errorLocation = errorLocation;
    }

    public Details withErrorLocation(String errorLocation) {
        this.errorLocation = errorLocation;
        return this;
    }

    /**
     * Exception details if available
     * 
     */
    @JsonProperty("exception")
    public String getException() {
        return exception;
    }

    /**
     * Exception details if available
     * 
     */
    @JsonProperty("exception")
    public void setException(String exception) {
        this.exception = exception;
    }

    public Details withException(String exception) {
        this.exception = exception;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Details.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("filename");
        sb.append('=');
        sb.append(((this.filename == null)?"<null>":this.filename));
        sb.append(',');
        sb.append("fileType");
        sb.append('=');
        sb.append(((this.fileType == null)?"<null>":this.fileType));
        sb.append(',');
        sb.append("errorLocation");
        sb.append('=');
        sb.append(((this.errorLocation == null)?"<null>":this.errorLocation));
        sb.append(',');
        sb.append("exception");
        sb.append('=');
        sb.append(((this.exception == null)?"<null>":this.exception));
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
        result = ((result* 31)+((this.exception == null)? 0 :this.exception.hashCode()));
        result = ((result* 31)+((this.filename == null)? 0 :this.filename.hashCode()));
        result = ((result* 31)+((this.errorLocation == null)? 0 :this.errorLocation.hashCode()));
        result = ((result* 31)+((this.fileType == null)? 0 :this.fileType.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Details) == false) {
            return false;
        }
        Details rhs = ((Details) other);
        return (((((this.exception == rhs.exception)||((this.exception!= null)&&this.exception.equals(rhs.exception)))&&((this.filename == rhs.filename)||((this.filename!= null)&&this.filename.equals(rhs.filename))))&&((this.errorLocation == rhs.errorLocation)||((this.errorLocation!= null)&&this.errorLocation.equals(rhs.errorLocation))))&&((this.fileType == rhs.fileType)||((this.fileType!= null)&&this.fileType.equals(rhs.fileType))));
    }


    /**
     * Type of the file being processed
     * 
     */
    @Generated("jsonschema2pojo")
    public enum FileType {

        EXTERNAL("EXTERNAL"),
        ISIN("ISIN"),
        INTERNAL("INTERNAL");
        private final String value;
        private final static Map<String, Details.FileType> CONSTANTS = new HashMap<String, Details.FileType>();

        static {
            for (Details.FileType c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        FileType(String value) {
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
        public static Details.FileType fromValue(String value) {
            Details.FileType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
