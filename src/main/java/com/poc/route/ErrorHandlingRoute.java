//package com.poc.route;
//
//import jakarta.enterprise.context.ApplicationScoped;
//import org.apache.camel.Exchange;
//import org.apache.camel.builder.RouteBuilder;
//
//@ApplicationScoped
//public class ErrorHandlingRoute extends RouteBuilder {
//
//    @Override
//    public void configure() throws Exception {
//        from("direct:processError")
//                .id("errorHandlerRoute")
//                .log("Error during processing: ${exception.message}")
//                .process(exchange -> {
//                    // Assuming FileProcessingHelper is available as a bean; otherwise, re-inject the logic.
//                    FileProcessingHelper helper = exchange.getContext().getRegistry().lookupByNameAndType("fileProcessingHelper", FileProcessingHelper.class);
//                    helper.handleError(exchange);
//                })
//                .toD("file:{{app.input.error-directory}}?fileName=${header.CamelFileName}.error");
//    }
//}
