package com.plovdev.pornviewer.utility.deeplink;

import com.plovdev.pornviewer.utility.LauncherHelper;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;
import java.util.List;

public class DeepLinker {
    private static final Logger log = LoggerFactory.getLogger(DeepLinker.class);
    private static final List<String> POSSIBLE_LINKS = List.of("pv://favorites/add?id=", "pv://favorites/remove?id=", "pv://share/video?id=", "pv://share/model?id=0&name=", "pv://open/", "pv://open/main", "pv://open/models", "pv://open/favorites", "pv://open/downloads");

    public static void init(LauncherHelper launcherHelper) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                desktop.setOpenURIHandler(event -> {
                    URI deeplink = event.getURI();
                    if (deeplink != null) {
                        launcherHelper.notifyDeepLink(deeplink);
                    }
                });
            }
        } catch (Exception e) {
            log.error("Error to init deepleenk handler: {}", e.getMessage());
        }
    }

    public static void bindAutocompleteToSearchFiled(TextField field) {
        AutoCompletionBinding<String> binding = TextFields.bindAutoCompletion(field, suggestionRequest -> {
            String userText = suggestionRequest.getUserText().toLowerCase();
            if (userText.startsWith("pv") || userText.startsWith("pornviewer")) {
                return POSSIBLE_LINKS.stream().filter(s -> s.startsWith(userText)).toList();
            }
            return List.of();
        });
        binding.setDelay(0);
        binding.prefWidthProperty().bind(field.widthProperty());
        binding.getAutoCompletionPopup().skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                ListView<String> listView = (ListView<String>) newSkin.getNode();

                listView.setCellFactory(param -> new ListCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.replaceAll("pv://|pornviewer://|id=0", "")
                                    .replace("/", " → ")
                                    .replace("?", " ● ")
                                    .replace("id=", "ID ")
                                    .replace("name=", "Имя ")
                                    .replace("&", " | "));
                        }
                    }
                });
            }
        });
    }
}