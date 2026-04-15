package com.plovdev.pornviewer.gui.utils;

import com.plovdev.pornviewer.models.ModelCard;
import com.plovdev.pornviewer.utility.sharing.ShareParameter;
import com.plovdev.pornviewer.utility.sharing.Sharer;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

public class ShareUtils {
    public static Button getShareButton(Stage stage, ModelCard card, ShareParameter... p) {
        Button shareButton = new Button();
        shareButton.getStyleClass().add("share-button");
        SVGPath shareIcon = new SVGPath();
        shareIcon.setContent("M1613,203a2.967,2.967,0,0,1-1.86-.661l-3.22,2.01a2.689,2.689,0,0,1,0,1.3l3.22,2.01A2.961,2.961,0,0,1,1613,207a3,3,0,1,1-3,3,3.47,3.47,0,0,1,.07-0.651l-3.21-2.01a3,3,0,1,1,0-4.678l3.21-2.01A3.472,3.472,0,0,1,1610,200,3,3,0,1,1,1613,203Zm0,8a1,1,0,1,0-1-1A1,1,0,0,0,1613,211Zm-8-7a1,1,0,1,0,1,1A1,1,0,0,0,1605,204Zm8-5a1,1,0,1,0,1,1A1,1,0,0,0,1613,199Z");
        shareIcon.setStroke(Color.WHITE);
        shareIcon.setScaleY(1.5);
        shareIcon.setScaleX(1.5);
        shareIcon.setStrokeWidth(1);
        shareIcon.setFill(Color.TRANSPARENT);

        shareButton.setGraphic(shareIcon);
        shareButton.setOnAction(e -> Sharer.share(stage, card, p));
        return shareButton;
    }
}