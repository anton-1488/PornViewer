package com.plovdev.pornviewer.gui.panes.pagination;

import com.plovdev.pornviewer.gui.filters.TrinaglePaginationBlock;
import com.plovdev.pornviewer.httpquering.defimpl.PBPornHandler;
import com.plovdev.pornviewer.models.VideoCard;
import com.plovdev.pornviewer.pornimpl.porn365.DefPornParser;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainPagination {
    private final Set<ObservableList<Pane>> cahe = new HashSet<>();
    private final IntegerProperty page = new SimpleIntegerProperty(0);
    private String baseUrl = "http://7porno365.info/";

    public MainPagination(FlowPane content, TrinaglePaginationBlock block) {
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
            runPornParsing(content, baseUrl);
            page.set(0);
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
        Thread.startVirtualThread(getParseTask(pane, url));
    }

    public Set<ObservableList<Pane>> getCahe() {
        return cahe;
    }

    public int getPage() {
        return page.get();
    }

    public IntegerProperty pageProperty() {
        return page;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Contract(pure = true)
    private @NotNull Runnable getParseTask(FlowPane pane, String url) {
        return () -> {
            try {
                PBPornHandler handler = new PBPornHandler();
                String htmlPage = handler.requestPorn(url);
                DefPornParser pornParser = new DefPornParser();
                List<VideoCard> cards = pornParser.getAllVideos(htmlPage);
                cards.forEach(e -> {
                    e.render();
                    Platform.runLater(() -> pane.getChildren().add(e));
                });
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        };
    }
}