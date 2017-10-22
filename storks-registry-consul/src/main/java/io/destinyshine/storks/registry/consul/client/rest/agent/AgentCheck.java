package io.destinyshine.storks.registry.consul.client.rest.agent;

import java.util.Map;

import lombok.Data;

/**
 * @author Vasily Vasilkov (vgv@ecwid.com)
 */
@Data
public class AgentCheck {

    private String name;

    private String id;

    private String interval;

    private String notes;

    private String deregisterCriticalServiceAfter;

    private String script;

    private String dockerContainerID;

    private String shell;

    private String http;

    private String method;

    private Map<String, Object> header;

    private Boolean tlsSkipVerify;

    private String tcp;

    private String ttl;

    private String serviceId;

    private String status;

}
