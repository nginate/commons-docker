package com.github.nginate.commons.docker;

import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.jaxrs.DockerCmdExecFactoryImpl;
import com.github.nginate.commons.docker.client.NDockerClient;
import com.github.nginate.commons.docker.client.options.CreateContainerOptions;
import com.github.nginate.commons.docker.client.options.ListContainersOptions;
import com.github.nginate.commons.docker.client.options.RemoveContainerOptions;
import com.github.nginate.commons.docker.wrapper.DockerContainer;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Optional;

@UtilityClass
@Slf4j
public class DockerUtils {

    public static DockerContainer wrapContainer(NDockerClient client, String containerId) {
        return new DockerContainer(client, containerId);
    }

    public static Optional<DockerContainer> findContainer(NDockerClient client, String containerName) {
        ListContainersOptions listContainersOptions = ListContainersOptions.builder().showAll(true).build();

        return client.listContainers(listContainersOptions).stream()
                .filter(container -> ArrayUtils.contains(container.getNames(), "/" + containerName))
                .map(container -> new DockerContainer(client, container.getId()))
                .findAny();
    }

    public static DockerContainer createContainer(NDockerClient client, CreateContainerOptions options) {
        return new DockerContainer(client, client.createContainer(options));
    }

    public static DockerContainer forceCreateContainer(NDockerClient client, CreateContainerOptions options) {
        ListContainersOptions listContainersOptions = ListContainersOptions.builder().showAll(true).build();

        client.listContainers(listContainersOptions).stream()
                .filter(container -> ArrayUtils.contains(container.getNames(), "/" + options.getName()))
                .forEach(container -> {
                    log.warn("Container with name {} already exists [{}]. Removing it",
                            options.getName(), container.getId());

                    RemoveContainerOptions removeOptions = RemoveContainerOptions.builder()
                            .force(true)
                            .removeVolume(true)
                            .build();
                    client.removeContainer(container.getId(), removeOptions);
                });

        return createContainer(client, options);
    }

    public static NDockerClient createClient(String serverUrl) {
        DockerClientImpl client = DockerClientImpl.getInstance(serverUrl)
                .withDockerCmdExecFactory(new DockerCmdExecFactoryImpl());
        return new NDockerClient(client);
    }
}
