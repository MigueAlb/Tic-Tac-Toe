package espol.com.tresenraya.ui;

import espol.com.tresenraya.model.User;
import espol.com.tresenraya.model.UserRepository;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;
import java.util.function.Consumer;

public final class LoginView extends StackPane {
    private static final List<String> BACKGROUNDS = List.of(
            "/images/momazos.jpeg", "/images/space-cats.jpeg", "/images/kitty-burrito.jpg",
            "/images/pizza-cat.jpeg", "/images/nyan-cat.jpeg", "/images/burger-cat.jpeg",
            "/images/floating-cat.jpeg", "/images/laser-cats.jpeg", "/images/earth-cat.jpeg",
            "/images/planet-cat.jpeg", "/images/close-cat.jpeg");

    private final UserRepository repository;
    private final Consumer<User> onEnter;
    private final VBox card;
    private final ImageView background = new ImageView();
    private Timeline backgroundTimer;
    private int backgroundIndex;
    private String registrationEmail;
    private String registrationPassword;

    public LoginView(UserRepository repository, Consumer<User> onEnter) {
        this.repository = repository;
        this.onEnter = onEnter;
        getStyleClass().add("login-root");

        background.setPreserveRatio(false);
        background.fitWidthProperty().bind(widthProperty());
        background.fitHeightProperty().bind(heightProperty());

        StackPane shade = new StackPane();
        shade.getStyleClass().add("login-shade");

        card = new VBox(13);
        card.getStyleClass().add("login-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(36));
        card.setMaxWidth(440);
        card.setMaxHeight(USE_PREF_SIZE);
        StackPane.setAlignment(card, Pos.CENTER_LEFT);
        StackPane.setMargin(card, new Insets(40, 40, 40, 80));

        getChildren().addAll(background, shade, card);
        showFirstBackground();
        startBackgroundRotation();
        showLoginForm();
    }

    private void showLoginForm() {
        Label badge = createBadge("MISIÓN: TRES EN RAYA");
        Label title = createTitle("Iniciar sesión");
        Label subtitle = createSubtitle("Ingresa con tu correo y contraseña");
        TextField email = new TextField();
        email.setPromptText("Correo electrónico");
        email.getStyleClass().add("name-field");
        PasswordField password = new PasswordField();
        password.setPromptText("Contraseña");
        password.getStyleClass().add("name-field");
        Label error = createError();

        Button login = new Button("INGRESAR A LA NAVE");
        login.getStyleClass().add("space-button");
        login.setMaxWidth(Double.MAX_VALUE);
        login.setOnAction(event -> login(email, password, error));

        Button register = new Button("CREAR CUENTA NUEVA");
        register.getStyleClass().add("ghost-button");
        register.setMaxWidth(Double.MAX_VALUE);
        register.setOnAction(event -> beginRegistration(email, password, error));

        card.getChildren().setAll(badge, title, subtitle, email, password, error, login, register);
    }

    private void login(TextField emailField, PasswordField passwordField, Label error) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        if (email.isEmpty() || password.isEmpty()) {
            error.setText("Completa el correo y la contraseña");
            return;
        }

        if (repository.findByEmail(email) == null) {
            error.setText("Esta cuenta no está registrada. Debes crear una cuenta.");
            SoundPlayer.playError();
            return;
        }

        User user = repository.login(email, password);
        if (user == null) {
            error.setText("La contraseña es incorrecta");
            SoundPlayer.playError();
            return;
        }
        SoundPlayer.playSuccess();
        stopBackgroundRotation();
        onEnter.accept(user);
    }

    private void beginRegistration(TextField emailField, PasswordField passwordField, Label error) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        if (!isValidEmail(email)) {
            error.setText("Escribe un correo válido");
            return;
        }
        if (password.length() < 4) {
            error.setText("La contraseña debe tener al menos 4 caracteres");
            return;
        }
        if (repository.findByEmail(email) != null) {
            error.setText("Ese correo ya está registrado");
            return;
        }

        registrationEmail = email;
        registrationPassword = password;
        SoundPlayer.playClick();
        showNameForm();
    }

    private void showNameForm() {
        Label badge = createBadge("NUEVO TRIPULANTE");
        Label title = createTitle("¡Bienvenido!");
        Label subtitle = createSubtitle("¿Cómo quieres que te llamemos durante la misión?");
        TextField name = new TextField();
        name.setPromptText("Nombre del tripulante");
        name.getStyleClass().add("name-field");
        Label error = createError();

        Button finish = new Button("COMPLETAR REGISTRO");
        finish.getStyleClass().add("space-button");
        finish.setMaxWidth(Double.MAX_VALUE);
        finish.setOnAction(event -> finishRegistration(name, error));

        Button back = new Button("VOLVER");
        back.getStyleClass().add("ghost-button");
        back.setMaxWidth(Double.MAX_VALUE);
        back.setOnAction(event -> showLoginForm());

        card.getChildren().setAll(badge, title, subtitle, name, error, finish, back);
        name.requestFocus();
    }

    private void finishRegistration(TextField nameField, Label error) {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            error.setText("Escribe tu nombre para continuar");
            return;
        }

        User user = repository.register(registrationEmail, registrationPassword, name);
        if (user == null) {
            error.setText("No se pudo crear el usuario");
            SoundPlayer.playError();
            return;
        }
        SoundPlayer.playSuccess();
        stopBackgroundRotation();
        onEnter.accept(user);
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }

    private Label createBadge(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("mission-badge");
        return label;
    }

    private Label createTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("login-title");
        return label;
    }

    private Label createSubtitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("login-subtitle");
        label.setWrapText(true);
        return label;
    }

    private Label createError() {
        Label label = new Label();
        label.getStyleClass().add("form-error");
        return label;
    }

    // Muestra el primer fondo
    private void showFirstBackground() {
        backgroundIndex = 0;
        String imagePath = BACKGROUNDS.get(backgroundIndex);
        Image image = new Image(getClass().getResourceAsStream(imagePath));
        background.setImage(image);
    }

    // Cambia el fondo cada diez segundos
    private void startBackgroundRotation() {
        KeyFrame change = new KeyFrame(Duration.seconds(10), event -> changeBackground());
        backgroundTimer = new Timeline(change);
        backgroundTimer.setCycleCount(Timeline.INDEFINITE);
        backgroundTimer.play();
    }

    // Cambia la imagen con una transicion suave
    private void changeBackground() {
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), background);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(event -> showNextBackground());
        fadeOut.play();
    }

    private void showNextBackground() {
        backgroundIndex++;
        if (backgroundIndex >= BACKGROUNDS.size()) {
            backgroundIndex = 0;
        }

        String imagePath = BACKGROUNDS.get(backgroundIndex);
        Image image = new Image(getClass().getResourceAsStream(imagePath));
        background.setImage(image);

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), background);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void stopBackgroundRotation() {
        if (backgroundTimer != null) {
            backgroundTimer.stop();
        }
    }
}
