package io.destinyshine.storks.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by liujianyu.ljy on 17/8/10.
 *
 * @author liujianyu.ljy
 * @date 2017/08/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestMessage {

    private Long traceId;
    private String serviceInterface;
    private String serviceVersion;
    private String methodName;
    private String[] parameterTypes;
    private Object[] parameters;

}
