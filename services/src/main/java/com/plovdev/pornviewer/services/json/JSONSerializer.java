package com.plovdev.pornviewer.services.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import com.plovdev.pornviewer.core.models.porn.Country;
import com.plovdev.pornviewer.core.models.video.VideoQuality;
import com.plovdev.pornviewer.services.json.typeadapters.ByteArrayAdapter;
import com.plovdev.pornviewer.services.json.typeadapters.DurationTypeAdapter;
import com.plovdev.pornviewer.services.json.typeadapters.ModelCountryAdapter;
import com.plovdev.pornviewer.services.json.typeadapters.VideoQualityAdapter;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Type;
import java.time.Duration;

public class JSONSerializer {
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .registerTypeAdapter(Country.class, new ModelCountryAdapter())
            .registerTypeAdapter(VideoQuality.class, new VideoQualityAdapter())
            .registerTypeAdapter(byte[].class, new ByteArrayAdapter())
            .setStrictness(Strictness.LENIENT)
            .disableHtmlEscaping()
            .create();

    public static @NonNull String serialize(Object o) {
        return GSON.toJson(o);
    }

    public static <V> V deserialize(String json, Type type) {
        return GSON.fromJson(json, type);
    }
}