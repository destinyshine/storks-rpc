package io.destinyshine.storks.spring.boot;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static io.destinyshine.storks.registry.consul.ConsulRegistry.DEFAULT_HEALTH_CHECK_INTERVAL;

/**
 * @author liujianyu
 */
@ConfigurationProperties(prefix = StorksProperties.PROP_PREFIX)
@Getter
@Setter
public class StorksProperties {

    public static final String PROP_PREFIX = "storks";

    private String appName;
    private RegistryProperties registry = new RegistryProperties();
    private ProviderProperties provider = new ProviderProperties();

    private boolean providerEnabled = true;
    private boolean consumerEnabled = true;

    @Getter
    @Setter
    public static class ProviderProperties {
        private String defaultVersion = "1.0.0";
        private int servicePort = 0;
        private String serviceHost;
    }

    @Getter
    @Setter
    public static class RegistryProperties {

        private ConsulProperties consul = new ConsulProperties();
    }

    @Getter
    @Setter
    public static class ConsulProperties {
        private String host = "127.0.0.1";
        private int port = 8500;
        private int healthCheckIntervalSeconds = DEFAULT_HEALTH_CHECK_INTERVAL;
    }
}
