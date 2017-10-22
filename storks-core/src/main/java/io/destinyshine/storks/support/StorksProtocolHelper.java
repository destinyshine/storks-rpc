package io.destinyshine.storks.support;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @author destinyliu
 */
public class StorksProtocolHelper {

    public static final int MESSAGE_MAX_LENGTH = Integer.MAX_VALUE;
    public static final int LENGTH_FIELD_SIZE = Integer.BYTES;

    public static LengthFieldBasedFrameDecoder newFrameDecoder() {
        return new LengthFieldBasedFrameDecoder(MESSAGE_MAX_LENGTH, 0, LENGTH_FIELD_SIZE, 0, LENGTH_FIELD_SIZE);
    }

}
