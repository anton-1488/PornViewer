package com.plovdev.pornviewer.services.json.typeadapters;

import com.google.gson.*;
import com.plovdev.pornviewer.core.models.porn.Country;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Type;

public class ModelCountryAdapter implements JsonTypeAdapter<Country> {
    @Override
    public JsonElement serialize(@NonNull Country country, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(country.name());
    }

    @Override
    public Country deserialize(@NonNull JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return Country.fromString(jsonElement.getAsString());
    }
}