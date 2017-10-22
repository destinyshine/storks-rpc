package io.destinyshine.storks.registry.consul;

import lombok.Data;

/**
 * @author destinyliu
 */
@Data
public class ConsulResponse<T> {
    /**
     * consul返回的具体结果
     */
    private T value;

    private Long consulIndex;

    private Boolean consulKnownLeader;

    private Long consulLastContact;

}
