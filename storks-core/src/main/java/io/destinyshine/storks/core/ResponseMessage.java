package io.destinyshine.storks.core;

/**
 * Created by liujianyu.ljy on 17/8/10.
 *
 * @author liujianyu.ljy
 * @date 2017/08/10
 */
public class ResponseMessage {

    private Long traceId;

    private Object returnValue;

    public Long getTraceId() {
        return traceId;
    }

    public void setTraceId(Long traceId) {
        this.traceId = traceId;
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }
}
