package io.destinyshine.storks.registry.consul.client.rest.health;

import java.util.List;

import lombok.Data;
import lombok.ToString;

/**
 * @author Vasily Vasilkov (vgv@ecwid.com)
 */
@Data
public class HealthService {

    @Data
    @ToString
    public static class Node {
        private String node;

        private String address;

    }

    @Data
    @ToString
    public static class Service {
        private String id;

        private String service;

        private List<String> tags;

        private String address;

        private Integer port;

    }

    private Node node;

    private Service service;

    private List<HealthCheck> checks;

}
