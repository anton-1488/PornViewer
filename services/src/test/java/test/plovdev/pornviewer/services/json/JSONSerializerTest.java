package test.plovdev.pornviewer.services.json;

import com.plovdev.pornviewer.services.json.JSONSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JSONSerializerTest {
    @Test
    void testSerialization() {
        Json json = new Json("Hello");
        String serialized = JSONSerializer.serialize(json);
        Json deseralizted = JSONSerializer.deserialize(serialized, Json.class);

        Assertions.assertEquals(json, deseralizted);
    }

    private record Json(String value) {
    }
}