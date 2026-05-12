package com.plovdev.pornviewer.utils.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.plovdev.pornviewer.utils.json.typeadapters.DurationTypeAdapter;

import java.lang.reflect.Type;
import java.time.Duration;

public class JSONSerializer {
    public static final Gson GSON = new GsonBuilder().registerTypeAdapter(Duration.class, new DurationTypeAdapter()).create();

    public static String serialize(Object o) {
        return GSON.toJson(o);
    }

    public static <V> V deserialize(String json, Type type) {
        return GSON.fromJson(json, type);
    }
}