package com.plovdev.pornviewer.gui.panes.pagination;

import com.plovdev.pornviewer.gui.filters.TrianglePaginationBlock;
import com.plovdev.pornviewer.httpquering.defimpl.PBPornHandler;
import com.plovdev.pornviewer.models.VideoCard;
import com.plovdev.pornviewer.pornimpl.porn365.DefPornParser;
import com.plovdev.pornviewer.pornimpl.porn365.DefRes;
import com.plovdev.pornviewer.utility.Globals;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.layout.FlowPane;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MainPagination {
    private static final Logger log = LoggerFactory.getLogger(MainPagination.class);
    private final Object parserLock = new Object();
    private final IntegerProperty page = new SimpleIntegerProperty(1);
    private String baseUrl = DefRes.BASE;
    private final TrianglePaginationBlock block;
    private final DefPornParser pornParser = new DefPornParser();
    private final PBPornHandler pornHandler = new PBPornHandler();
    private volatile CompletableFuture<List<VideoCard>> currentParser = null;

    public MainPagination(FlowPane content, @NotNull TrianglePaginationBlock block) {
        this.block = block;
        setBaseUrl(baseUrl);

        block.setOnBack(() -> {
            int next = Math.max(0, page.get() - 1);
            content.getChildren().clear();
            runPornParsing(content, baseUrl + next);
            block.getBack().setDisable(next == 0);
            page.set(next);

            block.getBack().setText("Назад " + Math.max(0, next - 1));
            block.getNext().setText((page.get()) + " Вперед");
        });
        block.setOnToStart(() -> {
            content.getChildren().clear();
            reset();
            runPornParsing(content, baseUrl);
        });
        block.setOnNext(() -> {
            int next = Math.max(0, page.get() + 1);
            content.getChildren().clear();
            runPornParsing(content, baseUrl + next);
            page.set(next);

            block.getBack().setText("Назад " + page.get());
            block.getNext().setText((next + 1) + " Вперед");
        });
    }

    private void runPornParsing(FlowPane pane, String url) {
        CompletableFuture<List<VideoCard>> current = currentParser;
        if (current != null && !current.isDone()) {
            log.info("Cancelling previous task");
            current.cancel(true);
        }

        CompletableFuture<List<VideoCard>> newParser = getParseTask(url);
        synchronized (parserLock) {
            currentParser = newParser;
        }

        newParser.thenAccept(cards -> {
            synchronized (parserLock) {
                if (currentParser != newParser) {
                    return;
                }
            }
            Platform.runLater(() -> cards.forEach(e -> {
                e.render();
                pane.getChildren().add(e);
            }));
        });
    }

    public void reset() {
        page.set(1);
        block.getBack().setText("Назад 0");
        block.getNext().setText("2 Вперед");
    }

    public void setBaseUrl(@NotNull String baseUrl) {
        this.baseUrl = baseUrl + (baseUrl.endsWith("/") ? "" : "/");
    }

    @Contract(pure = true)
    private @NotNull CompletableFuture<List<VideoCard>> getParseTask(String url) {
        return CompletableFuture.supplyAsync(() -> pornParser.getAllVideos(pornHandler.requestPorn(url)), Globals.GLOBAL_EXECUTOR)
                .exceptionally(throwable -> {
                    log.error("Error was happened: ", throwable);
                    return List.of();
                });
    }
}