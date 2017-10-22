package io.destinyshine.storks.support;

import java.util.List;
import java.util.function.Supplier;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.runtime.RuntimeSchema;

/**
 * Created by liujianyu.ljy on 17/8/10.
 *
 * @author liujianyu.ljy
 * @date 2017/08/10
 */
public class ProtostuffDecoder<T> extends ByteToMessageDecoder {

    private RuntimeSchema<T> messageSchema;
    private final Supplier<T> messageSupplier;

    public ProtostuffDecoder(Class<T> messageType, Supplier<T> messageSupplier) {
        this.messageSchema = RuntimeSchema.createFrom(messageType);
        this.messageSupplier = messageSupplier;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
        throws Exception {
        T message = messageSupplier.get();
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);
        ProtostuffIOUtil.mergeFrom(bytes, message, messageSchema);
        out.add(message);
    }
}
