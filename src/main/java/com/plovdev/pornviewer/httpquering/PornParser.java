package com.plovdev.pornviewer.httpquering;

import com.plovdev.pornviewer.models.*;

import java.util.List;

public interface PornParser {
    List<VideoCard> getAllVideos(String html);

    List<Category> getCategories(String html);

    List<ModelInfo> getModels(String html);

    VideoInfo parseVideo(String url);
}