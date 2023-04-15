package org.acme;

import java.util.Map;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.redpanda.RedpandaContainer;

import com.github.dockerjava.api.command.InspectContainerResponse;

import org.testcontainers.images.builder.Transferable; 

public class TestResource implements QuarkusTestResourceLifecycleManager {

    static final RedpandaContainer redpanda = new RedpandaContainer("docker.redpanda.com/vectorized/redpanda:latest"){
        protected void containerIsStarting(InspectContainerResponse containerInfo) {
            String command = "#!/bin/bash\n";
            command = command + "/usr/bin/rpk redpanda start --mode dev-container --overprovisioned  --smp=300m --memory=600M ";
            command = command + "--kafka-addr PLAINTEXT://0.0.0.0:29092,OUTSIDE://0.0.0.0:9092 ";
            command = command + "--advertise-kafka-addr PLAINTEXT://127.0.0.1:29092,OUTSIDE://" + this.getHost() + ":" + this.getMappedPort(9092);
            this.copyFileToContainer(Transferable.of(command, 511), "/testcontainers_start.sh");
        }
    }
      .withCreateContainerCmdModifier(cmd -> {
          cmd.getHostConfig()
            .withCpuCount(2l);
      });


    
    
    //static final  KafkaContainer redpanda =  new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.0.1"));

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
