package com.plovdev.pornviewer.pornimpl.porn365;

import com.plovdev.pornviewer.models.VideoInfo;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VideoInfoParser {
    private static final String TITLE = "og:title";
    private static final String VIDEO_URL = "og:url";
    private static final String DESCRIPTION = "og:description";
    private static final String PREVIEW = "og:image";
    private static final String DURATION = "og:duration";

    private static final String COL_VIDEO = "div.col_video";
    private static final int URL_ID_OFFSET = 29;

    public static VideoInfo parseInfo(Document document) {
        VideoInfo info = new VideoInfo();
        for (Element element : document.select("head meta[property]")) {
            String content = element.attr("content");
            switch (element.attr("property")) {
                case TITLE -> info.setTitle(content);
                case VIDEO_URL -> {
                    info.setUrl(content);
                    info.setId(Integer.parseInt(content.substring(URL_ID_OFFSET)));
                }
                case DURATION -> info.setDuration(Duration.ofSeconds(Long.parseLong(content)));
                case DESCRIPTION -> info.setDescription(content);
                case PREVIEW -> info.setPic(content);
            }
        }

        parseColVideo(info, document);
        return info;
    }

    private static void parseColVideo(VideoInfo info, Document document) {
        info.setCategories(parseTechnical(info, document.select("div.video-categories a[href]"), "model_link", false));
        info.setModels(parseTechnical(info, document.select("div.video-models a[href]"), null, false));
        info.setTags(parseTechnical(info, document.select("div.video-tags a[href]"), null, false));
        info.setTimeCodes(parseTechnical(info, document.select("div.video-tags span"), null, false));
        info.setUrls(parseTechnical(info, document.select("div.quality_chooser a[href]"), null, true));

        Element ratingElement = document.selectFirst(".rating_score");
        if (ratingElement != null) {
            info.setRating(ratingElement.ownText());
        }
        Element viewsElement = document.selectFirst("span[itemprop=interactionCount]");
        if (viewsElement != null) {
            info.setViews(Integer.parseInt(viewsElement.text()));
        }

        //TODO: Add comments parsing.
        info.setComments(new ArrayList<>());
    }

    private static Map<String, String> parseTechnical(VideoInfo info, Elements keys, String excludeClass, boolean reverseKeys) {
        Map<String, String> keyMap = new HashMap<>();
        for (Element key : keys) {
            if (excludeClass != null) {
                if (key.hasClass(excludeClass)) {
                    continue;
                }
            }
            String keyStr = key.attr("href");
            if (keyStr == null || keyStr.trim().isEmpty()) {
                continue;
            }
            if (reverseKeys) {
                keyMap.put(key.text(), keyStr.trim());
            } else {
                keyMap.put(keyStr.trim(), key.text());
            }
        }
        return keyMap;
    }
}