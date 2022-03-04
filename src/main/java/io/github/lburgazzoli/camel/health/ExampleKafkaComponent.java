package io.github.lburgazzoli.camel.health;

import java.util.Map;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.camel.component.kafka.DefaultKafkaClientFactory;
import org.apache.camel.component.kafka.KafkaClientFactory;
import org.apache.camel.component.kafka.KafkaComponent;
import org.apache.camel.component.kafka.KafkaConfiguration;
import org.apache.camel.component.kafka.KafkaEndpoint;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.Readiness;

@Named("kc")
@ApplicationScoped
public class ExampleKafkaComponent extends KafkaComponent {
    public static final String KAFKA_BYTES_SERIALIZER = "org.apache.kafka.common.serialization.ByteArraySerializer";
    public static final String KAFKA_BYTES_DESERIALIZER = "org.apache.kafka.common.serialization.ByteArrayDeserializer";


    @Readiness
    @Inject
    ExampleKafkaConsumersHealthCheck consumersHealthCheck;

    @Readiness
    @Inject
    ExampleKafkaProducersHealthCheck producersHealthCheck;

    @ConfigProperty(name = "kafka.client.id")
    String kafkaClientId;
    @ConfigProperty(name = "kafka.client.secret")
    String kafkaClientSecret;
    @ConfigProperty(name = "kafka.server")
    String kafkaServer;

    @Override
    public void doInit() throws Exception {
        String KAFKA_JAAS_CONFIG = String.format(
            "org.apache.kafka.common.security.plain.PlainLoginModule required username='%s' password='%s';",
            kafkaClientId,
            kafkaClientSecret);

        getConfiguration().setSecurityProtocol("SASL_SSL");
        getConfiguration().setSaslMechanism("PLAIN");
        getConfiguration().setSaslJaasConfig(KAFKA_JAAS_CONFIG);
        getConfiguration().setKeySerializer(KAFKA_BYTES_SERIALIZER);
        getConfiguration().setValueSerializer(KAFKA_BYTES_SERIALIZER);
        getConfiguration().setKeyDeserializer(KAFKA_BYTES_DESERIALIZER);
        getConfiguration().setValueDeserializer(KAFKA_BYTES_DESERIALIZER);
        getConfiguration().setBrokers(kafkaServer);

        super.doInit();
    }

    @Override
    protected KafkaEndpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        KafkaEndpoint endpoint = super.createEndpoint(uri, remaining, parameters);
        KafkaClientFactory factory = new DefaultKafkaClientFactory();

        endpoint.setKafkaClientFactory(new KafkaClientFactory() {
            @Override
            public Producer getProducer(Properties properties) {
                Producer<?, ?> client = null;

                try {
                    client = factory.getProducer(properties);
                } finally {
                    producersHealthCheck.track(uri, client, properties);
                }

                return client;
            }

            @Override
            public Consumer getConsumer(Properties properties) {
                Consumer<?, ?> client = null;

                try {
                    client = factory.getConsumer(properties);
                } finally {
                    consumersHealthCheck.track(uri, client, properties);
                }

                return client;
            }

            @Override
            public String getBrokers(KafkaConfiguration configuration) {
                return factory.getBrokers(configuration);
            }
        });

        return endpoint;
    }
}
