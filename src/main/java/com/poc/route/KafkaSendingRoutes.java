package com.poc.route;

import com.poc.usecase.KafkaEmitterUseCase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.camel.builder.RouteBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.List;

import static org.apache.camel.LoggingLevel.INFO;

@ApplicationScoped
public class KafkaSendingRoutes extends RouteBuilder {

    protected static final Logger LOG = Logger.getLogger(KafkaSendingRoutes.class);

    private final KafkaEmitterUseCase kafkaEmitterUseCase;

    @ConfigProperty(name = "app.output.kafka.topic", defaultValue = "aggregated-events")
    String kafkaTopic;

    @ConfigProperty(name = "kafka.bootstrap.servers", defaultValue = "localhost:9092")
    String kafkaBootstrapServers;

    @Inject
    public KafkaSendingRoutes(KafkaEmitterUseCase kafkaEmitterUseCase) {
        this.kafkaEmitterUseCase = kafkaEmitterUseCase;
    }

    @Override
    public void configure() throws Exception {
        // Route to send events to Kafka using Camel Kafka component
        from("direct:sendToCamelKafka")
                .id("camelKafkaRoute")
                .log(INFO, "Sending events to Kafka using Camel component: ${header.eventCount} events")
                .to("kafka:" + kafkaTopic + "?brokers=" + kafkaBootstrapServers)
                .log(INFO, "Events successfully sent to topic " + kafkaTopic);

        // Route to send events to Kafka using SmallRye Reactive Messaging Emitter
        from("direct:sendToEmitter")
                .id("emitterKafkaRoute")
                .log(INFO, "Sending events to Kafka using Emitter: ${header.eventCount} events")
                .process(exchange -> {
                    @SuppressWarnings("unchecked")
                    List<String> events = exchange.getIn().getBody(List.class);
                    String correlationId = exchange.getIn().getHeader("correlationId", String.class);

                    // This presumably calls your KafkaEmitterUseCase
                    // Blocks until sending is completed
                    kafkaEmitterUseCase.sendEvents(events, correlationId)
                            .toCompletableFuture()
                            .join();
                })
                .log(INFO, "Events successfully sent to topic " + kafkaTopic);
    }
}