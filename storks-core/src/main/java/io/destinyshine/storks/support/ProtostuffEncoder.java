package io.destinyshine.storks.support;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.runtime.RuntimeSchema;

/**
 * Created by liujianyu.ljy on 17/8/10.
 *
 * @author liujianyu.ljy
 * @date 2017/08/10
 */
public class ProtostuffEncoder<T> extends MessageToByteEncoder<T> {

    private RuntimeSchema<T> messageSchema;

    public ProtostuffEncoder(Class<T> messageType) {
        super(messageType);
        this.messageSchema = RuntimeSchema.createFrom(messageType);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        T responseMessage = (T) msg;
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        byte[] bytes = ProtostuffIOUtil.toByteArray(responseMessage, messageSchema, buffer);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);

    }

}
