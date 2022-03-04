package io.github.lburgazzoli.camel.health;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;

import org.apache.camel.util.ReflectionHelper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.internals.ConsumerNetworkClient;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class ExampleKafkaConsumersHealthCheck implements HealthCheck {
    public static final String NAME = "camel-kafka-consumers-readiness-checks";
    public static final String DATA_ERROR_MESSAGE = "error.message";

    private final Map<String, Holder> clients = new ConcurrentHashMap<>();

    @Override
    public HealthCheckResponse call() {
        var builder = HealthCheckResponse.named(NAME).up();

        for (Holder holder: clients.values()) {
            if (holder.client == null) {

            }
            try {
                if (holder.client != null) {
                    ConsumerNetworkClient nc = (ConsumerNetworkClient) ReflectionHelper.getField(
                        holder.client.getClass().getDeclaredField("client"),
                        holder.client);

                    if (!nc.hasReadyNodes(System.currentTimeMillis())) {
                        builder.down();
                        builder.withData(DATA_ERROR_MESSAGE, "KafkaConsumer is not ready");
                    }
                } else {
                    builder.down();
                    builder.withData(DATA_ERROR_MESSAGE, "KafkaConsumer has not been created");
                }

                builder.withData("bootstrap.servers", holder.configuration.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));

                String cid = holder.configuration.getProperty(ConsumerConfig.CLIENT_ID_CONFIG);
                if (cid != null) {
                    builder.withData("client.id", cid);
                }
                String gid = holder.configuration.getProperty(ConsumerConfig.GROUP_ID_CONFIG);
                if (gid != null) {
                    builder.withData("group.id", gid);
                }
            } catch (NoSuchFieldException e) {
                builder.up();
            }
        }

        return builder.build();
    }

    public void track(String uri, Consumer client, Properties configuration) {
        clients.put(uri, new Holder(client, configuration));
    }

    private static class Holder {
        private final Consumer client;
        private final Properties configuration;

        public Holder(Consumer client, Properties configuration) {
            this.client = client;
            this.configuration = configuration;
        }
    }
}