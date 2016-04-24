package com.github.nginate.commons.docker.client;

import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.api.model.VolumesFrom;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.nginate.commons.docker.client.options.CreateContainerOptions;
import com.github.nginate.commons.docker.client.options.KillContainerOptions;
import com.github.nginate.commons.docker.client.options.ListContainersOptions;
import com.github.nginate.commons.docker.client.options.RemoveContainerOptions;
import com.github.nginate.commons.docker.wrapper.DockerLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.github.nginate.commons.lang.NCollections.mapToArray;
import static org.apache.commons.lang.ArrayUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotBlank;

@Slf4j
@RequiredArgsConstructor
public class DockerClient {

    private final com.github.dockerjava.api.DockerClient client;

    public void pullImage(String repository) {
        client.pullImageCmd(repository)
                .exec(new PullImageResultCallback())
                .awaitSuccess();
    }

    public List<Container> listContainers() {
        return client.listContainersCmd().exec();
    }

    public List<Container> listContainers(ListContainersOptions options) {
        ListContainersCmd cmd = client.listContainersCmd();
        if (options.getShowAll() != null) {
            cmd.withShowAll(options.getShowAll());
        }
        if (options.getLimit() != null) {
            cmd.withLimit(options.getLimit());
        }
        if (options.getSince() != null) {
            cmd.withSince(options.getSince());
        }
        if (options.getBefore() != null) {
            cmd.withBefore(options.getBefore());
        }
        if (options.getSize() != null) {
            cmd.withShowSize(options.getSize());
        }
        if (options.getFilters() != null) {
            cmd.withFilters(options.getFilters());
        }
        return cmd.exec();
    }

    public String createContainer(CreateContainerOptions options) {
        CreateContainerResponse response = toCreateContainerCmd(client, options).exec();
        if (response.getWarnings() != null) {
            for (String warning : response.getWarnings()) {
                log.warn(warning);
            }
        }
        return response.getId();
    }

    public void startContainer(String containerId) {
        client.startContainerCmd(containerId).exec();
    }

    public void stopContainer(String containerId) {
        client.stopContainerCmd(containerId).exec();
    }

    public void killContainer(String containerId) {
        client.killContainerCmd(containerId).exec();
    }

    public void killContainer(String containerId, KillContainerOptions options) {
        KillContainerCmd cmd = client.killContainerCmd(containerId);
        if (options.getSignal() != null) {
            cmd.withSignal(options.getSignal());
        }
        cmd.exec();
    }

    public void removeContainer(String containerId) {
        removeContainer(containerId, RemoveContainerOptions.builder().build());
    }

    public void removeContainer(String containerId, RemoveContainerOptions options) {
        client.removeContainerCmd(containerId)
                .withRemoveVolumes(options.isRemoveVolume())
                .withForce(options.isForce())
                .exec();
    }

    public InspectContainerResponse inspectContainer(String containerId) {
        return client.inspectContainerCmd(containerId).exec();
    }

    public void logContainer(String containerId, Consumer<String> logConsumer) {
        client.logContainerCmd(containerId)
                .withStdOut()
                .withStdErr()
                .exec(new DockerLogger(logConsumer))
                .awaitCompletion();
    }

    public static CreateContainerCmd toCreateContainerCmd(com.github.dockerjava.api.DockerClient dockerClient,
            CreateContainerOptions config) {
        CreateContainerCmd createContainerCmd =
                dockerClient.createContainerCmd(config.getImage())
                        .withAttachStderr(config.isAttachStderr())
                        .withAttachStdin(config.isAttachStdin())
                        .withAttachStdout(config.isAttachStdout())
                        .withCpuShares(config.getCpuShares())
                        .withExposedPorts(config.getExposedPorts()
                                .toArray(new ExposedPort[config.getExposedPorts().size()]))
                        .withLogConfig(config.getLogConfig())
                        .withMemoryLimit(config.getMemoryLimit())
                        .withMemorySwap(config.getMemorySwap())
                        .withNetworkDisabled(config.isNetworkDisabled())
                        .withPrivileged(config.isPrivileged())
                        .withPublishAllPorts(config.isPublishAllPorts())
                        .withReadonlyRootfs(config.isReadonlyRootfs())
                        .withRestartPolicy(config.getRestartPolicy())
                        .withStdInOnce(config.isStdInOnce())
                        .withStdinOpen(config.isStdinOpen())
                        .withTty(config.isTty());
        if (!isEmpty(config.getBinds())) {
            createContainerCmd.withBinds(config.getBinds());
        }
        Optional.ofNullable(config.getBlkioWeight()).ifPresent(createContainerCmd::withBlkioWeight);
        if (!isEmpty(config.getCapAdd())) {
            createContainerCmd.withCapAdd(config.getCapAdd());
        }
        if (!isEmpty(config.getCapDrop())) {
            createContainerCmd.withCapDrop(config.getCapDrop());
        }
        if (!isEmpty(config.getCmd())) {
            createContainerCmd.withCmd(config.getCmd());
        }
        Optional.ofNullable(config.getContainerIDFile()).ifPresent(createContainerCmd::withContainerIDFile);
        Optional.ofNullable(config.getCpuPeriod()).ifPresent(createContainerCmd::withCpuPeriod);
        Optional.ofNullable(config.getCpuset()).ifPresent(createContainerCmd::withCpuset);
        Optional.ofNullable(config.getCpusetMems()).ifPresent(createContainerCmd::withCpusetMems);
        if (!isEmpty(config.getDevices())) {
            createContainerCmd.withDevices(config.getDevices());
        }
        if (!isEmpty(config.getDns())) {
            createContainerCmd.withDns(config.getDns());
        }
        if (!isEmpty(config.getDnsSearch())) {
            createContainerCmd.withDnsSearch(config.getDnsSearch());
        }
        if (!isEmpty(config.getEntrypoint())) {
            createContainerCmd.withEntrypoint(config.getEntrypoint());
        }
        if (!MapUtils.isEmpty(config.getEnv())) {
            String[] envArray = mapToArray(config.getEnv(), String[]::new, (k, v) -> k + "=" + v);
            createContainerCmd.withEnv(envArray);
        }
        if (!isEmpty(config.getExtraHosts())) {
            createContainerCmd.withExtraHosts(config.getExtraHosts());
        }
        if (!MapUtils.isEmpty(config.getLabels())) {
            createContainerCmd.withLabels(config.getLabels());
        }
        if (!isEmpty(config.getLinks())) {
            createContainerCmd.withLinks(config.getLinks());
        }
        if (!isEmpty(config.getLxcConf())) {
            createContainerCmd.withLxcConf(config.getLxcConf());
        }
        if (isNotBlank(config.getMacAddress())) {
            createContainerCmd.withMacAddress(config.getMacAddress());
        }
        if (config.getOomKillDisable() != null) {
            createContainerCmd.withOomKillDisable(config.getOomKillDisable());
        }
        if (isNotBlank(config.getPidMode())) {
            createContainerCmd.withPidMode(config.getPidMode());
        }
        if (!isEmpty(config.getPortBindings())) {
            createContainerCmd.withPortBindings(config.getPortBindings());
        }
        if (!isEmpty(config.getPortSpecs())) {
            createContainerCmd.withPortSpecs(config.getPortSpecs());
        }
        if (!isEmpty(config.getUlimits())) {
            createContainerCmd.withUlimits(config.getUlimits());
        }
        if (!CollectionUtils.isEmpty(config.getVolumes())) {
            createContainerCmd.withVolumes(mapToArray(config.getVolumes(), Volume[]::new));
        }
        if (!CollectionUtils.isEmpty(config.getVolumesFrom())) {
            createContainerCmd.withVolumesFrom(mapToArray(config.getVolumesFrom(), VolumesFrom[]::new));
        }
        if (isNotBlank(config.getDomainName())) {
            createContainerCmd.withDomainName(config.getDomainName());
        }
        if (isNotBlank(config.getHostName())) {
            createContainerCmd.withHostName(config.getHostName());
        }
        if (isNotBlank(config.getName())) {
            createContainerCmd.withName(config.getName());
        }
        if (isNotBlank(config.getUser())) {
            createContainerCmd.withUser(config.getUser());
        }
        if (isNotBlank(config.getWorkingDir())) {
            createContainerCmd.withWorkingDir(config.getWorkingDir());
        }
        return createContainerCmd;
    }
}
