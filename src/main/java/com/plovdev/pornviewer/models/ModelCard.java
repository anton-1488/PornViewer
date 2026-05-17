package com.plovdev.pornviewer.models;

import com.plovdev.pornviewer.events.listeners.ClickListener;
import com.plovdev.pornviewer.httpquering.defimpl.PBPornHandler;
import com.plovdev.pornviewer.utility.Globals;
import com.plovdev.pornviewer.utility.sharing.ShareParameter;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.util.Objects;

import static com.plovdev.pornviewer.gui.utils.ShareUtils.getShareButton;

public class ModelCard extends PornCard {
    private static final Logger log = LoggerFactory.getLogger(ModelCard.class);
    private ModelInfo modelInfo;
    private Pane pane;

    public void setModelInfo(ModelInfo modelInfo) {
        this.modelInfo = modelInfo;
    }

    public void setModelInfo() {
    }

    public ModelInfo getModelInfo() {
        return modelInfo;
    }

    public Pane getPane() {
        return pane;
    }

    public ModelCard() {

    }

    public ModelCard(ModelInfo modelInfo, Pane p) {
        this.modelInfo = modelInfo;
        pane = p;
    }

    public void setPane(Pane pane) {
        this.pane = pane;
    }

    @Override
    public void render() {
        try {
            StackPane mainContainer = new StackPane();
            PBPornHandler handler = new PBPornHandler();

            Image image;
            String pic = modelInfo.getAvatar();
            if (pic.endsWith(".webp")) {
                image = SwingFXUtils.toFXImage(ImageIO.read(new ByteArrayInputStream(handler.getBytes(pic))), null);
            } else {
                image = new Image(pic);
            }
            ImageView view = new ImageView(image);

            view.setSmooth(true);
            view.getStyleClass().add("video-preview");
            view.setPreserveRatio(true);

            Hyperlink titleLabel = new Hyperlink(modelInfo.getRusName());
            setTitle(modelInfo.getRusName());
            titleLabel.setOnAction(e -> ClickListener.notifyListeners(modelInfo));
            titleLabel.getStyleClass().add("video-title");

            HBox hBox = new HBox(titleLabel);
            hBox.getStyleClass().add("title-box");
            VBox box = new VBox(10, view, hBox);
            box.getStyleClass().add("trailer");

            view.fitWidthProperty().bind(pane.widthProperty().divide(3.5));
            view.fitWidthProperty().bind(pane.widthProperty().divide(3.5));
            view.fitWidthProperty().bind(pane.widthProperty().divide(3.5));

            box.minWidthProperty().bind(pane.widthProperty().divide(3.5));
            box.prefWidthProperty().bind(pane.widthProperty().divide(3.5));
            box.maxWidthProperty().bind(pane.widthProperty().divide(3.5));

            Label county = new Label(modelInfo.getCountry());
            county.getStyleClass().add("model-country");

            Label videos = new Label(String.valueOf(modelInfo.getVideos()));
            videos.getStyleClass().add("marker-download");

            Region region = new Region();
            HBox.setHgrow(region, Priority.ALWAYS);

            String modelName = modelInfo.getUrl().substring(modelInfo.getUrl().lastIndexOf("/") + 1);
            HBox infoOverlay = new HBox(county, region, new VBox(10, videos, getShareButton(Globals.getPrimaryStage(), this, new ShareParameter("name", modelName))));
            infoOverlay.setVisible(false);
            infoOverlay.getStyleClass().add("model-hover");

            // Позиционируем overlay поверх изображения
            StackPane.setAlignment(infoOverlay, Pos.BOTTOM_CENTER);
            StackPane.setMargin(infoOverlay, new Insets(0, 0, 50, 0));

            // Обработчики наведения
            mainContainer.setOnMouseEntered(e -> infoOverlay.setVisible(true));
            mainContainer.setOnMouseExited(e -> infoOverlay.setVisible(false));

            mainContainer.getChildren().addAll(box, infoOverlay);
            getChildren().add(mainContainer);
        } catch (Exception e) {
            log.error("Model rendering error: ", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ModelCard modelCard = (ModelCard) o;
        return Objects.equals(modelInfo, modelCard.modelInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), modelInfo);
    }
}