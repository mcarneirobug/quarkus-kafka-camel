# quarkus-camel-kafka

![img.png](img.png)

**Overview**:

Our system processes three types of CSV files (external, ISIN, and Internal), aggregates the data based on correlation keys (so, we truly needs something to make relation) and sends the results to Kafka.

We've **FileWatcherRoute**: Basically, the purpose is monitors the input directory for new CSV files and routes them to the appropriate processor based on filename pattern. Extracts correlation ID (batch_id) from filenames, based on date and implements file locking to ensure only one thread processes each file. And use file component with idempotent processing to prevent duplicate processing.

**CsvProcessingUseCase**: Purpose to parses and validates CSV content, and creating entities for storage. So, there we can validates headers and file structure, converts CSV rows to domain entities, saves records to the database, updates batch status.

**BatchProcessingRepository**: It's the central component for tracking batch status, so, we can tracks which file types have been received for each batch, determines when a batch is ready for processing, and manages batch state transitions (PENDING -> PROCESSING > AGGREGATED > COMPLETED).

**RouteUtils**: Utility class that checks if a batch is ready for aggregation. So, queries batch status and trigger aggregation when all required files are received.

**BatchAggregationRoute**: Coordinates batch aggregation and Kafka Publishing, and implements a timer that periodically checks for ready batches and makes aggregated data to Kafka publishing.

**AggregationUseCase**: Core business logic for data aggregation, so, retrieve all data for a batch from all three tables, create correlation maps for efficient lookups, combine data from all sources based on correlation keys, and then creates aggregated events and stores them, in the end updates batch status too.

**KafkaPublisherUseCase**: Reliable delivery of aggregated events to Kafka.

**ErrorHandlingRoute**: Centralized error handling for Camel Routes, there we processes exception from all routes, determines appropriate handling based on exception type, moves files to error directory when appropriate and trigger incident creating and monitoring.