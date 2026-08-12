package com.plovdev.pornviewer.gui.video;

import com.plovdev.pornviewer.models.DownloadedVideoCard;
import com.plovdev.pornviewer.models.FavoriteVideo;
import com.plovdev.pornviewer.models.VideoCard;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.plovdev.pornviewer.gui.utils.ShareUtils.getShareButton;
import static javafx.scene.media.MediaPlayer.Status.PAUSED;
import static javafx.scene.media.MediaPlayer.Status.READY;

public class VideoPlyer extends StackPane {
    private static final Logger log = LoggerFactory.getLogger(VideoPlyer.class);
    private MediaPlayer mediaPlayer;
    private final MediaView mediaView;
    private final Slider slider = new Slider(0, 1, 0);
    private final Label timeLabel = new Label("00:00");
    private boolean isWork = false;
    private final BorderPane content;
    private final PauseTransition hideTimer;
    private boolean isPlay = false;
    private final ToggleButton playStop = new ToggleButton("| |");
    private Duration oneVideoDurationPercent = Duration.millis(5000);

    public VideoPlyer(Media media, VideoCard card, @NotNull Stage stage) {
        stage.setMaximized(true);

        mediaPlayer = new MediaPlayer(media);
        mediaView = new MediaView(mediaPlayer);
        mediaView.fitWidthProperty().bind(widthProperty());
        mediaView.fitHeightProperty().bind(heightProperty());

        TimecodesBar timecodesBar = new TimecodesBar(DurationUtils.ofJavaFxDuraion(mediaPlayer.getTotalDuration()));

        Label totalLabel = new Label(DurationUtils.formatDurationToString(DurationUtils.ofJavaFxDuraion(mediaPlayer.getTotalDuration())));
        totalLabel.getStyleClass().add("marker-download");
        timeLabel.getStyleClass().add("marker-download");
        mediaPlayer.setOnReady(() -> {
            Duration totalDuration = mediaPlayer.getTotalDuration();
            totalLabel.setText(DurationUtils.formatDurationToString(DurationUtils.ofJavaFxDuraion(totalDuration)));
            timecodesBar.setTotalDuration(DurationUtils.ofJavaFxDuraion(totalDuration));
            timecodesBar.addTimecodes(card.getTimecodes().stream().map(t -> new TimecodeView(t.getText(), t.getTime())).toList());
            double totalMillis = totalDuration.toMillis();
            oneVideoDurationPercent = Duration.millis(totalMillis / 100);
        });

        playStop.setMinSize(70, 70);
        playStop.setPrefSize(70, 70);
        playStop.setMaxSize(70, 70);
        playStop.getStyleClass().add("play-stop");
        playStop.selectedProperty().addListener((p1, p2, p3) -> {
            if (p3) {
                pause();
            } else {
                play();
            }
            resetHideTimer(); // Сброс таймера при взаимодействии
        });
        slider.prefWidthProperty().bind(widthProperty().divide(1.02));
        slider.getStyleClass().add("video-slider");


        mediaPlayer.setVolume(0.3);
        Slider volume = new Slider(0, 1, 0.3);
        volume.valueProperty().addListener((p1, p2, p3) -> mediaPlayer.setVolume(p3.doubleValue()));

        Label magn = new Label("\uD83D\uDD0D");

        ComboBox<Double> rates = new ComboBox<>(FXCollections.observableArrayList(0.05, 0.1, 0.2, 0.25, 0.5, 0.75, 1.0, 1.25, 1.5, 1.75, 2.0, 2.25, 2.50,
                2.75, 3.00, 3.25, 3.5, 3.75, 4.0, 4.25, 4.50, 4.75, 5.0));
        rates.getStyleClass().add("video-rates");
        rates.setValue(1.0);
        rates.valueProperty().addListener((p1, p2, p3) -> mediaPlayer.setRate(p3));
        rates.focusedProperty().addListener(e -> rates.requestFocus());

        HBox top = new HBox(volume, magn, hReg(), rates);
        if (!(card instanceof DownloadedVideoCard)) {
            top.getChildren().add(getShareButton(stage, card));
        }

        HBox center = new HBox(new StackPane(slider, timecodesBar));
        HBox bottom = new HBox(timeLabel, hReg(), totalLabel);

        content = new BorderPane();
        content.getStyleClass().add("video-player-bordered");
        content.setVisible(false);
        content.setCenter(playStop);
        content.setBottom(new VBox(10, top, center, bottom));

        // Инициализация таймера скрытия
        hideTimer = new PauseTransition(Duration.seconds(3));
        hideTimer.setOnFinished(event -> content.setVisible(false));

        setup();

        mediaView.setSmooth(true);
        mediaView.setOnMouseExited(event -> resetHideTimer());
        setupControlInteractions();
        mediaPlayer.setAutoPlay(true);
        getChildren().addAll(mediaView, content);

        Magnifier magnifier = new Magnifier(this);
        getChildren().add(magnifier);

        this.setFocusTraversable(true);
        this.setOnMouseClicked(event -> this.requestFocus());

        Slider setSoomSlider = new Slider(1, 5, 2);
        setSoomSlider.getStyleClass().add("video-slider");
        setSoomSlider.valueProperty().addListener((p1, p2, p3) -> magnifier.setZoomFactor(p3.doubleValue()));
        CustomMenuItem setZoom = new CustomMenuItem(setSoomSlider, false);
        setZoom.setGraphic(new Label("zoom"));

        Slider setSizeSlider = new Slider(100, 150, 125);
        setSizeSlider.getStyleClass().add("video-slider");
        setSizeSlider.valueProperty().addListener((p1, p2, p3) -> magnifier.setMagnifierRadius(p3.doubleValue()));
        CustomMenuItem setSize = new CustomMenuItem(setSizeSlider, false);
        setSize.setGraphic(new Label("size"));
        ContextMenu contextMenu = new ContextMenu(setZoom, setSize);
        magn.setOnMousePressed(e -> {
            double x = e.getScreenX();
            double y = e.getScreenY();
            contextMenu.show(magn, x, y);
        });

        setOnKeyPressed(event -> {
            if (event.isControlDown()) {
                magnifier.toggle();
            }
            switch (event.getCode()) {
                case T -> {
                    if (card instanceof FavoriteVideo || card instanceof DownloadedVideoCard) {
                        TextInputDialog inputText = new TextInputDialog();
                        inputText.setTitle("Timecode");
                        inputText.setContentText("Введите описание таймкода:");
                        Optional<String> optText = inputText.showAndWait();
                        optText.ifPresent(text -> {
                            if (!text.isEmpty()) {
                                log.info("Adding timecode text: {}", text);
                                TimecodeView view = new TimecodeView(text, DurationUtils.ofJavaFxDuraion(mediaPlayer.getCurrentTime()));
                                view.setOnChecked(() -> mediaPlayer.seek(DurationUtils.ofJavaDuration(view.getTime())));
                                timecodesBar.addTimecode(view);
                            }
                        });
                    }
                }
                case SPACE -> {
                    if (isPlay) {
                        pause();
                    } else {
                        play();
                    }
                }
                case RIGHT -> {
                    Duration current = mediaPlayer.getCurrentTime();
                    mediaPlayer.seek(current.add(oneVideoDurationPercent));
                }
                case LEFT -> {
                    Duration current = mediaPlayer.getCurrentTime();
                    mediaPlayer.seek(current.subtract(oneVideoDurationPercent));
                }
            }
            event.consume();
        });

        mediaView.setOnMouseMoved(event -> {
            if (!magnifier.isActive()) {
                content.setVisible(true);
                resetHideTimer();
            }
        });

        requestFocus();
        setFocused(true);
        setFocusTraversable(true);
    }

    private void setupControlInteractions() {
        // Сброс таймера при взаимодействии со слайдером
        slider.setOnMouseEntered(event -> resetHideTimer());
        slider.setOnMouseMoved(event -> resetHideTimer());

        slider.setOnMousePressed(event -> {
            isWork = true;
            resetHideTimer();
        });
        slider.setOnMouseDragged(event -> {
            isWork = true;
            resetHideTimer();
        });

        // Сброс таймера при взаимодействии с кнопкой play/stop
        content.getCenter().setOnMouseEntered(event -> resetHideTimer());
        content.getCenter().setOnMouseMoved(event -> resetHideTimer());
    }

    private void resetHideTimer() {
        hideTimer.stop();
        hideTimer.playFromStart();
        content.setVisible(true);
        this.requestFocus();
    }

    private void setup() {
        mediaPlayer.currentTimeProperty().addListener((p, p1, p2) -> {
            if (!isWork && mediaPlayer.getTotalDuration().greaterThan(Duration.ZERO)) {
                double progress = p2.toSeconds() / mediaPlayer.getTotalDuration().toSeconds();
                slider.setValue(progress);
                timeLabel.setText(DurationUtils.formatDurationToString(DurationUtils.ofJavaFxDuraion(p2)));
            }
        });
        slider.valueProperty().addListener((p1, p2, p3) -> {
            if (isWork) {
                Duration time = Duration.seconds(p3.doubleValue() * mediaPlayer.getTotalDuration().toSeconds());
                mediaPlayer.seek(time);
            }
        });

        slider.setOnMouseReleased(e -> {
            isWork = false;
            Duration time = Duration.seconds(slider.getValue() * mediaPlayer.getTotalDuration().toSeconds());
            mediaPlayer.seek(time);
            resetHideTimer();
        });
    }

    public void play() {
        MediaPlayer.Status status = mediaPlayer.getStatus();
        if (status == READY || status == PAUSED) {
            mediaPlayer.play();
        } else {
            Runnable r = mediaPlayer.getOnReady();
            mediaPlayer.setOnReady(() -> {
                if (r != null) r.run();
                mediaPlayer.play();
            });
        }
        isPlay = true;
        playStop.setText("| |");
        resetHideTimer();
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public Media getMedia() {
        return mediaPlayer.getMedia();
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
        mediaView.setMediaPlayer(mediaPlayer);
    }

    public void setMediaPlayer(Media newMedia) {
        this.mediaPlayer = new MediaPlayer(newMedia);
        mediaView.setMediaPlayer(mediaPlayer);
    }

    public void stop() {
        Thread.startVirtualThread(() -> {
            mediaPlayer.pause();
            mediaPlayer.stop();
            mediaPlayer.setOnStopped(() -> mediaPlayer.dispose());
        });
    }

    public void pause() {
        mediaPlayer.pause();
        isPlay = false;
        playStop.setText("►");
        resetHideTimer();
    }

    private Region hReg() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    private Region vReg() {
        Region region = new Region();
        VBox.setVgrow(region, Priority.ALWAYS);
        return region;
    }
}