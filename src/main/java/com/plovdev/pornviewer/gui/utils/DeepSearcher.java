package com.plovdev.pornviewer.gui.utils;

import com.plovdev.pornviewer.events.SearcherFoundVideoListener;
import com.plovdev.pornviewer.httpquering.defimpl.PBPornHandler;
import com.plovdev.pornviewer.models.VideoCard;
import com.plovdev.pornviewer.pornimpl.porn365.DefPornParser;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class DeepSearcher {
    private static final Logger log = LoggerFactory.getLogger(DeepSearcher.class);

    public static synchronized void searchVideo(@NotNull DefPornParser pornParser, String url, List<String> keywords, @NotNull SearcherFoundVideoListener onFound) {
        PBPornHandler handler = new PBPornHandler();
        String html = handler.requestPorn(url + 0);
        Optional<VideoCard> mc = containsKeywordsInVideos(pornParser.getAllVideos(html), keywords);
        mc.ifPresent(onFound::onFound);

        String nextLink;
        while ((nextLink = handler.getNextLink(html)) != null) {
            try {
                html = handler.requestPorn(nextLink);
                Optional<VideoCard> maybeCard = containsKeywordsInVideos(pornParser.getAllVideos(html), keywords);
                maybeCard.ifPresent(onFound::onFound);
                Thread.sleep(500);
            } catch (Exception e) {
                log.error("Error search video in page: ", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    @Contract(pure = true)
    private static @NotNull Optional<VideoCard> containsKeywordsInVideos(@NotNull List<VideoCard> videoCards, @NotNull List<String> keywords) {
        return videoCards.stream()
                .filter(card -> keywords.stream().allMatch(keyword -> card.getTitle().toLowerCase().contains(keyword)))
                .findFirst();
    }
}