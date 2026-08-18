package espol.com.tresenraya.ui;

import espol.com.tresenraya.model.User;
import espol.com.tresenraya.model.UserRepository;
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

import java.util.function.Consumer;

public final class LoginView extends StackPane {
    private final UserRepository repository;
    private final Consumer<User> onEnter;
    private final VBox card;
    private String registrationEmail;
    private String registrationPassword;

    public LoginView(UserRepository repository, Consumer<User> onEnter) {
        this.repository = repository;
        this.onEnter = onEnter;
        getStyleClass().add("login-root");

        Image image = new Image(getClass().getResourceAsStream("/images/momazos.jpeg"));
        ImageView background = new ImageView(image);
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
}
