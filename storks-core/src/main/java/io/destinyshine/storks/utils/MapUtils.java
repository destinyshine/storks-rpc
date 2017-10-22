package io.destinyshine.storks.utils;

import java.util.Map;

public class MapUtils {

    public static boolean isNotEmpty(Map<?, ?> headers) {
        return headers != null && !headers.isEmpty();
    }
}
