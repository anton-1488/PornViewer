package com.plovdev.pornviewer.gui.video;

import javafx.animation.AnimationTimer;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.image.ImageView;
import javafx.geometry.Rectangle2D;

import java.util.Objects;

public class Magnifier extends StackPane {
    private final Pane contentPane;
    private final ImageView magnifiedView;
    private final AnimationTimer updateTimer;
    private double zoomFactor = 2.0;
    private double magnifierRadius = 150;
    private boolean isActive = false;
    private double mouseX = 0;
    private double mouseY = 0;

    // Смещение от углов
    private double offsetX = 20;
    private double offsetY = 20;

    public Magnifier(Pane content) {
        this.contentPane = content;

        // Создание вида для увеличенного изображения
        magnifiedView = new ImageView();
        magnifiedView.setFitWidth(magnifierRadius * 2);
        magnifiedView.setFitHeight(magnifierRadius * 2);
        magnifiedView.setClip(new Circle(magnifierRadius, magnifierRadius, magnifierRadius));
        magnifiedView.setVisible(false);

        // Эффект для стекла
        magnifiedView.setStyle(
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 2);"
        );

        this.getChildren().addAll(magnifiedView);
        this.setMouseTransparent(true);

        // Устанавливаем явный размер для StackPane
        this.setMinSize(magnifierRadius * 2, magnifierRadius * 2);
        this.setPrefSize(magnifierRadius * 2, magnifierRadius * 2);
        this.setMaxSize(magnifierRadius * 2, magnifierRadius * 2);

        // Таймер для постоянного обновления
        updateTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isActive) {
                    updateMagnifierPosition();
                    updateMagnifiedView();
                }
            }
        };

        setupMouseHandlers();
        setupKeyHandlers();
    }

    private void setupMouseHandlers() {
        // Отслеживаем позицию мыши только для обновления изображения
        contentPane.setOnMouseMoved(event -> {
            updateMousePosition(event.getX(), event.getY());

            if (isActive) {
                updateMagnifiedView();
            }
        });

        contentPane.setOnMouseDragged(event -> {
            updateMousePosition(event.getX(), event.getY());

            if (isActive) {
                updateMagnifiedView();
            }
        });

        // Обновляем позицию при изменении размера contentPane
        contentPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (isActive) {
                updateMagnifierPosition();
            }
        });

        contentPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (isActive) {
                updateMagnifierPosition();
            }
        });
    }

    private void updateMousePosition(double x, double y) {
        mouseX = x;
        mouseY = y;
    }

    private void setupKeyHandlers() {
        // Обработка нажатия Ctrl - переключение режима
        contentPane.setOnKeyPressed(event -> {
            if (Objects.requireNonNull(event.getCode()) == KeyCode.CONTROL) {
                if (!isActive) {
                    activateMagnifier();
                } else {
                    deactivateMagnifier();
                }
                event.consume();
            }
        });

        // Убедимся, что панель может получать фокус для обработки клавиш
        contentPane.setFocusTraversable(true);
        contentPane.setOnMouseClicked(event -> contentPane.requestFocus());
    }

    private void activateMagnifier() {
        isActive = true;
        magnifiedView.setVisible(true);
        this.setVisible(true);

        // Позиционируем и обновляем изображение
        updateMagnifierPosition();
        updateMagnifiedView();

        updateTimer.start();
        System.out.println("Magnifier activated in bottom-right corner");
    }

    private void deactivateMagnifier() {
        isActive = false;
        magnifiedView.setVisible(false);
        this.setVisible(false);
        updateTimer.stop();
        System.out.println("Magnifier deactivated");
    }

    private void updateMagnifierPosition() {
        if (!isActive) return;

        // Для StackPane используем абсолютное позиционирование
        double magnifierSize = magnifierRadius * 2;

        // Позиция X: ширина contentPane минус размер лупы минус отступ
        double posX = contentPane.getWidth()/1.7 - magnifierSize - offsetX;

        // Позиция Y: высота contentPane минус размер лупы минус отступ
        double posY = contentPane.getHeight()/1.7 - magnifierSize - offsetY;

        // Проверяем, чтобы не выйти за левую/верхнюю границу
        posX = Math.max(0, posX);
        posY = Math.max(0, posY);

        // Устанавливаем позицию через управление layout
        this.setTranslateX(posX);
        this.setTranslateY(posY);
    }

    private void updateMagnifiedView() {
        if (!isActive) return;

        // Вычисляем область для захвата с учетом zoom factor
        double captureRadius = magnifierRadius / zoomFactor;

        // Проверяем границы contentPane
        if (mouseX - captureRadius < 0 || mouseX + captureRadius > contentPane.getWidth() ||
                mouseY - captureRadius < 0 || mouseY + captureRadius > contentPane.getHeight()) {
            // Если выходим за границы, показываем пустую область
            magnifiedView.setImage(null);
            return;
        }

        // Создаем снимок области под курсором
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        params.setViewport(new Rectangle2D(
                mouseX - captureRadius,
                mouseY - captureRadius,
                captureRadius * 2,
                captureRadius * 2
        ));

        try {
            WritableImage snapshot = contentPane.snapshot(params, null);
            if (snapshot != null) {
                magnifiedView.setImage(snapshot);
            }
        } catch (Exception e) {
            System.err.println("Error creating snapshot: " + e.getMessage());
            magnifiedView.setImage(null);
        }
    }

    // Методы для настройки позиции
    public void setCornerOffset(double offsetX, double offsetY) {
        this.offsetX = Math.max(0, offsetX);
        this.offsetY = Math.max(0, offsetY);
        if (isActive) {
            updateMagnifierPosition();
        }
    }

    // Методы для настройки увеличительного стекла
    public void setZoomFactor(double zoomFactor) {
        this.zoomFactor = Math.max(1.0, zoomFactor);
        if (isActive) {
            updateMagnifiedView();
        }
    }

    public void setMagnifierRadius(double radius) {
        this.magnifierRadius = Math.max(10, radius);
        magnifiedView.setFitWidth(radius * 2);
        magnifiedView.setFitHeight(radius * 2);

        // Обновляем размер StackPane
        this.setMinSize(radius * 2, radius * 2);
        this.setPrefSize(radius * 2, radius * 2);
        this.setMaxSize(radius * 2, radius * 2);

        // Обновляем clip с новым радиусом
        Circle clip = new Circle(radius, radius, radius);
        magnifiedView.setClip(clip);

        if (isActive) {
            updateMagnifierPosition();
            updateMagnifiedView();
        }
    }

    // Геттеры
    public double getZoomFactor() {
        return zoomFactor;
    }

    public double getMagnifierRadius() {
        return magnifierRadius;
    }

    public boolean isActive() {
        return isActive;
    }

    public double getOffsetX() {
        return offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }

    // Переключение состояния
    public void toggle() {
        if (isActive) {
            deactivateMagnifier();
        } else {
            activateMagnifier();
        }
    }

    public void dispose() {
        updateTimer.stop();
    }

    // Метод для принудительного обновления
    public void updateFromExternal(double x, double y) {
        updateMousePosition(x, y);
        if (isActive) {
            updateMagnifiedView();
        }
    }

    // Метод для принудительного обновления позиции
    public void refreshPosition() {
        if (isActive) {
            updateMagnifierPosition();
        }
    }

    // ДОБАВЬТЕ ЭТОТ МЕТОД ДЛЯ ОТЛАДКИ
    public void debugInfo() {
        System.out.println("=== Magnifier Debug Info ===");
        System.out.println("Active: " + isActive);
        System.out.println("Visible: " + this.isVisible());
        System.out.println("LayoutX: " + this.getLayoutX());
        System.out.println("LayoutY: " + this.getLayoutY());
        System.out.println("TranslateX: " + this.getTranslateX());
        System.out.println("TranslateY: " + this.getTranslateY());
        System.out.println("Bounds: " + this.getBoundsInParent());
        System.out.println("Parent: " + (this.getParent() != null ? this.getParent().getClass().getSimpleName() : "null"));
        System.out.println("ContentPane size: " + contentPane.getWidth() + "x" + contentPane.getHeight());
        System.out.println("============================");
    }
}