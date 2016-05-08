package com.github.nginate.commons.docker.client;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Wither;

@Value
@Builder(toBuilder = true)
@Wither
public class DockerClientOptions {
    private String dockerUri;
    private String dockerCertsPath;
    private Integer readTimeout;
    private Integer connectionTimeout;
    private Integer maxRouteConnections;
}
