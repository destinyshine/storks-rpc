package io.destinyshine.storks.registry.consul;

/**
 * @author destinyliu
 */
public class ConsulConstants {

    public static final String TAG_PREFIX_APP = "app:";

    /**
     * consul block 查询时 block的最长时间,单位，分钟
     */
    public static int CONSUL_BLOCK_TIME_MINUTES = 10;

    /**
     * consul block 查询时 block的最长时间,单位，秒
     */
    public static long CONSUL_BLOCK_TIME_SECONDS = CONSUL_BLOCK_TIME_MINUTES * 60;
}
