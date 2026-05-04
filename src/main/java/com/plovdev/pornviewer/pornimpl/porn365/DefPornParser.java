package com.plovdev.pornviewer.pornimpl.porn365;

import com.plovdev.pornviewer.databases.FavoriteVideos;
import com.plovdev.pornviewer.httpquering.PornParser;
import com.plovdev.pornviewer.httpquering.defimpl.PBPornHandler;
import com.plovdev.pornviewer.models.*;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DefPornParser implements PornParser {
    private static final Logger log = LoggerFactory.getLogger(DefPornParser.class);

    @Override
    public List<VideoCard> getAllVideos(@NotNull String html) {
        Document doc = Jsoup.parse(html);
        List<VideoCard> cards = new ArrayList<>();
        try (ExecutorService service = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<VideoCard>> futures = new ArrayList<>();
            Elements elements = doc.select("li.video_block");
            elements.forEach(e -> {
                Future<VideoCard> future = service.submit(() -> (VideoCard) parseVideoBlock(e));
                futures.add(future);
            });

            futures.forEach(f -> {
                try {
                    cards.add(f.get());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return cards;
    }

    @Override
    public List<ModelCard> getAllModels(String html) {
        Document doc = Jsoup.parse(html);
        List<ModelCard> cards = new ArrayList<>();
        try (ExecutorService service = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<ModelCard>> futures = new ArrayList<>();

            Elements elements = doc.select("li.video_block");
            elements.forEach(e -> {
                Future<ModelCard> future = service.submit(() -> (ModelCard) parseVideoBlock(e));
                futures.add(future);
            });

            futures.forEach(f -> {
                try {
                    cards.add(f.get());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return cards;
    }

    private @NotNull PornCard parseVideoBlock(@NotNull Element videoElement) {
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

        long cardId = pornCard.getCardId();
        for (Integer vc : FavoriteVideos.getAllId()) {
            if (vc == cardId) {
                pornCard.setFavorite(true);
                break;
            }
        }

        return pornCard;
    }

    @Override
    public List<Category> getCategories(String html) {
        List<Category> categories = new ArrayList<>();
        try (ExecutorService service = Executors.newVirtualThreadPerTaskExecutor()) {
            Document doc = Jsoup.parse(html);

            Elements elements = doc.select("div ul.top-menu li a[href]");
            elements.forEach(e -> service.submit(() -> {
                String key = e.text().trim();
                String value = e.attr("abs:href");
                categories.add(new Category(key, value));
            }));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

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