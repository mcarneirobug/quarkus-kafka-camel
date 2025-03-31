package com.poc.exception;

import com.poc.model.generated.ProcessingIncident;

public class FileIOException extends FileProcessingException {

    public FileIOException(String message, ProcessingIncident incident) {
        super(message, incident);
    }

    public FileIOException(String message, Throwable cause, ProcessingIncident incident) {
        super(message, cause, incident);
    }
}