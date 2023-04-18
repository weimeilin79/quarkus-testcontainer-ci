package org.acme;

import java.util.Map;

import org.testcontainers.images.builder.Transferable;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.redpanda.RedpandaContainer;
import org.testcontainers.utility.DockerImageName;

import com.github.dockerjava.api.command.InspectContainerResponse;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class TestResource implements QuarkusTestResourceLifecycleManager {

    private static DockerImageName REDPANDA_IMAGE = DockerImageName
            .parse("docker.redpanda.com/vectorized/redpanda:latest");

    @Container
    private static final RedpandaContainer redpanda = new RedpandaContainer(REDPANDA_IMAGE) {
        protected void containerIsStarting(InspectContainerResponse containerInfo) {
            String command = "#!/bin/bash\n";
            command = command
                    + "/usr/bin/rpk redpanda start --mode dev-container --overprovisioned --smp=1 --memory=2G ";
            command = command + "--kafka-addr PLAINTEXT://0.0.0.0:29092,OUTSIDE://0.0.0.0:9092 ";
            command = command + "--advertise-kafka-addr PLAINTEXT://127.0.0.1:29092,OUTSIDE://" + this.getHost() + ":"
                    + this.getMappedPort(9092);
            this.copyFileToContainer(Transferable.of(command, 511), "/testcontainers_start.sh");
        }
    }.withCreateContainerCmdModifier(cmd -> {
        cmd.getHostConfig()
                .withCpuCount(2l);
    })
            .withEnv("redpanda.auto_create_topics_enabled", "true")
            .withEnv("group_initial_rebalance_delay", "true");

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
