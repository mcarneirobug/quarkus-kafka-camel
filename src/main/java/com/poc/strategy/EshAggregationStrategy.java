package com.poc.strategy;

import com.poc.model.EshEvent;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;

public class EshAggregationStrategy implements AggregationStrategy {

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        EshEvent event = oldExchange == null
                ? new EshEvent()
                : oldExchange.getIn().getBody(EshEvent.class);

        String[] line = newExchange.getIn().getBody(String[].class);
        String fileName = newExchange.getIn().getHeader("CamelFileName", String.class);

        event.correlationId = line[1];

        if (fileName.contains("csv1")) {
            event.name = line[0];
            event.value1 = line[2];
        } else if (fileName.contains("csv2")) {
            event.label = line[0];
            event.value2 = line[2];
        } else if (fileName.contains("csv3")) {
            event.tag = line[0];
            event.value3 = line[2];
        }

        newExchange.getIn().setBody(event);
        return newExchange;
    }
}