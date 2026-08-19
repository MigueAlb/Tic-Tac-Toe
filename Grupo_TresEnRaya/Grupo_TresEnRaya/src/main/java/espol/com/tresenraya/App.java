package espol.com.tresenraya;

import espol.com.tresenraya.model.GameMode;
import espol.com.tresenraya.model.User;
import espol.com.tresenraya.model.UserRepository;
import espol.com.tresenraya.model.SavedGame;
import espol.com.tresenraya.ui.GameModeView;
import espol.com.tresenraya.ui.GameView;
import espol.com.tresenraya.ui.LoginView;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class App extends Application {
    private UserRepository userRepository;
    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        userRepository = new UserRepository();
        Scene scene = new Scene(new LoginView(userRepository, user -> showGameModes(user)), 1100, 720);
        scene.getStylesheets().add(App.class.getResource("/styles/game.css").toExternalForm());
        stage.setTitle("Mishi Cósmico");
        stage.setMinWidth(960);
        stage.setMinHeight(680);
        stage.setScene(scene);
        stage.show();
    }
    
    private void showLogin() {
        LoginView loginView = new LoginView(userRepository, this::showGameModes);
        if (stage.getScene() == null) {
            Scene scene = new Scene(loginView, 1100, 720);
            scene.getStylesheets().add(App.class.getResource("/styles/game.css").toExternalForm());
            stage.setScene(scene);
        } else {
            stage.getScene().setRoot(loginView);
        }
    }

    private void showGameModes(User user) {
        stage.getScene().setRoot(new GameModeView(user, mode -> showGame(user, mode)));
    }

    private void showGame(User user, GameMode mode) {
        stage.getScene().setRoot(new GameView(
                user, userRepository, mode,
                () -> showGameModes(user),
                null,
                saved -> showSavedGame(stage, user, saved),
                () ->
                {
                    showLogin();
                }));
    }

    private void showSavedGame(Stage stage, User user, SavedGame savedGame) {
        stage.getScene().setRoot(new GameView(
                user, userRepository, savedGame.getGameMode(),
                () -> showGameModes(user),
                savedGame,
                saved -> showSavedGame(stage, user, saved),
                () ->
                {
                    showLogin();
                }));
    }

    public static void main(String[] args) {
        launch(args);
    }
}