package com.poc.exception;

import com.poc.model.generated.ProcessingIncident;

public class FileTransformationException extends FileProcessingException {

    public FileTransformationException(String message, ProcessingIncident incident) {
        super(message, incident);
    }

    public FileTransformationException(String message, Throwable cause, ProcessingIncident incident) {
        super(message, cause, incident);
    }
}