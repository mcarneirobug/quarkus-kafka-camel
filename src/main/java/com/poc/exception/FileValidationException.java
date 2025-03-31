package com.poc.exception;

import com.poc.model.generated.ProcessingIncident;

public class FileValidationException extends FileProcessingException {

    public FileValidationException(String message, ProcessingIncident incident) {
        super(message, incident);
    }

    public FileValidationException(String message, Throwable cause, ProcessingIncident incident) {
        super(message, cause, incident);
    }
}