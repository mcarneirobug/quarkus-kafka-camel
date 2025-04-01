-- External data table
CREATE TABLE IF NOT EXISTS external_data (
    id UUID PRIMARY KEY,
    batch_id VARCHAR(100) NOT NULL,
    external_id VARCHAR(100) NOT NULL,
    external_name VARCHAR(255) NOT NULL,
    external_value DECIMAL(19,2) NOT NULL,
    correlation_key VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    file_name VARCHAR(255) NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE
);

-- ISIN data table
CREATE TABLE IF NOT EXISTS isin_data (
    id UUID PRIMARY KEY,
    batch_id VARCHAR(100) NOT NULL,
    isin VARCHAR(100) NOT NULL,
    isin_description VARCHAR(255) NOT NULL,
    isin_category VARCHAR(100) NOT NULL,
    correlation_key VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    file_name VARCHAR(255) NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE
);

-- Internal data table
CREATE TABLE IF NOT EXISTS internal_data (
    id UUID PRIMARY KEY,
    batch_id VARCHAR(100) NOT NULL,
    internal_id VARCHAR(100) NOT NULL,
    internal_code VARCHAR(100) NOT NULL,
    internal_amount DECIMAL(19,2) NOT NULL,
    correlation_key VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    file_name VARCHAR(255) NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE
);

-- Batch processing table
CREATE TABLE IF NOT EXISTS batch_processing (
    batch_id VARCHAR(100) PRIMARY KEY,
    has_external BOOLEAN NOT NULL DEFAULT FALSE,
    has_isin BOOLEAN NOT NULL DEFAULT FALSE,
    has_internal BOOLEAN NOT NULL DEFAULT FALSE,
    ready_for_processing BOOLEAN NOT NULL DEFAULT FALSE,
    processing_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Aggregated events table
CREATE TABLE IF NOT EXISTS aggregated_events (
    id UUID PRIMARY KEY,
    batch_id VARCHAR(100) NOT NULL,
    correlation_key VARCHAR(100) NOT NULL,
    event_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_to_kafka BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at TIMESTAMP
);