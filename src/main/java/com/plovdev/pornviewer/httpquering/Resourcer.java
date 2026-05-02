package com.plovdev.pornviewer.httpquering;

import java.util.List;

public interface Resourcer {
    String baseUrl();

    String searchUrl();

    String modelsSearchUrl();

    String modelsUrl();

    String modelUrl(String model);

    String categories();

    String videoUrl();

    String buildVideoUrlFromId(int id);

    List<String> getUrls();

    String getTrailerUrl(String id);
}