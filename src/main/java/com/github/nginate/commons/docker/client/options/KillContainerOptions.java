package com.github.nginate.commons.docker.client.options;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class KillContainerOptions {
    private String signal;
}
