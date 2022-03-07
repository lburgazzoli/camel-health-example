package io.github.lburgazzoli.camel.health;

import javax.enterprise.context.ApplicationScoped;

import org.apache.camel.builder.endpoint.EndpointRouteBuilder;

@ApplicationScoped
public class ExampleRoute extends EndpointRouteBuilder {
    @Override
    public void configure() throws Exception {
        //from("timer:tick")
        //    .routeId("main")
        //    .process(e -> { throw new RuntimeCamelException(new UnsupportedOperationException("foo operation")); })
        //    .errorHandler(deadLetterChannel("rc:fail?routeId=current"))
        //    .to("log:info?showAll=true");


        /*
        camel.component.kc.brokers           = ${kafka.server}
        camel.component.kc.securityProtocol  = SASL_SSL
        camel.component.kc.saslMechanism     = PLAIN
        camel.component.kc.saslJaasConfig    = org.apache.kafka.common.security.plain.PlainLoginModule required username='${kafka.client.id}' password='${kafka.client.secret}';
        camel.component.kc.keySerializer     = org.apache.kafka.common.serialization.ByteArraySerializer
        camel.component.kc.valueSerializer   = org.apache.kafka.common.serialization.ByteArraySerializer
        camel.component.kc.keyDeserializer   = org.apache.kafka.common.serialization.ByteArrayDeserializer
        camel.component.kc.valueDeserializer = org.apache.kafka.common.serialization.ByteArrayDeserializer
        */

        var kc = kafka("kc", "foo")
            .brokers("{{kafka.server}}")
            .securityProtocol("SASL_SSL")
            .saslMechanism("PLAIN")
            .saslJaasConfig("org.apache.kafka.common.security.plain.PlainLoginModule required username='{{kafka.client.id}}' password='{{kafka.client.secret}}';")
            .valueDeserializer("org.apache.kafka.common.serialization.ByteArrayDeserializer")
            .keyDeserializer("org.apache.kafka.common.serialization.ByteArrayDeserializer");


        from(kc)
            .routeId("kafka")
            .to("log:info?showAll=true");
    }
}
