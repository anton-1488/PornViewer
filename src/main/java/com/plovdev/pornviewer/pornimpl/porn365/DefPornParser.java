package com.plovdev.pornviewer.pornimpl.porn365;

import com.plovdev.pornviewer.databases.FavoriteVideos;
import com.plovdev.pornviewer.httpquering.PornParser;
import com.plovdev.pornviewer.httpquering.defimpl.PBPornHandler;
import com.plovdev.pornviewer.models.*;
import com.plovdev.pornviewer.utility.Globals;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class DefPornParser implements PornParser {
    private static final Logger log = LoggerFactory.getLogger(DefPornParser.class);
    private final List<Integer> allIds = FavoriteVideos.getAllId();

    @Override
    public List<VideoCard> getAllVideos(@NotNull String html) {
        Document doc = Jsoup.parse(html);
        Elements elements = doc.select("li.video_block");
        List<VideoCard> cards = new ArrayList<>();

        try {
            Globals.GLOBAL_EXECUTOR.invokeAll(elements
                    .stream()
                    .map(e -> (Callable<VideoCard>) () -> (VideoCard) parseVideoBlock(e))
                    .toList()).forEach(f -> {
                try {
                    cards.add(f.get());
                } catch (Exception e) {
                    log.error("Error to collect parsed video: ", e);
                }
            });
        } catch (Exception e) {
            log.error("Error parse all videos: ", e);
        }
        return cards;
    }

    private @NotNull ModelCard parseModelBlock(@NotNull Element videoElement) {
        ModelCard pornCard = new ModelCard();

        pornCard.setCardId(Integer.parseInt(videoElement.id()));
        Element link = videoElement.selectFirst("a.image");
        pornCard.setUrl(link.attr("abs:href"));
        Element img = link.selectFirst("img");
        pornCard.setPic(img.attr("abs:src"));
        Element title = link.selectFirst("p");
        pornCard.setTitle(title.text());

        return pornCard;
    }

    private @NotNull VideoCard parseVideoBlock(@NotNull Element videoElement) {
        VideoCard pornCard = new VideoCard();

        pornCard.setCardId(Integer.parseInt(videoElement.id())); // "45894"

        // 2. Ссылка на страницу видео
        Element link = videoElement.selectFirst("a.image");
        pornCard.setUrl(link.attr("abs:href"));

        // 3. Ссылка на превью (thumbnail)
        Element img = link.selectFirst("img");
        pornCard.setPic(img.attr("abs:src"));

        // 4. Название видео
        Element title = link.selectFirst("p");
        pornCard.setTitle(title.text());

        // 5. Длительность
        Element duration = videoElement.selectFirst("span.duration");
        pornCard.setDuration(duration.text()); // "18:53"

        // 6. Количество просмотров
        Element views = videoElement.selectFirst("span.video_views");
        pornCard.setViews(Integer.parseInt(views.text().replace(",", ""))); // "826,230" -> 826230

        // 7. Рейтинг
        Element rating = videoElement.selectFirst("span.mini-rating");
        pornCard.setRating(rating.text()); // "83%" -> 0.83f

        int cardId = pornCard.getCardId();
        if (allIds.contains(cardId)) {
            pornCard.setFavorite(true);
        }

        return pornCard;
    }

    @Override
    public List<Category> getCategories(String html) {
        Document doc = Jsoup.parse(html);
        List<Category> categories = new ArrayList<>();

        Elements elements = doc.select("div ul.top-menu li a[href]");
        elements.forEach(e -> {
            String key = e.text().trim();
            String value = e.attr("abs:href");
            categories.add(new Category(key, value));
        });
        return categories;
    }

    @Override
    public VideoInfo parseVideo(String videoUrl) {
        PBPornHandler handler = new PBPornHandler();
        String html = handler.requestPorn(videoUrl);
        if (html == null || html.isEmpty()) {
            throw new IllegalArgumentException("Html to parsing is empty");
        }
        Document document = Jsoup.parse(html);
        if (document == null) {
            throw new IllegalArgumentException("Html document to parsing is null");
        }
        return VideoInfoParser.parseInfo(document);
    }

    @Override
    public List<ModelInfo> getModels(String html) {
        Document doc = Jsoup.parse(html);
        return ModelsParser.parseModels(doc);
    }
}