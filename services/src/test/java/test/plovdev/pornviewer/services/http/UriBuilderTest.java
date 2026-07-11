package test.plovdev.pornviewer.services.http;

import com.plovdev.pornviewer.services.http.UriBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;

public class UriBuilderTest {
    @Test
    void testBuilder() {
        String baseUrl = "https://example.com";
        UriBuilder builder = new UriBuilder(baseUrl);
        builder.appendUriPart("/plugins");

        URI builded = builder.build();
        Assertions.assertEquals(URI.create("https://example.com/plugins"), builded);
    }
}