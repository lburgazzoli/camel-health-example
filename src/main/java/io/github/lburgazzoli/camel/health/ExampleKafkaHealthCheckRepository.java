package io.github.lburgazzoli.camel.health;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.DeferredContextBinding;
import org.apache.camel.ExtendedCamelContext;
import org.apache.camel.NonManagedService;
import org.apache.camel.StaticService;
import org.apache.camel.health.HealthCheck;
import org.apache.camel.health.HealthCheckConfiguration;
import org.apache.camel.health.HealthCheckRegistry;
import org.apache.camel.health.HealthCheckRepository;
import org.apache.camel.health.HealthCheckResolver;
import org.apache.camel.support.service.ServiceSupport;

/**
 * Repository for camel-kafka {@link HealthCheck}s.
 */
@org.apache.camel.spi.annotations.HealthCheck(ExampleKafkaHealthCheckRepository.REPO_NAME)
@DeferredContextBinding
public class ExampleKafkaHealthCheckRepository extends ServiceSupport
    implements CamelContextAware, HealthCheckRepository, StaticService, NonManagedService {

    public static final String REPO_NAME = "example-camel-kafka-repository";
    public static final String REPO_ID = "example-camel-kafka";

    private final Map<String, HealthCheck> checks = new ConcurrentHashMap<>();
    private volatile CamelContext context;
    private boolean enabled = true;

    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.context = camelContext;
    }

    @Override
    public String getId() {
        return REPO_ID;
    }

    @Override
    public CamelContext getCamelContext() {
        return context;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public Stream<HealthCheck> stream() {
        return this.context != null && enabled
            ? checks.values().stream()
            : Stream.empty();
    }

    public void addHealthCheck(HealthCheck healthCheck) {
        CamelContextAware.trySetCamelContext(healthCheck, getCamelContext());
        this.checks.put(healthCheck.getId(), healthCheck);
    }

    public void removeHealthCheck(String id) {
        this.checks.remove(id);
    }

    @Override
    public void setConfigurations(Map<String, HealthCheckConfiguration> configurations) {
        // unsupported
    }

    @Override
    public Map<String, HealthCheckConfiguration> getConfigurations() {
        // unsupported
        return Map.of();
    }

    @Override
    public void addConfiguration(String id, HealthCheckConfiguration configuration) {
        // unsupported
    }

    public static ExampleKafkaHealthCheckRepository get(CamelContext context) {
        ExampleKafkaHealthCheckRepository answer = null;

        HealthCheckRegistry hcr = context.getExtension(HealthCheckRegistry.class);
        if (hcr != null && hcr.isEnabled()) {
            Optional<HealthCheckRepository> repo = hcr.getRepository(REPO_ID);
            if (repo.isEmpty()) {
                // use resolver to load from classpath if needed
                HealthCheckResolver resolver
                    = context.adapt(ExtendedCamelContext.class).getHealthCheckResolver();

                HealthCheckRepository hr = resolver.resolveHealthCheckRepository(REPO_ID);
                if (hr != null) {
                    repo = Optional.of(hr).filter(ExampleKafkaHealthCheckRepository.class::isInstance);
                    hcr.register(hr);
                }
            }
            if (repo.isPresent()) {
                answer = repo.map(ExampleKafkaHealthCheckRepository.class::cast).get();
            }
        }

        return answer;
    }
}
