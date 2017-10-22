package io.destinyshine.storks.core;

/**
 * Created by liujianyu.ljy on 17/8/20.
 *
 * @author liujianyu.ljy
 * @date 2017/08/20
 */
public class ApplicationProperties {

    private String appName;

    private int servicePort;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public int getServicePort() {
        return servicePort;
    }

    public void setServicePort(int servicePort) {
        this.servicePort = servicePort;
    }
}
