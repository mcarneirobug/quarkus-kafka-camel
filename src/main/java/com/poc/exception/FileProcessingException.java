package com.poc.exception;

import com.poc.model.generated.ProcessingIncident;

public class FileProcessingException extends RuntimeException {

    private final ProcessingIncident incident;

    public FileProcessingException(String message, ProcessingIncident incident) {
        super(message);
        this.incident = incident;
    }

    public FileProcessingException(String message, Throwable cause, ProcessingIncident incident) {
        super(message, cause);
        this.incident = incident;
    }

    public ProcessingIncident getIncident() {
        return incident;
    }
}