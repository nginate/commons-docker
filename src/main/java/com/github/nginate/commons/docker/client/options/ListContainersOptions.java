package com.github.nginate.commons.docker.client.options;

import com.github.dockerjava.api.model.Filters;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ListContainersOptions {
    private Boolean showAll;
    private Integer limit;
    private String since;
    private String before;
    private Boolean size;
    private Filters filters;
}
