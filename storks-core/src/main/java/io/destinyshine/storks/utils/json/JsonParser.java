package io.destinyshine.storks.utils.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;

/**
 * @author liujianyu
 * @date 2017/10/17
 */
@Slf4j
public class JsonParser {

    private final String json;

    public static Object parse(String json) {
        return new JsonParser(json).parse();
    }

    public JsonParser(String json) {
        this.json = json;
    }

    /**
     * 入口方法
     * @return 解析完成的对象
     */
    public Object parse() {
        CharsRange trimmedJson = newRange(0, json.length()).trim();
        return processValue(trimmedJson);
    }

    private Object processPlainObject(CharsRange range) {
        List<Property> properties = processProperties(newRange(range.start + 1, range.end - 1));
        Map<String, Object> object = new HashMap<>();
        properties.forEach(prop -> object.put(prop.name, prop.value));
        return object;
    }

    private List<Property> processProperties(CharsRange range) {
        List<Property> properties = new ArrayList<>();
        int nameStartMark = range.start;
        for (int i = range.start; i < range.end; i++) {
            char ch = json.charAt(i);
            if (ch == ':') {
                CharsRange nameToken = newRange(nameStartMark, i).trim();
                AtomicInteger readCursor = new AtomicInteger();
                CharsRange valueSegment = findNextValue(newRange(++i, range.end), readCursor);
                i = readCursor.intValue() + 1;
                nameStartMark = i;
                logger.debug("nameToken:{},\nvalueSegment:{}", nameToken, valueSegment);
                //TODO::valid nameToken is start and end with '"'
                final String name = newRange(nameToken.start + 1, nameToken.end - 1).toString();
                final Object value = processValue(valueSegment);
                properties.add(Property.of(name, value));
            }
        }
        return properties;
    }

    private List<?> processArray(CharsRange range) {
        return processElements(newRange(range.start + 1, range.end - 1));
    }

    private List<?> processElements(CharsRange range) {
        List<Object> array = new ArrayList<>();
        int elementStartMark = range.start;
        for (int i = range.start; i < range.end; i++) {
            AtomicInteger readCursor = new AtomicInteger();
            CharsRange elementSegment = findNextValue(newRange(elementStartMark, range.end), readCursor);
            Object elementValue = processValue(elementSegment);
            array.add(elementValue);
            i = readCursor.intValue();
            elementStartMark = i + 1;
        }
        return array;
    }

    /**
     * @param chars
     * @return value segment trimmed.
     */
    private CharsRange findNextValue(CharsRange chars, AtomicInteger readCursor) {
        CharsRange trimChars = chars.trimLeft();
        if (trimChars.relativeChar(0) == '{') {
            return completeSymbolPair(trimChars, readCursor, "{}");
        } else if (trimChars.relativeChar(0) == '[') {
            return completeSymbolPair(trimChars, readCursor, "[]");
        } else {
            int i;
            for (i = trimChars.start + 1; i < trimChars.end; i++) {
                char ch = json.charAt(i);
                if (ch == ',') {
                    break;
                }
            }
            readCursor.set(i);
            return newRange(trimChars.start, i).trim();
        }
    }

    private CharsRange completeSymbolPair(CharsRange trimChars, AtomicInteger readCursor, String symbolPair) {
        int leftSymbol = symbolPair.charAt(0);
        int rightSymbol = symbolPair.charAt(1);
        int symbolsScore = 1;
        //nested object
        int i;
        CharsRange valueSegment = null;
        for (i = trimChars.start + 1; i < trimChars.end; i++) {
            char ch = json.charAt(i);
            if (ch == leftSymbol) {
                symbolsScore++;
            } else if (ch == rightSymbol) {
                symbolsScore--;
            }
            if (symbolsScore == 0) {
                valueSegment = newRange(trimChars.start, i + 1);
                break;
            }
        }

        for (; i < trimChars.end; i++) {
            char chx = json.charAt(i);
            if (chx == ',') {
                break;
            }
        }

        readCursor.set(i);
        return valueSegment;
    }

    private Object processValue(CharsRange valueSegment) {
        final Object value;
        if (valueSegment.relativeChar(0) == '"') {
            value = newRange(valueSegment.start + 1, valueSegment.end - 1).toString();
        } else if (valueSegment.relativeChar(0) == '{') {
            value = processPlainObject(valueSegment);
        } else if (valueSegment.relativeChar(0) == '[') {
            value = processArray(valueSegment);
        } else if (valueSegment.equalsString("true")) {
            value = true;
        } else if (valueSegment.equalsString("false")) {
            value = false;
        } else if (valueSegment.equalsString("null")) {
            value = null;
        } else {
            value = Double.parseDouble(valueSegment.toString());
        }
        return value;
    }

    static class Property {
        final String name;
        final Object value;

        Property(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        static Property of(String name, Object value) {
            return new Property(name, value);
        }
    }

    CharsRange newRange(int start, int end) {
        return new CharsRange(start, end);
    }

    class CharsRange {
        final int start;
        final int end;

        CharsRange(int start, int end) {
            this.start = start;
            this.end = end;
        }

        CharsRange trimLeft() {
            int newStart = -1;
            for (int i = start; i < end; i++) {
                if (!Character.isWhitespace(json.charAt(i))) {
                    newStart = i;
                    break;
                }
            }

            if (newStart == -1) {
                throw new IllegalArgumentException("illegal blank string!");
            }
            return newRange(newStart, end);
        }

        CharsRange trimRight() {
            int newEnd = -1;

            for (int i = end - 1; i >= start; i--) {
                if (!Character.isWhitespace(json.charAt(i))) {
                    newEnd = i + 1;
                    break;
                }
            }
            if (newEnd == -1) {
                throw new IllegalArgumentException("illegal blank string!");
            }
            return newRange(start, newEnd);
        }

        CharsRange trim() {
            return this.trimLeft().trimRight();
        }

        char relativeChar(int index) {
            return json.charAt(start + index);
        }

        public boolean equalsString(String str) {
            return json.regionMatches(true, start, str, 0, str.length());
        }

        @Override
        public String toString() {
            return json.subSequence(start, end).toString();
        }
    }

}
