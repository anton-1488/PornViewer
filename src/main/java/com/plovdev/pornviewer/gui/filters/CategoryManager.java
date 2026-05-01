package com.plovdev.pornviewer.gui.filters;

import com.plovdev.pornviewer.databases.UserPreferences;
import com.plovdev.pornviewer.events.listeners.PornUpdateListener;
import com.plovdev.pornviewer.httpquering.*;
import com.plovdev.pornviewer.httpquering.defimpl.PBPornHandler;
import com.plovdev.pornviewer.models.Category;
import com.plovdev.pornviewer.pornimpl.porn365.DefRes;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Callback;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CategoryManager extends VBox {
    private boolean isOpen = false;
    private double startX = 0;

    private static final ObservableList<Hyperlink> originNots = FXCollections.observableArrayList();
    private static final FilteredList<Hyperlink> filtereds = new FilteredList<>(originNots, p -> true);
    private static final SortedList<Hyperlink> sortered = new SortedList<>(filtereds);

    private final PBPornHandler handler = new PBPornHandler();
    private final Resourcer resourcer;

    public CategoryManager(Resourcer rsc) {
        super(20);
        resourcer = rsc;

        setPadding(new Insets(20));

        setPrefWidth(350);
        getStyleClass().add("nots-panel");

        Label label = new Label("Категории");
        label.getStyleClass().add("mark");


        Label close = new Label("—");
        close.getStyleClass().add("mark-close");

        TextField field = new TextField();
        field.textProperty().addListener((q, w, e) -> filtereds.setPredicate(n -> n.getText().toLowerCase().contains(e.toLowerCase())));
        field.setPromptText("Искать...");
        field.getStyleClass().add("search-filed");
        field.prefWidthProperty().bind(widthProperty().divide(2.3));
        field.getStyleClass().add("search");
        field.setBackground(Background.EMPTY);


        Region reg1 = new Region();
        HBox.setHgrow(reg1, Priority.ALWAYS);
        Region reg2 = new Region();
        HBox.setHgrow(reg2, Priority.ALWAYS);

        HBox top = new HBox(10, new VBox(5, label), reg1, field, reg2, close);
        top.getStyleClass().add("top-nots");

        close.setOnMouseClicked(e -> toggle());

        ListView<Hyperlink> view = getList();

        getChildren().addAll(top, view);
        view.prefHeightProperty().bind(heightProperty());

        setTranslateX(350);
        load();
    }

    private void load() {
        Thread.startVirtualThread(() -> {
            PornVideoAdapter adapter = UserPreferences.get("0000").getPornAdapter();
            PornChecker checker = adapter.getChecker();
            if (!checker.hasCategories()) return;

            PornParser parser = adapter.getParser();
            List<Category> ctgs = parser.getCategories(handler.requestPorn(resourcer.baseUrl()));
            ctgs.add(new Category("Главная", DefRes.BASE6));

            for (Category category : ctgs) {
                Hyperlink link = new Hyperlink(category.getName());
                link.setOnAction(e -> {
                    PornUpdateListener.notifyListeners(category.getUrl(), 0);
                    toggle();
                });
                originNots.add(link);
            }
        });
    }

    public void resize(@NotNull AnchorPane anchorPane) {
        Pane pane = new Pane();
        pane.setPrefWidth(4);

        AnchorPane.setRightAnchor(pane, 298.0);
        AnchorPane.setTopAnchor(pane, 0.0);
        AnchorPane.setBottomAnchor(pane, 0.0);

        anchorPane.getChildren().add(pane);

        pane.setCursor(Cursor.E_RESIZE);

        pane.setOnMousePressed(e -> startX = e.getScreenX());
        pane.setOnMouseDragged(e -> {
            double deltaX = e.getScreenX() - startX;
            double newX = getWidth() - (deltaX * 2);

            setPrefWidth(newX);
            AnchorPane.setRightAnchor(pane, newX);

            startX = e.getSceneX();
        });
    }

    private ListView<Hyperlink> getList() {
        ListView<Hyperlink> links = new ListView<>(sortered);
        links.getStyleClass().add("notify-view");

        links.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Hyperlink> call(ListView<Hyperlink> vBoxListView) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Hyperlink vBox, boolean b) {
                        super.updateItem(vBox, b);
                        getStyleClass().add("notify-item");

                        if (vBox == null || b) {
                            setGraphic(null);
                            setBorder(null);
                        } else {
                            vBox.getStyleClass().add("notify-item-label");
                            setGraphic(vBox);
                        }
                    }
                };
            }
        });

        return links;
    }

    public void toggle() {
        TranslateTransition transition = new TranslateTransition(Duration.millis(200), this);
        transition.setInterpolator(Interpolator.EASE_IN);

        transition.setToX(isOpen ? getPrefWidth() : 0);
        transition.play();

        isOpen = !isOpen;
    }
}