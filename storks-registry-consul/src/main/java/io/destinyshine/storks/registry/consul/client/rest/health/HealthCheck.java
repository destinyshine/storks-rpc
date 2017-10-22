package io.destinyshine.storks.registry.consul.client.rest.health;

import lombok.Data;

/**
 * @author Vasily Vasilkov (vgv@ecwid.com)
 */
@Data
public class HealthCheck {

    public static enum CheckStatus {
        unknown,
        passing,
        warning,
        critical
    }

    private String node;

    private String checkId;

    private String name;

    private CheckStatus status;

    private String notes;

    private String output;

    private String serviceID;

    private String serviceName;

}
