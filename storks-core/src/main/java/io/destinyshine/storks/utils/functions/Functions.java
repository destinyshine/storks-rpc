package io.destinyshine.storks.utils.functions;

import java.util.function.Consumer;

public abstract class Functions {

    public static Consumer EMPTY_CONSUMER = arg -> {
    };

    public static <T> Consumer<T> emptyConsumer() {
        return (Consumer<T>)EMPTY_CONSUMER;
    }

}
