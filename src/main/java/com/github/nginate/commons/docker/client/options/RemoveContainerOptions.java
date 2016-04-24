package com.github.nginate.commons.docker.client.options;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RemoveContainerOptions {
    private boolean force;
    private boolean removeVolume;
}
