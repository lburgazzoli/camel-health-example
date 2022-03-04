package io.github.lburgazzoli.camel.health;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.apache.kafka.clients.producer.Producer;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class ExampleKafkaProducersHealthCheck implements HealthCheck {
    public static final String NAME = "camel-kafka-producers-readiness-checks";

    private final Map<String, Holder> clients = new ConcurrentHashMap<>();

    @Inject
    CamelContext context;

    @Override
    public HealthCheckResponse call() {
        return  HealthCheckResponse.named(NAME).up().build();
    }

    public void track(String uri, Producer client, Properties configuration) {
        clients.put(uri, new Holder(client, configuration));
    }

    private static class Holder {
        private final Producer client;
        private final Properties configuration;

        public Holder(Producer client, Properties configuration) {
            this.client = client;
            this.configuration = configuration;
        }
    }
}