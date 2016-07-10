package com.github.nginate.commons.docker.client.options;

import com.github.dockerjava.api.model.*;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;
import java.util.Map;

@Data
@Builder(toBuilder = true)
public class CreateContainerOptions {
    private String name;
    private String hostName;
    private String domainName;
    private String user;
    private long memoryLimit;
    private long memorySwap;
    private int cpuShares;
    private Integer cpuPeriod;
    private String cpusetCpus;
    private String cpusetMems;
    private boolean attachStdin;
    private boolean attachStdout;
    private boolean attachStderr;
    private String[] portSpecs;
    private boolean tty;
    private boolean stdinOpen;
    private boolean stdInOnce;
    @Singular("env")
    private Map<String, String> env;
    private String[] cmd;
    private String[] entrypoint;
    private String image;
    @Singular("volume")
    private List<Volume> volumes;
    private String workingDir;
    private String macAddress;
    private boolean networkDisabled;
    @Singular("exposedPort")
    private List<ExposedPort> exposedPorts;
    private Map<String, String> labels;
    private Integer blkioWeight;
    private Boolean oomKillDisable;

    private Bind[] binds;
    private Link[] links;
    private LxcConf[] lxcConf;
    private LogConfig logConfig;
    @Singular
    private List<PortBinding> portBindings;
    private boolean publishAllPorts;
    private boolean privileged;
    private boolean readonlyRootfs;
    private String[] dns;
    private String[] dnsSearch;
    @Singular("volumeFrom")
    private List<VolumesFrom> volumesFrom;
    private String containerIDFile;
    private Capability[] capAdd;
    private Capability[] capDrop;
    private RestartPolicy restartPolicy;
    private String networkMode;
    private Device[] devices;
    private String[] extraHosts;
    private Ulimit[] ulimits;
    private String pidMode;

    public static class CreateContainerOptionsBuilder {
        public CreateContainerOptionsBuilder portsBinding(Integer hostPort, Integer exposedPort) {
            return portsBinding(hostPort, ExposedPort.tcp(exposedPort));
        }

        public CreateContainerOptionsBuilder portsBinding(Integer hostPort, ExposedPort exposedPort) {
            return portBinding(new PortBinding(new Ports.Binding(null, String.valueOf(hostPort)), exposedPort));
        }

        public CreateContainerOptionsBuilder portOneToOneBinding(Integer port) {
            return portOneToOneBinding(ExposedPort.tcp(port));
        }

        public CreateContainerOptionsBuilder portOneToOneBinding(ExposedPort port) {
            return portBinding(new PortBinding(new Ports.Binding(null, String.valueOf(port.getPort())), port));
        }
    }
}
