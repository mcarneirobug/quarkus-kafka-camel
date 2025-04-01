package com.poc.exception;

public class KafkaException extends RuntimeException {

    private final String topic;

    public KafkaException(String message) {
        super(message);
        this.topic = null;
    }

    public KafkaException(String message, Throwable cause) {
        super(message, cause);
        this.topic = null;
    }

    public KafkaException(String message, String topic) {
        super(message);
        this.topic = topic;
    }

    public KafkaException(String message, Throwable cause, String topic) {
        super(message, cause);
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }
}