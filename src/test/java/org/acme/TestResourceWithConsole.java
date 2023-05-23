package org.acme;

import java.util.Map;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.redpanda.RedpandaContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import org.testcontainers.containers.wait.strategy.Wait;

import com.github.dockerjava.api.command.InspectContainerResponse;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class TestResourceWithConsole implements QuarkusTestResourceLifecycleManager {

    
    private static DockerImageName REDPANDA_IMAGE = DockerImageName
            .parse("docker.redpanda.com/redpandadata/redpanda:v23.1.10");
    private static DockerImageName REDPANDA_CONSOLE_IMAGE = DockerImageName
            .parse("docker.redpanda.com/redpandadata/console:v2.2.4");

    private static  Network redpandanetwork = Network.newNetwork();
                    
    @Container
    private static final RedpandaContainer redpanda = new RedpandaContainer(REDPANDA_IMAGE) {
        protected void containerIsStarting(InspectContainerResponse containerInfo) {
                    String command = "#!/bin/bash\n";
                    command = command + " /usr/bin/rpk redpanda start --mode dev-container --overprovisioned --smp=1";
                    command = command + " --kafka-addr INTERNAL://redpanda:19092,PLAINTEXT://0.0.0.0:9092";
                    command = command + " --advertise-kafka-addr INTERNAL://redpanda:19092,PLAINTEXT://" + this.getHost() + ":"+ this.getMappedPort(9092);
                    this.copyFileToContainer(Transferable.of(command, 511), "/testcontainers_start.sh");
                }
            }.withNetwork(redpandanetwork)
            .withNetworkAliases("redpanda")
            .withExposedPorts( 9092)
            .withEnv("redpanda.auto_create_topics_enabled", "true")
            .withEnv("group_initial_rebalance_delay", "true");

    @Container
    private static final GenericContainer<?> console = new GenericContainer<>(REDPANDA_CONSOLE_IMAGE)
        .withNetwork(redpandanetwork)
        .withNetworkAliases("redpanda")
        .waitingFor(Wait.forListeningPort())
        .withExposedPorts(8080)
        .withEnv("CONFIG_FILEPATH", "/tmp/config.yml")
        .withCopyFileToContainer(MountableFile.forClasspathResource("console-config.yml"), "/tmp/config.yml")
        .dependsOn(redpanda)
        ;

    @Override
    public Map<String, String> start() {
        redpanda.start();
        console.start();
        return Map.of("kafka.bootstrap.servers", redpanda.getBootstrapServers());
    }

    @Override
    public void stop() {
        if (redpanda != null) {
            redpanda.close();
        }
        if(console != null){
            console.close();
        }
        System.clearProperty("boostrap.servers");
    }


    
    public static String getBootstrapServers() {
        return "PLAINTEXT://"+redpanda.getHost()+":"+redpanda.getMappedPort(9092);

    }

}
