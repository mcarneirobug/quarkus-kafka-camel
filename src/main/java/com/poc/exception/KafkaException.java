package com.poc.exception;

import com.poc.model.generated.ProcessingIncident;

public class KafkaException extends FileProcessingException {

    public KafkaException(String message, ProcessingIncident incident) {
        super(message, incident);
    }

    public KafkaException(String message, Throwable cause, ProcessingIncident incident) {
        super(message, cause, incident);
    }
}