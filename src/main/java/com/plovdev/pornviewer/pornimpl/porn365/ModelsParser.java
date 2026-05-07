package com.plovdev.pornviewer.pornimpl.porn365;

import com.plovdev.pornviewer.models.ModelInfo;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ModelsParser {
    private static final Logger log = LoggerFactory.getLogger(ModelsParser.class);

    public static @NonNull List<ModelInfo> parseModels(Document document) {
        List<ModelInfo> infos = new ArrayList<>();

        try (ExecutorService modelParserExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<ModelInfo>> futures = new ArrayList<>();
            Elements models = document.select("div.item_model");
            models.forEach(e -> {
                Future<ModelInfo> future = modelParserExecutor.submit(() -> parseModelBlock(e));
                futures.add(future);
            });
            futures.forEach(f -> {
                try {
                    ModelInfo info = f.get();
                    infos.add(info);
                } catch (Exception e) {
                    log.error("Error to get model parsing result: ", e);
                }
            });
        } catch (Exception e) {
            log.error("Models parsing error: ", e);
        }

        return infos;
    }

    private static @NonNull ModelInfo parseModelBlock(@NonNull Element modelBlock) {
        ModelInfo result = new ModelInfo();
        result.setUrl(modelBlock.selectFirst("a[href]").attr("abs:href").trim());

        Element countryElement = modelBlock.selectFirst("span.flag");
        if (countryElement != null) {
            String classElement = countryElement.attr("class");
            String country = classElement.substring(classElement.lastIndexOf("-") + 1).trim();
            result.setCountry(country);
        }
        result.setAvatar(modelBlock.selectFirst("img").attr("src").trim());
        result.setVideos(Integer.parseInt(modelBlock.selectFirst("span.cnt_span").text()));
        setModelName(result, modelBlock);

        return result;
    }

    private static void setModelName(@NonNull ModelInfo info, @NonNull Element modelBlock) {
        Element names = modelBlock.selectFirst("span.model_name");
        info.setRusName(names.selectFirst("span.model_rus_name").text());
        info.setEngName(names.selectFirst("span.model_eng_name").text());
    }
}