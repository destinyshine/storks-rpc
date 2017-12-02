package io.destinyshine.storks.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by liujianyu.ljy on 17/8/10.
 *
 * @author liujianyu
 * @date 2017/08/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseMessage {

    private Long traceId;

    private Object returnValue;

    private Throwable exception;

}
