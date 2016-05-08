package com.github.nginate.commons.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.DockerCmdExecFactory;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.jaxrs.DockerCmdExecFactoryImpl;
import com.github.nginate.commons.docker.client.DockerClientOptions;
import com.github.nginate.commons.docker.client.NDockerClient;
import com.github.nginate.commons.docker.client.options.CreateContainerOptions;
import com.github.nginate.commons.docker.client.options.ListContainersOptions;
import com.github.nginate.commons.docker.client.options.RemoveContainerOptions;
import com.github.nginate.commons.docker.wrapper.DockerContainer;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@UtilityClass
@Slf4j
public class DockerUtils {

    private static final String DOCKER_URI = getProperty("docker.uri", "DOCKER_HOST", "http://localhost:2375");
    private static final String DOCKER_CERTS_PATH = getProperty("docker.certs", null);
    private static final int DOCKER_READ_TIMEOUT = Integer.parseInt(getProperty("docker.read.timeout", "10000"));
    private static final int DOCKER_CONNECT_TIMEOUT = Integer.parseInt(getProperty("docker.connect.timeout", "500"));
    private static final int DOCKER_MAX_ROUTE_CONNECTIONS =
            Integer.parseInt(getProperty("docker.max.route.connections", "10"));

    private static String getProperty(String sysPropName, String defaultValue) {
        String envPropName = sysPropName.toUpperCase().replace(".", "_");
        return getProperty(sysPropName, envPropName, defaultValue);
    }

    private static String getProperty(String sysPropName, String envPropName, String defaultValue) {
        return System.getProperty(sysPropName, defaultIfNull(System.getenv(envPropName), defaultValue));
    }

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

    public static NDockerClient createClient() {
        return createClient(DOCKER_URI, DOCKER_CERTS_PATH);
    }

    public static NDockerClient createClient(String serverUrl) {
        return createClient(serverUrl, DOCKER_CERTS_PATH);
    }

    public static NDockerClient createClient(String serverUrl, String dockerCertsPath) {
        DockerClientOptions clientOptions = defaultClientOptions().toBuilder()
                .dockerUri(serverUrl)
                .dockerCertsPath(dockerCertsPath)
                .build();
        return createClient(clientOptions);
    }

    public static DockerClientOptions defaultClientOptions() {
        return DockerClientOptions.builder()
                .dockerUri(DOCKER_URI)
                .dockerCertsPath(DOCKER_CERTS_PATH)
                .readTimeout(DOCKER_READ_TIMEOUT)
                .connectionTimeout(DOCKER_CONNECT_TIMEOUT)
                .maxRouteConnections(DOCKER_MAX_ROUTE_CONNECTIONS)
                .build();
    }

    public static NDockerClient createClient(DockerClientOptions options) {
        DockerClientConfig.DockerClientConfigBuilder configBuilder = DockerClientConfig.createDefaultConfigBuilder()
                .withUri(options.getDockerUri())
                .withDockerCertPath(options.getDockerCertsPath());

        DockerCmdExecFactory dockerCmdExecFactory = new DockerCmdExecFactoryImpl()
                .withConnectTimeout(options.getConnectionTimeout())
                .withReadTimeout(options.getReadTimeout())
                .withMaxPerRouteConnections(options.getMaxRouteConnections());

        DockerClient client = DockerClientBuilder.getInstance(configBuilder)
                .withDockerCmdExecFactory(dockerCmdExecFactory)
                .build();

        return new NDockerClient(client);
    }
}
