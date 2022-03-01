package io.github.lburgazzoli.camel.health;

import javax.enterprise.context.ApplicationScoped;

import org.apache.camel.RuntimeCamelException;
import org.apache.camel.builder.RouteBuilder;

@ApplicationScoped
public class ExampleRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("timer:tick")
            .routeId("main")
            .process(e -> { throw new RuntimeCamelException(new UnsupportedOperationException("foo operation")); })
            .errorHandler(deadLetterChannel("rc:fail?routeId=current"))
            .to("log:info?showAll=true");
    }
}
