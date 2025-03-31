package com.poc.exception;

import com.poc.model.generated.ProcessingIncident;

public class BusinessLogicException extends FileProcessingException {

    public BusinessLogicException(String message, ProcessingIncident incident) {
        super(message, incident);
    }

    public BusinessLogicException(String message, Throwable cause, ProcessingIncident incident) {
        super(message, cause, incident);
    }
}