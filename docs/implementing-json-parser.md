>自己实现json parser，只有一个类，不依赖任何第三方工具。

## 背景

为什么要实现json解析器呢？在我实现一个rpc框架的过程中，注册中心部分使用consul，而consul的api是通过restful http api来提供的，数据交互格式为json，此时就需要用到json解析工具。

让我们回顾一下java界较为通用的json处理库，常用的json处理库有jackson，gson，fastjson，其他还有许多json工具包，不过都不流行或已退出历史舞台。

java ee也有一个相关的jsr，jsr 353 json processing api，定义了通用的json 处理 api，一个参考实现是oracle 的glassfish jsonp。


jackson是最完善的json处理工具，实现的功能最多，并且支持jaxb注解，而且也有支持适配 jsr353 json processing api的模块。。也是我个人最喜欢的json处理工具包。其实fastjson的很多实现的常量定义都能看到jaxkson的影子。

本来决定使用jackson的，一般来讲现在spring是企业应用的标配，而spring mvc应用通常都会依赖jackson的包。因此依赖一下jackson的包也可以接受。但后来又考虑了几次，觉得依赖第三方的包毕竟不美，额外带来依赖总是会增加复杂性，对于基础组件，除了日志门面框架这样与实现无关的包，还是尽量少依赖其他库为妙。

因此决定自己写一个json解析工具，考虑了一下觉得也不是十分复杂，用递归的方式解析json串即可。具体实现思路接下来分析。

## 实现思路
### json的结构分析
json的结构包含几种元素：

* object（name value pair object），此处指狭义的对象，名值对形式。广义上任何元素都是对象。
* array，由[]符号包裹，元素用英文逗号分隔。
* 字面类型，字面类型最为简单，不能再嵌套
	* number
	* boolean，true or false
	* null
	
下面放几张[json.org](http://json.org)的图，以直观的形式展示json格式。

![json object](http://json.org/object.gif)


![json object](http://json.org/array.gif)


![json object](http://json.org/value.gif)

object 内部的value和array内部的元素都可以是任意组成类型，可以存在任意层次的嵌套。因此用递归方式解析比较简单。

### 实现思路
#### a) 基本概念
* <span style="color:red">trim</span> ，`trim`是把一个字符序列的头尾的不可见字符去掉，由于json允许在元素之间存在任意个tab、换行、空格。因此可能有许多地方需要用到`trim`。


#### b) processObjcet: object解析
1. 以"`{`"开始，正确找到对应的结束的"`}`"，由于花括号可能存在多重嵌套，找到正确的结束符号是有技巧的。记下整个{}区块的位置。
2. 脱去头尾的`{}`，中间的部分是properties列表，以`name:value,...`的形式存储。解析properties列表。
3. 标记`nameStartMark`，初始为0，遇到冒号"`:`"，从`nameStartMark`到冒号前都为`nameToken`（需要trim），从冒号后开始寻找`nextValue`。同样需要注意(1.)提及的花括号和中括号匹配，遇到逗号或结束表示value区块结束。（由findNextValue函数完成）
4. 移动游标到找到的value区块后，并更新`nameStartMark`标记。
5. 循环执行(3.)和(4.)，直到不再有冒号。
6. <span style="color:red">注意：</span>找到的value区块移交给另一个函数`processValue`处理，此处存在递归。

#### c) processArray: array解析
与object类似，但是比object简单

1. 以"`[`"开始，正确找到对应的结束的"`]`"，由于方括号可能存在多重嵌套，找到正确的结束符号是有技巧的。记下整个`[]`区块的位置。
2. 脱去头尾的`[]`，中间的部分是elements列表，以`element1,element2...`的形式存储。
3. 直接循环执行findNextValue即可，直到结束
6. <span style="color:red">注意：</span>同上，找到的value区块移交给另一个函数`processValue`处理，此处存在递归。

#### d) processValue: value解析
此处是一个递归操作，value本身可能是一个字面量，或者是object，或者是array。

1. 如果value区块以"`{`"开头，则是object，移交给`processObjcet` 做object解析，递归操作。
2. 如果value区块以"`[`"开头，则是array，移交给`processArray` 做array解析，递归操作。
3. 字面量，如string，boolean，number，null直接解析。*string可能有转义字符，这个目前没有考虑处理。*

#### e) completeSymbolPair: 寻找匹配的`{}`和`[]`

由于`{}`和`[]`都可能存在多重嵌套，因此需要正确的找到一个开始的花括号对应的结束符号，方括号同理。

这个可以用这个原理：**符号一定是成对出现的。**

步骤如下：

1. 对于已知的第一个`左符号`，定义`symbolsScore=1`，`index=1`，
2. 遍历后续的字符，遇到`左符号`则`symbolsScore++`，遇到`右符号`则`symbolsScore--`。
3. 直到symbolsScore==0，则找到正确的结束符。
4. 左边`开始符号`到右边`结束符号`之间的内容就是需要的内容。

## 代码实现

### 方法原型

```java
public class JsonParser {

    private final String json;

    /**
     * 入口方法
     * @return 解析完成的对象
     */
    public Object parse() {
        CharsRange trimmedJson = newRange(0, json.length()).trim();
        return processValue(trimmedJson);
    }

    private Object processPlainObject(CharsRange range) {}

    private List<Property> processProperties(CharsRange range) {}

    private List<?> processArray(CharsRange range) {}

    /**
     * @param chars
     * @return value segment trimmed.
     */
    private CharsRange findNextValue(CharsRange chars, AtomicInteger readCursor) {}

    private CharsRange completeSymbolPair(CharsRange trimChars, AtomicInteger readCursor, String symbolPair) {}

    private Object processValue(CharsRange valueSegment) {}

    static class Property { final String name, value;}

    class CharsRange { final int start, end;}

}


```

### 具体代码
此处当然要放具体的代码，只有一个类。

```java
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
                logger.info("nameToken:{},\nvalueSegment:{}", nameToken, valueSegment);
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

```

## 功能测试

### junit test
最后当然要做测试，不过我们这个东西是个简单的小东西，暂时不做性能测试，测试一下功能即可。
注意：所有用到的资源都在附件里，下载可直接使用。

```java
package jsonparse;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import io.destinyshine.storks.utils.json.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

/**
 * @author liujianyu
 * @date 2017/10/17
 */
@Slf4j
public class JsonParserTest {

    @Test
    public void parseComplexObject() throws IOException, URISyntaxException {
        String json = readFile("/json/nested.json");
        logger.info("origin json content:{}", json);
        JsonParser parser = new JsonParser(json);
        Object object = parser.parse();
        logger.info("parsed object:{}", object);
    }

    @Test
    public void parseEmptyObject() throws IOException, URISyntaxException {
        String json = readFile("/json/empty.json");
        logger.info("origin json content:{}", json);
        JsonParser parser = new JsonParser(json);
        Object object = parser.parse();
        logger.info("parsed object:{}", object);
    }

    @Test
    public void parseArray() throws IOException, URISyntaxException {
        String json = readFile("/json/array.json");
        logger.info("origin json content:{}", json);
        JsonParser parser = new JsonParser(json);
        Object object = parser.parse();
        logger.info("parsed object:{}", object);
    }

    private String readFile(String resource) throws URISyntaxException, IOException {
        return FileUtils.readFileToString(
            new File(JsonParserTest.class.getResource(resource).toURI()));
    }

}

```

### 测试结果

```
[main] INFO jsonparse.JsonParserTest - parsed array:[{area=12.0, color=green, shape=circle}, {nested={area=12.0, color=green, shape=circle}}]
[main] INFO jsonparse.JsonParserTest - parsed emptyObject:{}
[main] INFO jsonparse.JsonParserTest - parsed complexObject:{parent={address=null, array=[1.0, 3.0, {}], name=jerry, adult=true, age=45.4}, name=tom, adult=false, age=5.0}

```
## 结尾
任何功能，简单的实现总是很容易，但是要做到工程级别总是很复杂，一个完整的JSON解析程序会包含更多的特性，比如注解支持、容错性、语法错误提示等。因此我们写这个东西只是自我学习一下，如果真的追求性能和各种特性的支持，还是要用成熟的工具包。

还有，我们的程序没有处理转义字符，不过这个处理倒不是很复杂。


