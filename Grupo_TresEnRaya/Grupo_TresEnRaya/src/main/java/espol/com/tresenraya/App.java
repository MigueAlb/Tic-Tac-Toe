package espol.com.tresenraya;

import espol.com.tresenraya.model.GameMode;
import espol.com.tresenraya.model.User;
import espol.com.tresenraya.model.UserRepository;
import espol.com.tresenraya.ui.GameModeView;
import espol.com.tresenraya.ui.GameView;
import espol.com.tresenraya.ui.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class App extends Application {
    private UserRepository userRepository;

    @Override
    public void start(Stage stage) {
        userRepository = new UserRepository();
        Scene scene = new Scene(new LoginView(userRepository, user -> showGameModes(stage, user)), 1100, 720);
        scene.getStylesheets().add(App.class.getResource("/styles/game.css").toExternalForm());
        stage.setTitle("Mishi Cósmico");
        stage.setMinWidth(960);
        stage.setMinHeight(680);
        stage.setScene(scene);
        stage.show();
    }

    private void showGameModes(Stage stage, User user) {
        stage.getScene().setRoot(new GameModeView(user, mode -> showGame(stage, user, mode)));
    }

    private void showGame(Stage stage, User user, GameMode mode) {
        stage.getScene().setRoot(new GameView(user, userRepository, mode, () -> showGameModes(stage, user)));
    }

    public static void main(String[] args) {
        launch(args);
    }
}

