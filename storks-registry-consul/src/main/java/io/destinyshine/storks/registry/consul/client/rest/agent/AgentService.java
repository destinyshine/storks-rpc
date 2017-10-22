package io.destinyshine.storks.registry.consul.client.rest.agent;

import java.util.List;

import lombok.Data;

/**
 * @author Vasily Vasilkov (vgv@ecwid.com)
 * @author Spencer Gibb (spencer@gibb.us)
 */
@Data
public class AgentService {

    private String id;

    private String name;

    private List<String> tags;

    private String address;

    private Integer port;

    private Boolean enableTagOverride;

    private AgentCheck check;

    private List<AgentCheck> checks;

}
