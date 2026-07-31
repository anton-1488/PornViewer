package test.plovdev.pornviewer.core.http;

import com.plovdev.pornviewer.core.http.HttpMethod;
import com.plovdev.pornviewer.core.http.HttpUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class TestHttpUtils {
    @Test
    void testParseResponseBodyForGet() {
        String uri = "https://example.com/watch/video?filename=93hf8dmoi3149fumdw4&encryptmode=decrypt";
        Map<String, Object> parsed = HttpUtils.parseResponseBody(HttpMethod.GET, uri);

        Assertions.assertTrue(parsed.containsKey("filename"));
        Assertions.assertTrue(parsed.containsKey("encryptmode"));
    }

    @Test
    void testParseResponseBodyForPost() {
        String params = """
                {
                    "filename": "93hf8dmoi3149fumdw4",
                    "encryptmode": "decrypt"
                }
                """;

        Map<String, Object> parsed = HttpUtils.parseResponseBody(HttpMethod.POST, params);

        Assertions.assertTrue(parsed.containsKey("filename"));
        Assertions.assertTrue(parsed.containsKey("encryptmode"));
    }
}