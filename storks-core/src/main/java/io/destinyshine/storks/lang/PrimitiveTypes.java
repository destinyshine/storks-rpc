package io.destinyshine.storks.lang;

import java.util.Optional;

/**
 * @author liujianyu
 */
public abstract class PrimitiveTypes {

    public static final String BOOLEAN_NAME = "boolean";
    public static final String CHAR_NAME = "char";
    public static final String BYTE_NAME = "byte";
    public static final String SHORT_NAME = "short";
    public static final String INT_NAME = "int";
    public static final String LONG_NAME = "long";
    public static final String FLOAT_NAME = "float";
    public static final String DOUBLE_NAME = "double";
    public static final String VOID_NAME = "void";

    /**
     *
     * @param name
     * @return
     */
    private static Class<?> getPrimitiveType0(String name) {
        switch (name) {
            case BOOLEAN_NAME:
                return Boolean.TYPE;
            case CHAR_NAME:
                return Character.TYPE;
            case BYTE_NAME:
                return Byte.TYPE;
            case SHORT_NAME:
                return Short.TYPE;
            case INT_NAME:
                return Integer.TYPE;
            case LONG_NAME:
                return Long.TYPE;
            case FLOAT_NAME:
                return Float.TYPE;
            case DOUBLE_NAME:
                return Double.TYPE;
            case VOID_NAME:
                return Void.TYPE;
            default:
                return null;
        }
    }

    /**
     *
     * @param name
     * @return
     */
    public static Optional<Class<?>> getPrimitiveType(String name) {
        return Optional.ofNullable(getPrimitiveType0(name));
    }

    public static Object getPrimitiveDefaultValue(Class<?> primitiveType) {
        String primitiveTypeName = primitiveType.getName();
        switch (primitiveTypeName) {
            case BOOLEAN_NAME:
                return false;
            case CHAR_NAME:
                return (char)0;
            case BYTE_NAME:
                return (byte)0;
            case SHORT_NAME:
                return (short)0;
            case INT_NAME:
                return 0;
            case LONG_NAME:
                return 0L;
            case FLOAT_NAME:
                return 0F;
            case DOUBLE_NAME:
                return 0D;
            case VOID_NAME:
                return null;
            default:
                return null;
        }
    }
}
