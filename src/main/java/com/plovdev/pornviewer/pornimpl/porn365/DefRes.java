package com.plovdev.pornviewer.pornimpl.porn365;

import com.plovdev.pornviewer.httpquering.Resourcer;

import java.util.List;

public class DefRes implements Resourcer {
    public static final String BASE1 = "http://my.porno365.bike";
    public static final String BASE2 = "http://5porno365.net";
    public static final String BASE3 = "http://7porno365.net";
    public static final String BASE4 = "http://1porno365.info";
    public static final String BASE5 = "http://2porno365.info";
    public static final String BASE6 = "http://7porno365.info";

    @Override
    public String baseUrl() {
        return BASE6;
    }

    @Override
    public String searchUrl() {
        return "/search/";
    }

    @Override
    public String modelsUrl() {
        return "/models/";
    }

    @Override
    public String modelsSearchUrl() {
        return "/?do=ajax&action=searchModel&model=";
    }

    @Override
    public String modelUrl(String model) {
        return modelsUrl() + model;
    }

    @Override
    public List<String> getUrls() {
        return List.of(BASE1, BASE2, BASE3, BASE4, BASE5, BASE6);
    }

    @Override
    public String categories() {
        return "/categories/";
    }

    @Override
    public String videoUrl() {
        return "/movie/";
    }

    @Override
    public String buildVideoUrlFromId(int id) {
        return baseUrl() + videoUrl() + id;
    }
}