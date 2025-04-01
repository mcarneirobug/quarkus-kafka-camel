package com.poc.enums;

/**
 * Enum defining error types for better type safety and consistency
 */
public enum ErrorType {
    IO_ERROR,
    VALIDATION_ERROR,
    TRANSFORMATION_ERROR,
    BUSINESS_LOGIC_ERROR,
    KAFKA_ERROR,
    APP_ERROR,
    UNKNOWN_ERROR
}