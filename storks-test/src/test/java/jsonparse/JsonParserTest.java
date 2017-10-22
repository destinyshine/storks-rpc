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
        logger.info("parsed complexObject:{}", object);
    }

    @Test
    public void parseEmptyObject() throws IOException, URISyntaxException {
        String json = readFile("/json/empty.json");
        logger.info("origin json content:{}", json);
        JsonParser parser = new JsonParser(json);
        Object object = parser.parse();
        logger.info("parsed emptyObject:{}", object);
    }

    @Test
    public void parseArray() throws IOException, URISyntaxException {
        String json = readFile("/json/array.json");
        logger.info("origin json content:{}", json);
        JsonParser parser = new JsonParser(json);
        Object object = parser.parse();
        logger.info("parsed array:{}", object);
    }

    private String readFile(String resource) throws URISyntaxException, IOException {
        return FileUtils.readFileToString(
            new File(JsonParserTest.class.getResource(resource).toURI()));
    }

}
