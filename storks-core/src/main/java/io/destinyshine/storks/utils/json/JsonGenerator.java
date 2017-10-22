package io.destinyshine.storks.utils.json;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * @author liujianyu
 */
@Slf4j
public class JsonGenerator {
    private final Object object;

    private final StringBuilder json = new StringBuilder();

    boolean generated = false;

    public JsonGenerator(Object object) {
        this.object = object;
    }

    public static String generate(Object object) {
        return new JsonGenerator(object).generate();
    }

    public synchronized String generate() {
        if (!generated) {
            processValue(object);
        }
        return json.toString();
    }

    public void processValue(Object value) {
        if (value == null) {
            processNull();
        } else if (value instanceof CharSequence) {
            processString((CharSequence)value);
        } else if (value instanceof Map) {
            processMap((Map<?, ?>)value);
        } else if (value instanceof Collection) {
            processCollection((Collection<?>)value);
        } else if (value.getClass().isArray()) {
            processArray(value);
        } else if (value.getClass().isEnum()) {
            processEnum(value);
        } else if (value.getClass().isPrimitive() || value instanceof Number) {
            processPrimitive(value);
        } else {
            processBean(value);
        }
    }

    private void processNull() {
        json.append("null");
    }

    private void processString(CharSequence chars) {
        json.append('"').append(chars).append('"');
    }

    private void processBean(Object bean) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass(), Object.class);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            json.append('{');
            for (PropertyDescriptor desc : propertyDescriptors) {
                String name = desc.getName();
                Object value = desc.getReadMethod().invoke(bean);
                //
                processString(name);
                json.append(':');
                processValue(value);
                json.append(',');
            }
            json.setCharAt(json.length() - 1, '}');
        } catch (Throwable e) {
            logger.error("error, type={}, bean={}", bean.getClass(), bean, e);
            throw new RuntimeException(e);
        }
    }

    private void processMap(Map<?, ?> map) {
        json.append('{');
        map.forEach((key, value) -> {
            processValue(key.toString());
            json.append(':');
            processValue(value);
            json.append(',');
        });
        json.setCharAt(json.length() - 1, ']');
    }

    private void processCollection(Collection<?> collection) {
        json.append('[');
        collection.forEach(element -> {
            processValue(element);
            json.append(',');
        });
        json.setCharAt(json.length() - 1, ']');
    }

    private void processArray(Object array) {
        json.append('[');
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            Object element = Array.get(array, i);
            processValue(element);
            json.append(',');
        }
        json.setCharAt(json.length() - 1, ']');
    }

    private void processEnum(Object value) {
        json.append('"').append(value.toString()).append('"');
    }

    private void processPrimitive(Object primitive) {
        json.append(String.valueOf(primitive));
    }
}
