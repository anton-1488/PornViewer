package com.plovdev.pornviewer.gui.video;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

public class TimecodesBar extends AnchorPane {
    private static final Logger log = LoggerFactory.getLogger(TimecodesBar.class);
    private final ObservableList<TimecodeView> timecodes = FXCollections.observableArrayList();
    private final SortedList<TimecodeView> sortedTimecodes = new SortedList<>(timecodes);
    private Duration totalDuration;

    {
        sortedTimecodes.setComparator(Comparator.comparing(TimecodeView::getTime));
    }

    public TimecodesBar(Duration duration) {
        this.totalDuration = duration;
        setupTimecodes();
    }

    public TimecodesBar(List<TimecodeView> timecodes, Duration duration) {
        this.timecodes.addAll(timecodes);
        this.totalDuration = duration;
        setupTimecodes();
    }

    private void setupTimecodes() {
        setMouseTransparent(false);
        setPickOnBounds(false);

        setupBar();
        widthProperty().addListener((p1, p2, p3) -> recountBar(p3.doubleValue()));
    }

    private void setupBar() {
        setMinHeight(5);
        setPrefHeight(5);
        setMaxHeight(5);

        getChildren().clear();
        for (TimecodeView view : sortedTimecodes) {
            view.setMouseTransparent(false);
            addTimecodeToBar(view, getWidth());
        }
    }

    private void recountBar(double width) {
        for (TimecodeView view : sortedTimecodes) {
            view.setMouseTransparent(false);
            AnchorPane.setLeftAnchor(view, calculateTimecodePosition(view, width));
        }
    }

    private void addTimecodeToBar(TimecodeView view, double currentWidth) {
        getChildren().add(view);
        AnchorPane.setLeftAnchor(view, calculateTimecodePosition(view, currentWidth));
        AnchorPane.setBottomAnchor(view, -(view.getRadius() / 2));
    }

    private double calculateTimecodePosition(TimecodeView view, double currentWidth) {
        long timecode = view.getTime().toMillis();
        long total = totalDuration.toMillis();
        log.info("Timecode: {}, Total: {}, currentWidth: {}", timecode, total, currentWidth);
        if (timecode == 0 || total == 0) {
            return 0;
        }
        return ((double) timecode / total) * currentWidth;
    }

    public void addTimecode(TimecodeView timecode) {
        timecodes.add(timecode);
        addTimecodeToBar(timecode, getWidth());
    }

    public void removeTimecode(TimecodeView timecode) {
        timecodes.remove(timecode);
        getChildren().remove(timecode);
    }

    public Duration getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(Duration totalDuration) {
        this.totalDuration = totalDuration;
    }

    public List<TimecodeView> getTimecodeViews() {
        return List.copyOf(timecodes);
    }
}