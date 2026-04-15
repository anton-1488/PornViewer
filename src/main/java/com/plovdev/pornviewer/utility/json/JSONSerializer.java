package com.plovdev.pornviewer.utility.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.Duration;

public class JSONSerializer {
    public static final Gson GSON = new GsonBuilder().registerTypeAdapter(Duration.class, new DurationTypeAdapter()).create();

    public static String serialize(Object o) {
        return GSON.toJson(o);
    }

    public static <V> V deserialize(String json, Class<V> type) {
        return GSON.fromJson(json, type);
    }
}