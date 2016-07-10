package com.github.nginate.commons.docker.client.options;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
@Builder
public class ListContainersOptions {
    private Boolean showAll;
    private Integer limit;
    private String since;
    private String before;
    private Boolean size;
    private Map<String, List<String>> filters;
}
