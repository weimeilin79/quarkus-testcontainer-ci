package org.acme;

import java.util.Map;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.redpanda.RedpandaContainer;

public class TestResource implements QuarkusTestResourceLifecycleManager {

    static final RedpandaContainer redpanda = new RedpandaContainer("docker.redpanda.com/vectorized/redpanda:v22.2.1");
    
    @Override
    public Map<String, String> start() {
        redpanda.start();
        return Map.of("kafka.bootstrap.servers", redpanda.getBootstrapServers());
    }

    @Override
    public void stop() {
        if (redpanda != null) {
            redpanda.close();
        }
        System.clearProperty("boostrap.servers");
    }

    public static String getBootstrapServers() {
        return redpanda.getBootstrapServers();
        
    }
    
}
