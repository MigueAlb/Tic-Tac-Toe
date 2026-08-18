package espol.com.tresenraya.ui;

import espol.com.tresenraya.model.GameMode;
import espol.com.tresenraya.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public final class GameModeView extends StackPane {

    private static final String BACKGROUND = "/images/momazos.jpeg";

    private final User user;
    private final Consumer<GameMode> onModeSelected;

    public GameModeView(User user, Consumer<GameMode> onModeSelected) {
        this.user = user;
        this.onModeSelected = onModeSelected;

        getStyleClass().add("game-root");

        getChildren().addAll(
                createBackground(),
                createTint(),
                createContent()
        );
    }

    private ImageView createBackground() {
        ImageView view = new ImageView(
                new Image(getClass().getResourceAsStream(BACKGROUND))
        );

        view.setPreserveRatio(false);

        view.fitWidthProperty().bind(widthProperty());
        view.fitHeightProperty().bind(heightProperty());

        return view;
    }

    private Region createTint() {
        Region tint = new Region();

        tint.getStyleClass().add("game-tint");
        tint.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        return tint;
    }

    private VBox createContent() {
        Label eyebrow = new Label("ESTACIÓN ORBITAL 03");
        eyebrow.getStyleClass().add("eyebrow");

        Label title = new Label("Elige tu misión");
        title.getStyleClass().add("mode-title");

        Label subtitle = new Label(
                "Hola, " + user.getName() + ". Selecciona cómo quieres jugar."
        );
        subtitle.getStyleClass().add("subtitle");

        GridPane cards = new GridPane();

        cards.setHgap(20);
        cards.setVgap(20);
        cards.setAlignment(Pos.CENTER);

        int index = 0;

        for (GameMode mode : GameMode.values()) {
            Button card = createModeCard(mode);

            cards.add(card, index, 0);

            index++;
        }

        Label hint = new Label(
                "Puedes cambiar de modo en cualquier momento antes de iniciar una nueva partida."
        );

        hint.getStyleClass().add("panel-copy");
        hint.setWrapText(true);
        hint.setMaxWidth(700);
        hint.setAlignment(Pos.CENTER);

        VBox content = new VBox(
                14,
                eyebrow,
                title,
                subtitle,
                cards,
                hint
        );

        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(36));
        content.setMaxWidth(1100);
        content.getStyleClass().add("mode-container");

        return content;
    }

    private Button createModeCard(GameMode mode) {
        Label icon = new Label(mode.icon());
        icon.getStyleClass().add("mode-icon");

        Label title = new Label(mode.displayName());
        title.getStyleClass().add("mode-card-title");

        Label description = new Label(mode.description());
        description.getStyleClass().add("mode-card-description");
        description.setWrapText(true);
        description.setMaxWidth(250);

        VBox content = new VBox(
                10,
                icon,
                title,
                description
        );

        content.setAlignment(Pos.CENTER);
        content.setFillWidth(true);

        Button button = new Button();

        button.setGraphic(content);
        button.setContentDisplay(
                javafx.scene.control.ContentDisplay.GRAPHIC_ONLY
        );

        button.getStyleClass().add("mode-card");

        button.setPrefSize(300, 190);
        button.setMinSize(300, 190);
        button.setMaxSize(300, 190);

        button.setOnAction(
                event -> onModeSelected.accept(mode)
        );

        return button;
    }
}
