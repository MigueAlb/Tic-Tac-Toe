package espol.com.tresenraya.ui;

import espol.com.tresenraya.ai.Decision;
import espol.com.tresenraya.model.GameMode;
import espol.com.tresenraya.model.GameResult;
import espol.com.tresenraya.model.GameSession;
import espol.com.tresenraya.model.Mark;
import espol.com.tresenraya.model.Move;
import espol.com.tresenraya.model.SavedGame;
import espol.com.tresenraya.model.SavedGameRepository;
import espol.com.tresenraya.model.User;
import espol.com.tresenraya.model.UserRepository;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public final class GameView extends StackPane {

    private static final List<String> BACKGROUNDS = List.of(
            "/images/momazos.jpeg",
            "/images/space-cats.jpeg",
            "/images/kitty-burrito.jpg",
            "/images/pizza-cat.jpeg",
            "/images/nyan-cat.jpeg",
            "/images/burger-cat.jpeg",
            "/images/floating-cat.jpeg",
            "/images/laser-cats.jpeg",
            "/images/earth-cat.jpeg",
            "/images/planet-cat.jpeg",
            "/images/close-cat.jpeg",
            "/images/astronaut-cat.png",
            "/images/purple-galaxy.jpeg",
            "/images/cosmic-cat.jpeg",
            "/images/cosmic-koi.gif",
            "/images/pink-cat.jpeg",
            "/images/cosmic-flowers.jpeg"
    );

    private final User user;
    private final UserRepository userRepository;
    private final GameMode gameMode;
    private final Runnable onChangeMode;
    private final Runnable onLogout;
    private final Consumer<SavedGame> onOpenSavedGame;

    private final SavedGameRepository savedGameRepository =
            new SavedGameRepository();

    private final GameSession game = new GameSession();
    private final List<Decision> decisionHistory = new ArrayList<>();

    private final ImageView background = new ImageView();
    private final Button[][] cells = new Button[3][3];

    private final ToggleButton xButton = new ToggleButton("X");
    private final ToggleButton oButton = new ToggleButton("O");

    private final RadioButton firstPlayerStarts = new RadioButton();
    private final RadioButton secondPlayerStarts = new RadioButton();

    private final Label firstPlayerLabel = new Label();
    private final Label secondPlayerLabel = new Label();
    private final Label status = new Label();

    private final TextArea analysis = new TextArea();

    private final StackPane resultOverlay = new StackPane();
    private final Label resultEmoji = new Label();
    private final Label resultTitle = new Label();
    private final Label resultMessage = new Label();

    private final Label gamesValue = new Label();
    private final Label winsValue = new Label();
    private final Label lossesValue = new Label();
    private final Label drawsValue = new Label();

    private final Button decisionTreeButton =
            new Button("VER ÁRBOL DE DECISIONES");

    private final Button decisionAnalysisButton =
            new Button("VER TABLEROS DE MINIMAX");

    private final Button recommendationButton =
            new Button("RECOMENDAR MOVIMIENTO");

    private final Button saveGameButton =
            new Button("GUARDAR PARTIDA");

    private final Button loadGameButton =
            new Button("REABRIR PARTIDA");

    private final Label recommendationLabel = new Label();

    private Timeline backgroundTimer;
    private int backgroundIndex = -1;
    private boolean playing;
    private boolean resultRecorded;

    private Decision recommendationDecision;
    private Decision lastMachineDecision;
    private String currentSaveId;

    public GameView(
            User user,
            UserRepository userRepository,
            GameMode gameMode,
            Runnable onChangeMode,
            Runnable onLogout) {

        this(
                user,
                userRepository,
                gameMode,
                onChangeMode,
                null,
                saved -> {},
                onLogout
        );
    }

    public GameView(
            User user,
            UserRepository userRepository,
            GameMode gameMode,
            Runnable onChangeMode,
            SavedGame savedGame,
            Consumer<SavedGame> onOpenSavedGame) {

        this(
                user,
                userRepository,
                gameMode,
                onChangeMode,
                savedGame,
                onOpenSavedGame,
                () -> {}
        );
    }

    public GameView(
            User user,
            UserRepository userRepository,
            GameMode gameMode,
            Runnable onChangeMode,
            SavedGame savedGame,
            Consumer<SavedGame> onOpenSavedGame,
            Runnable onLogout) {

        this.user = user;
        this.userRepository = userRepository;
        this.gameMode = gameMode;
        this.onChangeMode = onChangeMode;
        this.onOpenSavedGame =
                onOpenSavedGame == null ? saved -> {} : onOpenSavedGame;
        this.onLogout = onLogout == null ? () -> {} : onLogout;

        getStyleClass().add("game-root");

        configureBackground();

        getChildren().addAll(
                background,
                createTint(),
                createLayout(),
                createResultOverlay()
        );

        showFirstBackground();
        startBackgroundRotation();

        configureControlsForMode();

        if (savedGame == null) {
            startGame();
        } else {
            loadSavedGame(savedGame);
        }
    }

    public GameView(User user, UserRepository userRepository) {
        this(
                user,
                userRepository,
                GameMode.PLAYER_VS_MACHINE,
                () -> {},
                null,
                saved -> {},
                () -> {}
        );
    }

    private void configureBackground() {
        background.setPreserveRatio(false);
        background.fitWidthProperty().bind(widthProperty());
        background.fitHeightProperty().bind(heightProperty());

        widthProperty().addListener(
                (observable, oldValue, newValue) -> updateBackgroundCrop()
        );

        heightProperty().addListener(
                (observable, oldValue, newValue) -> updateBackgroundCrop()
        );
    }

    private Region createTint() {
        Region tint = new Region();
        tint.getStyleClass().add("game-tint");
        tint.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return tint;
    }

    private BorderPane createLayout() {
        BorderPane layout = new BorderPane();
        layout.setTop(createHeader());
        layout.setCenter(createBoard());
        layout.setRight(createSidePanel());
        layout.setBottom(createFooter());
        return layout;
    }

    private HBox createHeader() {
        Label eyebrow = new Label("ESTACIÓN ORBITAL 03");
        eyebrow.getStyleClass().add("eyebrow");

        Label title = new Label("Mishi Cósmico");
        title.getStyleClass().add("title");

        Label subtitle = new Label(
                gameMode.displayName()
                        + " · Tres en raya a través de la galaxia"
        );
        subtitle.getStyleClass().add("subtitle");

        VBox titles = new VBox(2, eyebrow, title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label welcome = new Label(
                "Tripulante\n" + user.getName()
        );
        welcome.getStyleClass().add("player-chip");

        Button ranking = createMenuButton("RANKING");
        ranking.setOnAction(event -> showRanking());

        Button sound = createMenuButton(
                SoundPlayer.isEnabled() ? "SONIDO: SÍ" : "SONIDO: NO"
        );
        sound.setOnAction(event -> changeSound(sound));

        Button logout = createMenuButton("CERRAR SESIÓN");
        logout.setOnAction(event -> logout());

        HBox menu = new HBox(
                8, ranking, sound, logout, welcome
        );
        menu.setAlignment(Pos.CENTER_RIGHT);

        HBox header = new HBox(
                20, titles, spacer, menu
        );
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(24, 34, 12, 34));

        return header;
    }

    private Button createMenuButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("menu-button");
        return button;
    }

    private StackPane createBoard() {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("board");
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(9);
        grid.setVgap(9);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                Button cell = new Button();
                cell.getStyleClass().add("cell");
                cell.setDisable(true);

                int selectedRow = row;
                int selectedColumn = column;

                cell.setOnAction(event ->
                        playHumanTurn(
                                new Move(selectedRow, selectedColumn)
                        )
                );

                cells[row][column] = cell;
                grid.add(cell, column, row);
            }
        }

        StackPane holder = new StackPane(grid);
        holder.setPadding(new Insets(10, 24, 18, 34));
        return holder;
    }

    private VBox createSidePanel() {
        Label sectionTitle = new Label("PANEL DE VUELO");
        sectionTitle.getStyleClass().add("section-title");

        Label greeting = new Label(modeGreeting());
        greeting.getStyleClass().add("panel-copy");
        greeting.setWrapText(true);

        ToggleGroup marks = new ToggleGroup();
        xButton.setToggleGroup(marks);
        oButton.setToggleGroup(marks);
        xButton.setSelected(true);

        xButton.getStyleClass().add("mark-choice");
        oButton.getStyleClass().add("mark-choice");

        HBox markChoices = new HBox(10, xButton, oButton);

        ToggleGroup starts = new ToggleGroup();
        firstPlayerStarts.setToggleGroup(starts);
        secondPlayerStarts.setToggleGroup(starts);
        firstPlayerStarts.setSelected(true);

        firstPlayerLabel.getStyleClass().add("panel-copy");
        secondPlayerLabel.getStyleClass().add("panel-copy");

        Button start = new Button("DESPEGAR / NUEVA PARTIDA");
        start.getStyleClass().add("space-button");
        start.setMaxWidth(Double.MAX_VALUE);
        start.setOnAction(event -> startGame());

        Button changeMode = new Button("← CAMBIAR MODO");
        changeMode.getStyleClass().add("ghost-button");
        changeMode.setMaxWidth(Double.MAX_VALUE);
        changeMode.setOnAction(event -> changeMode());

        recommendationLabel.getStyleClass().add("panel-copy");
        recommendationLabel.setWrapText(true);
        recommendationLabel.setText(
                "La recomendación aparecerá en tu turno."
        );

        recommendationButton.getStyleClass().add("ghost-button");
        recommendationButton.setMaxWidth(Double.MAX_VALUE);
        recommendationButton.setOnAction(event -> recommendMove());

        decisionAnalysisButton.getStyleClass().add("ghost-button");
        decisionAnalysisButton.setMaxWidth(Double.MAX_VALUE);
        decisionAnalysisButton.setVisible(false);
        decisionAnalysisButton.setManaged(false);
        decisionAnalysisButton.setOnAction(
                event -> showDecisionAnalysis()
        );

        saveGameButton.getStyleClass().add("ghost-button");
        saveGameButton.setMaxWidth(Double.MAX_VALUE);
        saveGameButton.setOnAction(event -> saveCurrentGame());

        loadGameButton.getStyleClass().add("ghost-button");
        loadGameButton.setMaxWidth(Double.MAX_VALUE);
        loadGameButton.setOnAction(event -> openSavedGameDialog());

        analysis.setEditable(false);
        analysis.setWrapText(true);
        analysis.setPrefRowCount(8);

        TitledPane explanation = new TitledPane(
                "¿Cómo decidió Mishi?",
                analysis
        );
        explanation.setExpanded(false);
        explanation.getStyleClass().add("analysis-pane");

        boolean machineMode = gameMode != GameMode.PLAYER_VS_PLAYER;
        explanation.setVisible(machineMode);
        explanation.setManaged(machineMode);

        Label historyTitle = new Label("MI HISTORIAL");
        historyTitle.getStyleClass().add("section-title");

        HBox history = createHistory();

        VBox panel = new VBox(
                9,
                sectionTitle,
                greeting,
                new Label(modeMarkCaption()),
                markChoices,
                new Label("Primer turno"),
                firstPlayerStarts,
                firstPlayerLabel,
                secondPlayerStarts,
                secondPlayerLabel,
                start,
                changeMode,
                recommendationLabel,
                recommendationButton,
                decisionAnalysisButton,
                saveGameButton,
                loadGameButton,
                historyTitle,
                history,
                explanation
        );

        panel.getStyleClass().add("side-panel");
        panel.setPadding(new Insets(24));
        panel.setPrefWidth(330);
        panel.setMaxWidth(350);

        StackPane.setMargin(
                panel,
                new Insets(6, 32, 18, 4)
        );

        return panel;
    }

    private void configureControlsForMode() {
        if (gameMode == GameMode.PLAYER_VS_MACHINE) {
            firstPlayerLabel.setText("Yo comienzo");
            secondPlayerLabel.setText("Mishi comienza");
        } else if (gameMode == GameMode.PLAYER_VS_PLAYER) {
            firstPlayerLabel.setText("Jugador 1 comienza");
            secondPlayerLabel.setText("Jugador 2 comienza");
        } else {
            firstPlayerLabel.setText("Mishi X comienza");
            secondPlayerLabel.setText("Mishi O comienza");
        }

        firstPlayerStarts.setText("");
        secondPlayerStarts.setText("");

        boolean hasHuman =
                gameMode != GameMode.MACHINE_VS_MACHINE;

        recommendationButton.setVisible(hasHuman);
        recommendationButton.setManaged(hasHuman);

        recommendationLabel.setVisible(hasHuman);
        recommendationLabel.setManaged(hasHuman);
    }

    private String modeGreeting() {
        return switch (gameMode) {
            case PLAYER_VS_PLAYER ->
                    "Dos tripulantes, una galaxia. "
                    + "Tú controlas ambos lados del tablero.";

            case PLAYER_VS_MACHINE ->
                    "Hola, " + user.getName()
                    + ". Elige tu ficha y prepárate "
                    + "para enfrentar a Mishi.";

            case MACHINE_VS_MACHINE ->
                    "Observa a dos Mishi competir. "
                    + "La partida avanzará automáticamente.";
        };
    }

    private String modeMarkCaption() {
        return gameMode == GameMode.PLAYER_VS_MACHINE
                ? "Tu ficha"
                : "Ficha del Jugador 1 / Mishi X";
    }

    private HBox createHistory() {
        VBox games = createStat("Partidas", gamesValue);
        VBox wins = createStat("Ganadas", winsValue);
        VBox losses = createStat("Perdidas", lossesValue);
        VBox draws = createStat("Empates", drawsValue);

        HBox history = new HBox(
                7, games, wins, losses, draws
        );

        updateHistory();
        return history;
    }

    private VBox createStat(String title, Label value) {
        Label label = new Label(title);
        label.getStyleClass().add("stat-name");
        value.getStyleClass().add("stat-value");

        VBox box = new VBox(2, value, label);
        box.getStyleClass().add("stat-box");
        box.setAlignment(Pos.CENTER);

        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private void updateHistory() {
        gamesValue.setText(
                String.valueOf(user.getTotalGames())
        );
        winsValue.setText(
                String.valueOf(user.getWins())
        );
        lossesValue.setText(
                String.valueOf(user.getLosses())
        );
        drawsValue.setText(
                String.valueOf(user.getDraws())
        );
    }

    private HBox createFooter() {
        status.setText(
                "Configura la misión y pulsa Despegar"
        );
        status.getStyleClass().add("status");

        Label orbit = new Label("●  SISTEMAS LISTOS");
        orbit.getStyleClass().add("system-ready");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox footer = new HBox(
                status, spacer, orbit
        );
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(
                new Insets(8, 34, 18, 34)
        );

        return footer;
    }

    private StackPane createResultOverlay() {
        resultOverlay.getStyleClass().add("result-overlay");
        resultOverlay.setVisible(false);
        resultOverlay.setManaged(false);

        resultEmoji.getStyleClass().add("result-emoji");
        resultTitle.getStyleClass().add("result-title");
        resultMessage.getStyleClass().add("result-message");
        resultMessage.setWrapText(true);

        Button again = new Button("OTRA MISIÓN");
        again.getStyleClass().add("space-button");
        again.setOnAction(event -> {
            hideResult();
            startGame();
        });

        decisionTreeButton.getStyleClass().add("ghost-button");
        decisionTreeButton.setVisible(false);
        decisionTreeButton.setManaged(false);
        decisionTreeButton.setOnAction(event -> showDecisionTree());

        Button review = new Button("REVISAR TABLERO");
        review.getStyleClass().add("ghost-button");
        review.setOnAction(event -> hideResult());

        HBox actions = new HBox(
                10, again, decisionTreeButton, review
        );
        actions.setAlignment(Pos.CENTER);

        VBox card = new VBox(
                12,
                resultEmoji,
                resultTitle,
                resultMessage,
                actions
        );

        card.getStyleClass().add("result-card");
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(430);
        card.setPadding(new Insets(34));

        resultOverlay.getChildren().add(card);
        return resultOverlay;
    }

    private void startGame() {
        hideResult();

        if (backgroundTimer != null) {
            changeBackground();
        }

        resultRecorded = false;
        SoundPlayer.playClick();

        Mark playerOneMark =
                xButton.isSelected()
                        ? Mark.X
                        : Mark.O;

        boolean playerOneStarts =
                firstPlayerStarts.isSelected();

        game.start(
                gameMode,
                playerOneMark,
                playerOneStarts
        );

        playing = true;

        analysis.clear();
        decisionHistory.clear();

        recommendationDecision = null;
        lastMachineDecision = null;
        currentSaveId = null;

        decisionTreeButton.setVisible(false);
        decisionTreeButton.setManaged(false);

        decisionAnalysisButton.setVisible(false);
        decisionAnalysisButton.setManaged(false);

        renderBoard();
        updateRecommendationAvailability();
        updateSaveButtons();

        runAutomaticTurnIfNeeded();
    }

    private void playHumanTurn(Move move) {
        if (!playing
                || game.isMachineTurn()
                || !game.board().isEmpty(
                        move.getRow(),
                        move.getColumn())) {
            return;
        }

        try {
            game.playHumanMove(move);
            SoundPlayer.playClick();
        } catch (IllegalArgumentException
                | IllegalStateException exception) {
            status.setText(exception.getMessage());
            return;
        }

        recommendationDecision = null;
        renderBoard();

        if (finishIfNeeded()) {
            return;
        }

        updateRecommendationAvailability();
        runAutomaticTurnIfNeeded();
    }

    private void runAutomaticTurnIfNeeded() {
        if (!playing || !game.isMachineTurn()) {
            updateStatus();
            return;
        }

        setBoardDisabled(true);

        status.setText(
                gameMode == GameMode.MACHINE_VS_MACHINE
                        ? "Mishi está pensando..."
                        : "Mishi consulta las estrellas..."
        );

        PauseTransition pause =
                new PauseTransition(
                        Duration.millis(
                                gameMode
                                        == GameMode.MACHINE_VS_MACHINE
                                        ? 650
                                        : 450
                        )
                );

        pause.setOnFinished(event -> playMachineTurn());
        pause.play();
    }

    private void playMachineTurn() {
        if (!playing || !game.isMachineTurn()) {
            return;
        }

        Decision decision = game.playMachineMove();

        lastMachineDecision = decision;
        recommendationDecision = null;
        decisionHistory.add(decision);

        showDecision(decision);

        decisionAnalysisButton.setVisible(true);
        decisionAnalysisButton.setManaged(true);

        renderBoard();

        if (finishIfNeeded()) {
            return;
        }

        runAutomaticTurnIfNeeded();
    }

    private void showDecision(Decision decision) {
        String actor =
                gameMode == GameMode.MACHINE_VS_MACHINE
                        ? "Mishi"
                        : "Mishi";

        StringBuilder text = new StringBuilder();

        text.append(actor)
                .append(" eligió ")
                .append(decision.getMove().display())
                .append(".\n");

        text.append("Valor minimax: ")
                .append(decision.getMinimaxValue())
                .append("\n\n");

        for (Decision.CandidateScore candidate
                : decision.getCandidates()) {

            text.append("• ")
                    .append(candidate.getMove().display())
                    .append(": ")
                    .append(candidate.getWorstUtility())
                    .append('\n');
        }

        text.append("\nEstados del árbol: ")
                .append(decision.getDecisionTree().size());

        analysis.setText(text.toString());
    }

    private void renderBoard() {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {

                Mark mark = game.board().get(row, column);
                Button cell = cells[row][column];

                cell.setText(mark.symbol());

                cell.getStyleClass().removeAll(
                        "x-mark",
                        "o-mark",
                        "recommended-cell"
                );

                if (mark == Mark.X) {
                    cell.getStyleClass().add("x-mark");
                }

                if (mark == Mark.O) {
                    cell.getStyleClass().add("o-mark");
                }

                if (recommendationDecision != null
                        && recommendationDecision.getMove().getRow() == row
                        && recommendationDecision.getMove().getColumn() == column
                        && mark == Mark.EMPTY) {

                    cell.getStyleClass()
                            .add("recommended-cell");
                }

                boolean humanCanPlay =
                        playing
                        && game.isHumanTurn()
                        && mark == Mark.EMPTY;

                cell.setDisable(!humanCanPlay);
            }
        }
    }

    private boolean finishIfNeeded() {
        if (!game.result().isFinished()) {
            return false;
        }

        playing = false;
        setBoardDisabled(true);

        if (resultRecorded) {
            return true;
        }

        resultRecorded = true;

        if (currentSaveId != null) {
            savedGameRepository.delete(
                    user.getEmail(),
                    currentSaveId
            );
            currentSaveId = null;
        }

        GameResult result = game.result();

        boolean hasDecisionTree =
                !decisionHistory.isEmpty();

        decisionTreeButton.setVisible(hasDecisionTree);
        decisionTreeButton.setManaged(hasDecisionTree);

        updateSaveButtons();

        if (gameMode == GameMode.MACHINE_VS_MACHINE) {
            showResult(
                    "⚔",
                    resultTitleForMachineGame(result),
                    "La simulación terminó. "
                    + "Puedes revisar el tablero "
                    + "o lanzar otra partida."
            );
            return true;
        }

        if (result == GameResult.DRAW) {
            user.addDraw();

            showResult(
                    "✦",
                    "Órbita compartida",
                    "Empate, "
                    + user.getName()
                    + ". Ningún tripulante conquistó esta galaxia."
            );

        } else if (gameMode == GameMode.PLAYER_VS_PLAYER) {

            Mark winner =
                    result == GameResult.X_WINS
                            ? Mark.X
                            : Mark.O;

            if (winner == game.playerOneMark()) {
                user.addWin();

                showResult(
                        "★",
                        "¡Jugador 1 gana!",
                        "La ficha "
                        + winner.symbol()
                        + " conquistó la galaxia."
                );
            } else {
                user.addLoss();

                showResult(
                        "★",
                        "¡Jugador 2 gana!",
                        "La ficha "
                        + winner.symbol()
                        + " conquistó la galaxia."
                );
            }

        } else {

            Mark winner =
                    result == GameResult.X_WINS
                            ? Mark.X
                            : Mark.O;

            if (winner == game.humanMark()) {
                user.addWin();

                showResult(
                        "★",
                        "¡Misión cumplida!",
                        "Ganaste, "
                        + user.getName()
                        + ". Mishi reconoce tu destreza espacial."
                );
            } else {
                user.addLoss();

                showResult(
                        "☾",
                        "Mishi conquistó la galaxia",
                        "Esta ronda fue para la computadora. "
                        + "Puedes revisar el tablero o iniciar otra misión."
                );
            }
        }

        userRepository.saveUsers();
        updateHistory();

        return true;
    }

    private String resultTitleForMachineGame(
            GameResult result) {

        return switch (result) {
            case DRAW -> "Equilibrio cósmico";
            case X_WINS -> "Gana Mishi X";
            case O_WINS -> "Gana Mishi O";
            case IN_PROGRESS -> "Partida en curso";
        };
    }

    private void showDecisionTree() {
        if (decisionHistory.isEmpty()) {
            return;
        }

        DecisionTreeView treeView =
                new DecisionTreeView(
                        user.getName(),
                        gameMode,
                        decisionHistory
                );

        treeView.show();
    }

    private void showDecisionAnalysis() {
        if (lastMachineDecision == null) {
            return;
        }

        Mark machine =
                gameMode == GameMode.MACHINE_VS_MACHINE
                        ? game.currentTurn().opposite()
                        : game.computerMark();

        DecisionAnalysisView.show(
                gameMode.displayName(),
                lastMachineDecision,
                machine
        );
    }

    private void recommendMove() {
        if (!playing
                || gameMode == GameMode.MACHINE_VS_MACHINE
                || !game.isHumanTurn()) {
            return;
        }

        try {
            recommendationDecision =
                    game.recommendMove();

            Move move =
                    recommendationDecision.getMove();

            recommendationLabel.setText(
                    "Te conviene jugar en "
                    + move.display()
                    + ". Utilidad garantizada: "
                    + recommendationDecision.getMinimaxValue()
                    + ". La recomendación no realiza "
                    + "el movimiento por ti."
            );

            renderBoard();

        } catch (IllegalStateException exception) {
            recommendationLabel.setText(
                    exception.getMessage()
            );
        }
    }

    private void updateRecommendationAvailability() {
        boolean available =
                playing
                && gameMode != GameMode.MACHINE_VS_MACHINE
                && game.isHumanTurn();

        recommendationButton.setDisable(!available);

        if (!available) {
            recommendationLabel.setText(
                    "La recomendación aparecerá en tu turno."
            );
        }
    }

    private void updateSaveButtons() {
        saveGameButton.setDisable(
                !playing || game.result().isFinished()
        );
        loadGameButton.setDisable(false);
    }

    private void saveCurrentGame() {
        if (!playing || game.result().isFinished()) {
            return;
        }

        try {
            SavedGame save =
                    game.createSave(
                            user.getEmail(),
                            user.getName(),
                            currentSaveId == null
                                    ? java.util.UUID
                                            .randomUUID()
                                            .toString()
                                    : currentSaveId
                    );

            currentSaveId = save.getId();

            savedGameRepository.save(save);

            status.setText(
                    "Partida guardada. "
                    + "Puedes reabrirla cuando quieras."
            );

        } catch (RuntimeException exception) {
            status.setText(
                    "No se pudo guardar la partida: "
                    + exception.getMessage()
            );
        }
    }

    private void openSavedGameDialog() {
        List<SavedGame> saves =
                savedGameRepository.findByUser(
                        user.getEmail()
                );

        if (saves.isEmpty()) {
            status.setText(
                    "No tienes partidas guardadas a medio jugar."
            );
            return;
        }

        ChoiceDialog<SavedGame> dialog =
                new ChoiceDialog<>(
                        saves.get(0),
                        saves
                );

        dialog.setTitle("Reabrir partida");
        dialog.setHeaderText(
                "Selecciona una partida guardada"
        );
        dialog.setContentText("Partida:");

        Optional<SavedGame> selected =
                dialog.showAndWait();

        selected.ifPresent(
                this::openSelectedSavedGame
        );
    }

    private void openSelectedSavedGame(
            SavedGame savedGame) {

        if (savedGame.getGameMode() != gameMode) {
            onOpenSavedGame.accept(savedGame);
            return;
        }

        loadSavedGame(savedGame);
    }

    private void loadSavedGame(
            SavedGame savedGame) {

        try {
            if (savedGame.getGameMode() != gameMode) {
                onOpenSavedGame.accept(savedGame);
                return;
            }

            game.restore(savedGame);

            currentSaveId = savedGame.getId();

            xButton.setSelected(
                    savedGame.getPlayerOneMark() == Mark.X
            );

            oButton.setSelected(
                    savedGame.getPlayerOneMark() == Mark.O
            );

            firstPlayerStarts.setSelected(
                    savedGame.isPlayerOneStarts()
            );

            secondPlayerStarts.setSelected(
                    !savedGame.isPlayerOneStarts()
            );

            hideResult();

            playing = true;
            resultRecorded = false;

            analysis.clear();
            decisionHistory.clear();

            decisionHistory.addAll(
                    game.rebuildMachineDecisions()
            );

            recommendationDecision = null;

            lastMachineDecision =
                    decisionHistory.isEmpty()
                            ? null
                            : decisionHistory.get(
                                    decisionHistory.size() - 1
                            );

            boolean hasPreviousAnalysis =
                    lastMachineDecision != null;

            decisionTreeButton.setVisible(hasPreviousAnalysis);
            decisionTreeButton.setManaged(hasPreviousAnalysis);

            decisionAnalysisButton.setVisible(hasPreviousAnalysis);
            decisionAnalysisButton.setManaged(hasPreviousAnalysis);

            status.setText(
                    "Partida reabierta. Continúa desde el turno "
                    + game.currentTurn().symbol()
                    + "."
            );

            renderBoard();
            updateRecommendationAvailability();
            updateSaveButtons();

            runAutomaticTurnIfNeeded();

        } catch (RuntimeException exception) {
            status.setText(
                    "No se pudo reabrir la partida: "
                    + exception.getMessage()
            );
        }
    }

    private void showResult(
            String emoji,
            String title,
            String message) {

        resultEmoji.setText(emoji);
        resultTitle.setText(title);
        resultMessage.setText(message);

        resultOverlay.setManaged(true);
        resultOverlay.setVisible(true);
    }

    private void hideResult() {
        resultOverlay.setVisible(false);
        resultOverlay.setManaged(false);
    }

    private void updateStatus() {
        if (!playing) {
            return;
        }

        if (game.isMachineTurn()) {
            status.setText(
                    gameMode == GameMode.MACHINE_VS_MACHINE
                            ? "Mishi está pensando..."
                            : "Turno de Mishi"
            );

        } else if (gameMode == GameMode.PLAYER_VS_PLAYER) {

            status.setText(
                    "Turno del Jugador "
                    + (
                        game.currentTurn()
                                == game.playerOneMark()
                                ? "1"
                                : "2"
                    )
                    + " ("
                    + game.currentTurn().symbol()
                    + ")"
            );

        } else {

            status.setText(
                    user.getName()
                    + ", es tu turno: "
                    + "elige una órbita libre"
            );
        }

        updateRecommendationAvailability();
        updateSaveButtons();
        renderBoard();
    }

    private void setBoardDisabled(boolean disabled) {
        for (Button[] row : cells) {
            for (Button cell : row) {
                cell.setDisable(disabled);
            }
        }
    }

    private void showFirstBackground() {
        backgroundIndex = 0;

        background.setImage(
                new Image(
                        getClass()
                                .getResourceAsStream(
                                        BACKGROUNDS.get(backgroundIndex)
                                )
                )
        );

        updateBackgroundCrop();
    }

    private void startBackgroundRotation() {
        KeyFrame change =
                new KeyFrame(
                        Duration.seconds(10),
                        event -> changeBackground()
                );

        backgroundTimer = new Timeline(change);
        backgroundTimer.setCycleCount(
                Timeline.INDEFINITE
        );
        backgroundTimer.play();
    }

    private void changeBackground() {
        if (backgroundTimer == null) {
            return;
        }

        FadeTransition fadeOut =
                new FadeTransition(
                        Duration.seconds(1),
                        background
                );

        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        fadeOut.setOnFinished(event -> {

            backgroundIndex++;

            if (backgroundIndex >= BACKGROUNDS.size()) {
                backgroundIndex = 0;
            }

            background.setImage(
                    new Image(
                            getClass()
                                    .getResourceAsStream(
                                            BACKGROUNDS.get(
                                                    backgroundIndex
                                            )
                                    )
                    )
            );

            updateBackgroundCrop();

            FadeTransition fadeIn =
                    new FadeTransition(
                            Duration.seconds(1),
                            background
                    );

            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        });

        fadeOut.play();
    }

    private void showRanking() {
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("ranking-overlay");

        Label title =
                new Label("Ranking de tripulantes");
        title.getStyleClass().add("ranking-title");

        Label note =
                new Label(
                        "Ordenado por victorias y fecha de registro"
                );
        note.getStyleClass().add("ranking-note");

        VBox positions = createRankingList();

        ScrollPane list = new ScrollPane(positions);
        list.getStyleClass().add("ranking-scroll");
        list.setFitToWidth(true);
        list.setMaxHeight(300);

        Button close = new Button("CERRAR");
        close.getStyleClass().add("space-button");
        close.setMaxWidth(Double.MAX_VALUE);

        close.setOnAction(
                event -> getChildren().remove(overlay)
        );

        VBox card = new VBox(
                15,
                title,
                note,
                list,
                close
        );

        card.getStyleClass().add("ranking-card");
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(28));
        card.setMaxWidth(430);

        overlay.getChildren().add(card);
        getChildren().add(overlay);

        SoundPlayer.playClick();
    }

    private VBox createRankingList() {
        VBox positions = new VBox(9);

        ArrayList<User> ranking =
                userRepository.getRanking();

        if (ranking.isEmpty()) {
            Label empty =
                    new Label(
                            "Todavía no hay jugadores en el ranking"
                    );

            empty.getStyleClass().add("ranking-note");
            positions.getChildren().add(empty);

            return positions;
        }

        for (int i = 0; i < ranking.size(); i++) {
            User rankedUser = ranking.get(i);

            positions.getChildren().add(
                    createRankingRow(
                            String.valueOf(i + 1),
                            rankedUser.getName(),
                            rankedUser.getWins()
                                    + " victorias"
                    )
            );
        }

        return positions;
    }

    private HBox createRankingRow(
            String position,
            String name,
            String score) {

        Label number = new Label(position);
        number.getStyleClass().add("ranking-number");

        Label player = new Label(name);
        player.getStyleClass().add("ranking-player");

        Label result = new Label(score);
        result.getStyleClass().add("ranking-score");

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        HBox row = new HBox(
                12,
                number,
                player,
                space,
                result
        );

        row.getStyleClass().add("ranking-row");
        row.setAlignment(Pos.CENTER_LEFT);

        return row;
    }

    private void changeSound(Button soundButton) {
        if (SoundPlayer.isEnabled()) {
            SoundPlayer.setEnabled(false);
            soundButton.setText("SONIDO: NO");
        } else {
            SoundPlayer.setEnabled(true);
            soundButton.setText("SONIDO: SÍ");
            SoundPlayer.playClick();
        }
    }

    private void changeMode() {
        if (backgroundTimer != null) {
            backgroundTimer.stop();
        }

        onChangeMode.run();
    }

    private void logout() {
        if (backgroundTimer != null) {
            backgroundTimer.stop();
        }

        onLogout.run();
    }

    private void updateBackgroundCrop() {
        Image image = background.getImage();

        if (image == null
                || getWidth() <= 0
                || getHeight() <= 0) {
            return;
        }

        double imageRatio =
                image.getWidth() / image.getHeight();

        double viewRatio =
                getWidth() / getHeight();

        if (imageRatio > viewRatio) {

            double cropWidth =
                    image.getHeight() * viewRatio;

            background.setViewport(
                    new Rectangle2D(
                            (image.getWidth() - cropWidth) / 2,
                            0,
                            cropWidth,
                            image.getHeight()
                    )
            );

        } else {

            double cropHeight =
                    image.getWidth() / viewRatio;

            background.setViewport(
                    new Rectangle2D(
                            0,
                            (image.getHeight() - cropHeight) / 2,
                            image.getWidth(),
                            cropHeight
                    )
            );
        }
    }
}

