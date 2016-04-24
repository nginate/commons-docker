package com.github.nginate.commons.docker.wrapper;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;
import com.github.nginate.commons.docker.client.DockerClient;
import com.github.nginate.commons.docker.client.options.RemoveContainerOptions;
import com.github.nginate.commons.lang.function.unchecked.RuntimeIOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

import static com.github.nginate.commons.lang.await.Await.waitUntil;
import static com.github.nginate.commons.lang.function.NFunctions.unchecked;
import static com.google.common.base.Charsets.UTF_8;
import static java.nio.file.Files.newBufferedWriter;

@Slf4j
@RequiredArgsConstructor
public class DockerContainer {

    private final DockerClient dockerClient;
    private final String containerId;

    public void start() {
        dockerClient.startContainer(containerId);
    }

    public void stop() {
        dockerClient.stopContainer(containerId);
    }

    public void kill() {
        dockerClient.killContainer(containerId);
    }

    public void remove() {
        RemoveContainerOptions options = RemoveContainerOptions.builder().force(true).removeVolume(true).build();
        dockerClient.removeContainer(containerId, options);
    }

    public InspectContainerResponse inspect() {
        return dockerClient.inspectContainer(containerId);
    }

    public String getName() {
        return inspect().getName();
    }

    public boolean isRunning() {
        return inspect().getState().isRunning();
    }

    public void printLogs() {
        String name = getName();
        log.info("\n\nContainer logs start for {} \n\n", name);
        dockerClient.logContainer(containerId, log::info);
        log.info("\n\nContainer logs end for {} \n\n", name);
    }

    public void storeLogs(File outputFile) {
        try (BufferedWriter writer = newBufferedWriter(outputFile.toPath(), UTF_8)) {
            consumeLogs(unchecked(writer::write, RuntimeIOException::new));
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    public void consumeLogs(Consumer<String> consumer) {
        dockerClient.logContainer(containerId, consumer);
    }

    public String getIp() {
        return inspect().getNetworkSettings().getIpAddress();
    }

    /**
     * Iterates over all ports that are published from container to docker host, and finds external (published) port
     * for a given internal (container) port.
     *
     * @param servicePort port that is listened inside container
     * @return optional external port
     */
    public Optional<Integer> findPortBinding(String servicePort) {
        Ports ports = inspect().getNetworkSettings().getPorts();
        if (ports != null) {
            Ports.Binding[] hostBindings = ports.getBindings().get(ExposedPort.parse(servicePort));
            return Optional.of(hostBindings[0].getHostPort());
        }
        return Optional.empty();
    }

    public void awaitStarted() {
        waitUntil(10000, 1000, this::isRunning);
    }

    public void awaitStopped() {
        waitUntil(10000, 1000, () -> !isRunning());
    }
}
