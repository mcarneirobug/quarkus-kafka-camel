-- External data indexes
CREATE INDEX IF NOT EXISTS idx_external_batch_id ON external_data(batch_id);
CREATE INDEX IF NOT EXISTS idx_external_correlation_key ON external_data(correlation_key);
CREATE INDEX IF NOT EXISTS idx_external_processed ON external_data(processed);

-- ISIN data indexes
CREATE INDEX IF NOT EXISTS idx_isin_batch_id ON isin_data(batch_id);
CREATE INDEX IF NOT EXISTS idx_isin_correlation_key ON isin_data(correlation_key);
CREATE INDEX IF NOT EXISTS idx_isin_processed ON isin_data(processed);
CREATE INDEX IF NOT EXISTS idx_isin_isin ON isin_data(isin);

-- Internal data indexes
CREATE INDEX IF NOT EXISTS idx_internal_batch_id ON internal_data(batch_id);
CREATE INDEX IF NOT EXISTS idx_internal_correlation_key ON internal_data(correlation_key);
CREATE INDEX IF NOT EXISTS idx_internal_processed ON internal_data(processed);

-- Batch processing indexes
CREATE INDEX IF NOT EXISTS idx_batch_processing_status ON batch_processing(processing_status);
CREATE INDEX IF NOT EXISTS idx_batch_ready ON batch_processing(ready_for_processing);

-- Aggregated events indexes
CREATE INDEX IF NOT EXISTS idx_aggregated_batch_id ON aggregated_events(batch_id);
CREATE INDEX IF NOT EXISTS idx_aggregated_correlation_key ON aggregated_events(correlation_key);
CREATE INDEX IF NOT EXISTS idx_aggregated_sent ON aggregated_events(sent_to_kafka);