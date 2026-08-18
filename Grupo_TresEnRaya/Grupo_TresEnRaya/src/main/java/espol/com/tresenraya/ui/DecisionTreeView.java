package espol.com.tresenraya.ui;

import espol.com.tresenraya.ai.BoardState;
import espol.com.tresenraya.ai.Decision;
import espol.com.tresenraya.ai.UtilityCalculator;
import espol.com.tresenraya.model.Board;
import espol.com.tresenraya.model.GameMode;
import espol.com.tresenraya.model.Mark;
import espol.com.tresenraya.model.Move;
import espol.com.tresenraya.structures.Tree;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

import javafx.scene.text.Font;

import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;



public final class DecisionTreeView extends Stage {

    private static final double NODE_W = 92;
    private static final double NODE_H = 92;

    private static final double H_GAP = 22;
    private static final double V_GAP = 115;

    private static final double TURN_GAP = 150;

    private static final double PADDING = 80;

    private static final double MIN_SCALE = 0.15;
    private static final double MAX_SCALE = 3.0;

    private static final double ZOOM_FACTOR = 1.12;


    /*
     * Colores de los jugadores.
     */

    private static final Color PLAYER_1 =
            Color.web("#66D9FF");

    private static final Color PLAYER_2 =
            Color.web("#FF5964");

    private static final Color BOARD_LINE =
            Color.web("#DDE7F2");

    private static final Color TEXT =
            Color.web("#EAF4FF");

    private static final Color MUTED =
            Color.web("#8EA8BF");

    private static final Color BACKGROUND =
            Color.web("#06101D");

    private static final Color SELECTED =
            Color.WHITE;


    /*
     * ------------------------------------------------------------
     * DATOS
     * ------------------------------------------------------------
     */

    private final String playerName;

    private final GameMode gameMode;

    /**
     * Ficha correspondiente al Jugador 1.
     *
     * La ficha del Jugador 2 será automáticamente la opuesta.
     */
    private final Mark playerOneMark;

    /**
     * Todos los turnos reales realizados durante la partida.
     */
    private final List<TurnRecord> turns;


    /*
     * ------------------------------------------------------------
     * CANVAS Y CONTROLES
     * ------------------------------------------------------------
     */

    private final Canvas canvas =
            new Canvas(1200, 800);

    private final StackPane canvasHolder =
            new StackPane(canvas);

    private final Label zoomLabel =
            new Label("100%");


    /*
     * ------------------------------------------------------------
     * ZOOM / DESPLAZAMIENTO
     * ------------------------------------------------------------
     */

    private double scale = 1.0;

    private double offsetX = 0;

    private double offsetY = 0;

    private double pressX;

    private double pressY;

    private double pressOffsetX;

    private double pressOffsetY;


    /*
     * Dimensiones calculadas del contenido.
     */

    private double contentWidth;

    private double contentHeight;


    /*
     * ------------------------------------------------------------
     * CONSTRUCTOR PRINCIPAL
     * ------------------------------------------------------------
     */

    public DecisionTreeView(
            String playerName,
            GameMode gameMode,
            Mark playerOneMark,
            List<TurnRecord> turns) {

        this.playerName = playerName;

        this.gameMode = gameMode;

        this.playerOneMark = playerOneMark;

        this.turns = turns == null
                ? List.of()
                : List.copyOf(turns);


        setTitle(
                "Árbol de decisiones · Mishi Cósmico"
        );

        initModality(
                Modality.APPLICATION_MODAL
        );

        setMaximized(true);

        buildScene();
    }


    /*
     * ------------------------------------------------------------
     * CONSTRUCTOR DE COMPATIBILIDAD
     * ------------------------------------------------------------
     *
     * Permite que cualquier código anterior que todavía utilice:
     *
     * new DecisionTreeView(nombre, modo, decisions)
     *
     * continúe funcionando.
     */

    public DecisionTreeView(
            String playerName,
            GameMode gameMode,
            List<Decision> decisions) {

        this.playerName = playerName;

        this.gameMode = gameMode;

        this.playerOneMark = Mark.X;


        List<TurnRecord> converted =
                new ArrayList<>();


        if (decisions != null) {

            for (Decision decision : decisions) {

                if (decision.getDecisionTree() == null
                        || decision.getDecisionTree().isEmpty()) {

                    continue;
                }


                BoardState rootState =
                        decision.getDecisionTree()
                                .getRoot()
                                .getContent();


                Board board =
                        rootState.getBoard();

                Mark actor = Mark.EMPTY;


                for (Tree<BoardState> child
                        : decision.getDecisionTree()
                                .getRoot()
                                .getChildren()) {

                    BoardState state =
                            child.getRoot().getContent();


                    if (state.getMove() != null
                            && state.getMove()
                                    .equals(decision.getMove())) {

                        actor =
                                state.getPlayerWhoMoved();

                        break;
                    }
                }


                if (actor != Mark.EMPTY) {

                    converted.add(
                            new TurnRecord(
                                    board,
                                    actor,
                                    decision.getMove(),
                                    decision
                            )
                    );
                }
            }
        }


        this.turns =
                List.copyOf(converted);

        buildScene();
    }


    /*
     * ------------------------------------------------------------
     * MOSTRAR
     * ------------------------------------------------------------
     */

    public void showTree() {
        showAndWait();
    }


    /*
     * ------------------------------------------------------------
     * CREACIÓN DE LA ESCENA
     * ------------------------------------------------------------
     */

    private void buildScene() {

        BorderPane root =
                new BorderPane();

        root.setStyle(
                "-fx-background-color: #06101D;"
        );


        root.setTop(
                createTopBar()
        );


        canvas.setFocusTraversable(true);


        canvas.widthProperty()
                .bind(
                        canvasHolder.widthProperty()
                );

        canvas.heightProperty()
                .bind(
                        canvasHolder.heightProperty()
                );


        canvasHolder.setStyle(
                "-fx-background-color: #06101D;"
        );


        root.setCenter(canvasHolder);


        root.setBottom(
                createLegend()
        );


        Scene scene =
                new Scene(
                        root,
                        1400,
                        850,
                        BACKGROUND
                );


        configureMouse();


        setScene(scene);


        canvas.widthProperty()
                .addListener(
                        (obs, oldValue, newValue)
                                -> draw()
                );


        canvas.heightProperty()
                .addListener(
                        (obs, oldValue, newValue)
                                -> draw()
                );


        draw();
    }


    /*
     * ------------------------------------------------------------
     * BARRA SUPERIOR
     * ------------------------------------------------------------
     */

    private VBox createTopBar() {

        Label title =
                new Label(
                        "ÁRBOL DE DECISIONES"
                );

        title.setStyle(
                "-fx-text-fill: #EAF4FF;"
                        + "-fx-font-size: 22px;"
                        + "-fx-font-weight: bold;"
        );


        Label info =
                new Label(
                        gameMode.displayName()
                                + "  ·  "
                                + turns.size()
                                + " turnos"
                                + "  ·  "
                                + playerName
                );

        info.setStyle(
                "-fx-text-fill: #8EA8BF;"
                        + "-fx-font-size: 13px;"
        );


        Label help =
                new Label(
                        "Rueda: zoom   ·   Arrastrar: desplazar   ·   Doble clic: ajustar"
                );

        help.setStyle(
                "-fx-text-fill: #8EA8BF;"
                        + "-fx-font-size: 12px;"
        );


        Button fit =
                new Button("AJUSTAR");

        Button minus =
                new Button("−");

        Button plus =
                new Button("+");

        Button full =
                new Button("PANTALLA COMPLETA");


        fit.setOnAction(
                event -> fitToScreen()
        );

        minus.setOnAction(
                event ->
                        zoomFromCenter(
                                1.0 / ZOOM_FACTOR
                        )
        );

        plus.setOnAction(
                event ->
                        zoomFromCenter(
                                ZOOM_FACTOR
                        )
        );

        full.setOnAction(
                event ->
                        setFullScreen(
                                !isFullScreen()
                        )
        );


        zoomLabel.setStyle(
                "-fx-text-fill: #EAF4FF;"
                        + "-fx-font-size: 13px;"
                        + "-fx-font-weight: bold;"
        );

        zoomLabel.setMinWidth(58);

        zoomLabel.setAlignment(
                Pos.CENTER
        );


        styleToolbarButton(fit);
        styleToolbarButton(minus);
        styleToolbarButton(plus);
        styleToolbarButton(full);


        HBox controls =
                new HBox(
                        7,
                        fit,
                        minus,
                        zoomLabel,
                        plus,
                        full
                );

        controls.setAlignment(
                Pos.CENTER_RIGHT
        );


        VBox information =
                new VBox(
                        3,
                        title,
                        info
                );


        HBox line =
                new HBox(
                        20,
                        information,
                        help,
                        controls
                );


        line.setAlignment(
                Pos.CENTER_LEFT
        );


        HBox.setHgrow(
                help,
                Priority.ALWAYS
        );


        line.setPadding(
                new Insets(
                        18,
                        24,
                        14,
                        24
                )
        );


        line.setStyle(
                "-fx-background-color:"
                        + "rgba(8,20,34,0.96);"
        );


        return new VBox(line);
    }


    /*
     * ------------------------------------------------------------
     * LEYENDA
     * ------------------------------------------------------------
     */

    private HBox createLegend() {

        Label player1 =
                new Label(
                        "□ Jugador 1 ("
                                + playerOneMark.symbol()
                                + ")"
                );

        Label player2 =
                new Label(
                        "□ Jugador 2 ("
                                + playerOneMark
                                        .opposite()
                                        .symbol()
                                + ")"
                );


        Label explanation =
                new Label(
                        "Estado actual → "
                                + "movimientos posibles → "
                                + "respuestas posibles"
                );


        player1.setStyle(
                "-fx-text-fill: #66D9FF;"
                        + "-fx-font-size: 13px;"
                        + "-fx-font-weight: bold;"
        );


        player2.setStyle(
                "-fx-text-fill: #FF5964;"
                        + "-fx-font-size: 13px;"
                        + "-fx-font-weight: bold;"
        );


        explanation.setStyle(
                "-fx-text-fill: #8EA8BF;"
                        + "-fx-font-size: 12px;"
        );


        HBox box =
                new HBox(
                        22,
                        player1,
                        player2,
                        explanation
                );


        box.setAlignment(
                Pos.CENTER_LEFT
        );


        box.setPadding(
                new Insets(
                        12,
                        24,
                        14,
                        24
                )
        );


        box.setStyle(
                "-fx-background-color:"
                        + "rgba(8,20,34,0.96);"
        );


        return box;
    }


    /*
     * ------------------------------------------------------------
     * ESTILO DE BOTONES
     * ------------------------------------------------------------
     */

    private void styleToolbarButton(
            Button button) {

        button.setStyle(
                "-fx-background-color: #102235;"
                        + "-fx-text-fill: #EAF4FF;"
                        + "-fx-border-color: #29435A;"
                        + "-fx-border-radius: 6;"
                        + "-fx-background-radius: 6;"
                        + "-fx-padding: 7 11 7 11;"
        );


        button.setTooltip(
                new Tooltip(
                        button.getText()
                )
        );
    }


    /*
     * ------------------------------------------------------------
     * MOUSE
     * ------------------------------------------------------------
     */

    private void configureMouse() {

        canvas.addEventFilter(
                ScrollEvent.SCROLL,
                this::handleScroll
        );


        canvas.addEventHandler(
                MouseEvent.MOUSE_PRESSED,
                event -> {

                    if (event.getButton()
                            != MouseButton.PRIMARY) {

                        return;
                    }


                    pressX =
                            event.getX();

                    pressY =
                            event.getY();


                    pressOffsetX =
                            offsetX;

                    pressOffsetY =
                            offsetY;


                    canvas.setCursor(
                            javafx.scene.Cursor
                                    .CLOSED_HAND
                    );
                }
        );


        canvas.addEventHandler(
                MouseEvent.MOUSE_DRAGGED,
                event -> {

                    if (!event.isPrimaryButtonDown()) {
                        return;
                    }


                    offsetX =
                            pressOffsetX
                                    + event.getX()
                                    - pressX;

                    offsetY =
                            pressOffsetY
                                    + event.getY()
                                    - pressY;


                    draw();
                }
        );


        canvas.addEventHandler(
                MouseEvent.MOUSE_RELEASED,
                event ->
                        canvas.setCursor(
                                javafx.scene.Cursor.DEFAULT
                        )
        );


        canvas.addEventHandler(
                MouseEvent.MOUSE_CLICKED,
                event -> {

                    if (event.getClickCount() == 2) {
                        fitToScreen();
                    }
                }
        );
    }


    /*
     * ------------------------------------------------------------
     * ZOOM
     * ------------------------------------------------------------
     */

    private void handleScroll(
            ScrollEvent event) {

        double oldScale =
                scale;


        double factor =
                event.getDeltaY() > 0
                        ? ZOOM_FACTOR
                        : 1.0 / ZOOM_FACTOR;


        scale =
                clamp(
                        scale * factor,
                        MIN_SCALE,
                        MAX_SCALE
                );


        double mouseX =
                event.getX();

        double mouseY =
                event.getY();


        offsetX =
                mouseX
                        - (mouseX - offsetX)
                        * (scale / oldScale);


        offsetY =
                mouseY
                        - (mouseY - offsetY)
                        * (scale / oldScale);


        draw();

        event.consume();
    }


    private void zoomFromCenter(
            double factor) {

        double oldScale =
                scale;


        scale =
                clamp(
                        scale * factor,
                        MIN_SCALE,
                        MAX_SCALE
                );


        double centerX =
                canvas.getWidth() / 2.0;

        double centerY =
                canvas.getHeight() / 2.0;


        offsetX =
                centerX
                        - (centerX - offsetX)
                        * (scale / oldScale);


        offsetY =
                centerY
                        - (centerY - offsetY)
                        * (scale / oldScale);


        draw();
    }


    private void fitToScreen() {

        if (contentWidth <= 0
                || contentHeight <= 0) {

            return;
        }


        double availableWidth =
                Math.max(
                        300,
                        canvas.getWidth() - 50
                );


        double availableHeight =
                Math.max(
                        250,
                        canvas.getHeight() - 50
                );


        scale =
                clamp(
                        Math.min(
                                availableWidth
                                        / contentWidth,

                                availableHeight
                                        / contentHeight
                        ),
                        MIN_SCALE,
                        1.0
                );


        offsetX =
                (canvas.getWidth()
                        - contentWidth * scale)
                        / 2.0;


        offsetY =
                (canvas.getHeight()
                        - contentHeight * scale)
                        / 2.0;


        draw();
    }


    /*
     * ------------------------------------------------------------
     * DIBUJO PRINCIPAL
     * ------------------------------------------------------------
     */

    private void draw() {

        GraphicsContext gc =
                canvas.getGraphicsContext2D();


        double width =
                canvas.getWidth();

        double height =
                canvas.getHeight();


        gc.setFill(
                BACKGROUND
        );

        gc.fillRect(
                0,
                0,
                width,
                height
        );


        drawBackgroundGrid(
                gc,
                width,
                height
        );


        if (turns.isEmpty()) {

            gc.setFill(TEXT);

            gc.setFont(
                    Font.font(
                            "System",
                            20
                    )
            );

            gc.fillText(
                    "No hay turnos registrados.",
                    40,
                    60
            );

            updateZoomLabel();

            return;
        }


        List<TurnLayout> layouts =
                buildLayouts();


        contentWidth =
                layouts.stream()
                        .mapToDouble(
                                layout ->
                                        layout.width
                        )
                        .max()
                        .orElse(1200);


        contentHeight =
                layouts.stream()
                        .mapToDouble(
                                layout ->
                                        layout.y
                                                + layout.height
                        )
                        .max()
                        .orElse(800);


        gc.save();


        gc.translate(
                offsetX,
                offsetY
        );


        gc.scale(
                scale,
                scale
        );


        for (TurnLayout layout
                : layouts) {

            drawTurn(
                    gc,
                    layout
            );
        }


        gc.restore();


        updateZoomLabel();
    }


    /*
     * ------------------------------------------------------------
     * FONDO
     * ------------------------------------------------------------
     */

    private void drawBackgroundGrid(
            GraphicsContext gc,
            double width,
            double height) {

        gc.save();

        gc.setStroke(
                Color.web(
                        "#102235",
                        0.45
                )
        );

        gc.setLineWidth(1);


        double grid = 42;


        for (
                double x = 0;
                x < width;
                x += grid
        ) {

            gc.strokeLine(
                    x,
                    0,
                    x,
                    height
            );
        }


        for (
                double y = 0;
                y < height;
                y += grid
        ) {

            gc.strokeLine(
                    0,
                    y,
                    width,
                    y
            );
        }


        gc.restore();
    }


    /*
     * ------------------------------------------------------------
     * CONSTRUCCIÓN DE LOS ÁRBOLES
     * ------------------------------------------------------------
     */

    private List<TurnLayout> buildLayouts() {

        List<TurnLayout> result =
                new ArrayList<>();


        double y =
                PADDING;


        for (
                int i = 0;
                i < turns.size();
                i++
        ) {

            TurnRecord turn =
                    turns.get(i);


            Tree<BoardState> tree =
                    treeForTurn(turn);


            TurnLayout layout =
                    layoutTree(
                            tree,
                            turn,
                            i + 1,
                            y
                    );


            result.add(layout);


            y +=
                    layout.height
                            + TURN_GAP;
        }


        return result;
    }


    /**
     * Obtiene el árbol que corresponde al turno.
     *
     * Si Mishi jugó:
     *     utilizamos el árbol real de Minimax.
     *
     * Si jugó una persona:
     *     generamos el árbol equivalente.
     */
    private Tree<BoardState> treeForTurn(
            TurnRecord turn) {

        if (turn.wasCalculatedByMinimax()) {

            return turn
                    .getMinimaxDecision()
                    .getDecisionTree();
        }


        return buildHumanDecisionTree(
                turn.getBoardBefore(),
                turn.getPlayer(),
                turn.getMove()
        );
    }


    /**
     * Construye un árbol de dos niveles para un turno humano.
     *
     * Nivel 0:
     *     tablero actual
     *
     * Nivel 1:
     *     todos los movimientos posibles del jugador
     *
     * Nivel 2:
     *     todas las respuestas posibles del rival
     *
     * La jugada realmente realizada queda marcada como seleccionada
     * durante el dibujo.
     */
    private Tree<BoardState> buildHumanDecisionTree(
            Board board,
            Mark actor,
            Move actualMove) {

        Mark opponent =
                actor.opposite();


        int initialUtility =
                UtilityCalculator.calculate(
                        board,
                        actor
                );


        BoardState initialState =
                new BoardState(
                        board,
                        Mark.EMPTY,
                        null,
                        initialUtility
                );


        Tree<BoardState> tree =
                new Tree<>(
                        initialState
                );


        List<Move> possibleMoves =
                orderMoves(
                        board.availableMoves()
                );


        for (Move move
                : possibleMoves) {

            Board afterActor =
                    board.place(
                            move,
                            actor
                    );


            int utility =
                    utilityFor(
                            afterActor,
                            actor
                    );


            Tree<BoardState> branch =
                    tree.addChild(
                            new BoardState(
                                    afterActor,
                                    actor,
                                    move,
                                    utility
                            )
                    );


            /*
             * Si el movimiento ya terminó la partida,
             * no hay respuestas.
             */
            if (afterActor
                    .result()
                    .isFinished()) {

                continue;
            }


            List<Move> responses =
                    orderMoves(
                            afterActor.availableMoves()
                    );


            for (Move response
                    : responses) {

                Board afterOpponent =
                        afterActor.place(
                                response,
                                opponent
                        );


                int responseUtility =
                        utilityFor(
                                afterOpponent,
                                actor
                        );


                branch.addChild(
                        new BoardState(
                                afterOpponent,
                                opponent,
                                response,
                                responseUtility
                        )
                );
            }
        }


        return tree;
    }


    /*
     * ------------------------------------------------------------
     * UTILIDAD
     * ------------------------------------------------------------
     */

    private int utilityFor(
            Board board,
            Mark perspective) {

        if (board.winner()
                == perspective) {

            return 100;
        }


        if (board.winner()
                == perspective.opposite()) {

            return -100;
        }


        return UtilityCalculator.calculate(
                board,
                perspective
        );
    }


    /*
     * ------------------------------------------------------------
     * ORDEN DE MOVIMIENTOS
     * ------------------------------------------------------------
     *
     * Se mantiene la misma prioridad que Minimax:
     *
     * centro
     * esquinas
     * laterales
     */

    private List<Move> orderMoves(
            List<Move> moves) {

        List<Move> ordered =
                new ArrayList<>(
                        moves
                );


        ordered.sort(
                Comparator.comparingInt(
                        this::priority
                )
        );


        return ordered;
    }


    private int priority(
            Move move) {

        if (move.getRow() == 1
                && move.getColumn() == 1) {

            return 0;
        }


        if (
                (move.getRow()
                        + move.getColumn())
                        % 2 == 0
        ) {

            return 1;
        }


        return 2;
    }


    /*
     * ------------------------------------------------------------
     * POSICIONAMIENTO
     * ------------------------------------------------------------
     */

    private TurnLayout layoutTree(
            Tree<BoardState> tree,
            TurnRecord turn,
            int turnNumber,
            double y) {

        TreeNodeLayout root =
                new TreeNodeLayout(
                        tree.getRoot()
                                .getContent(),
                        0,
                        y + 35,
                        false
                );


        List<TreeNodeLayout> candidates =
                new ArrayList<>();


        List<List<TreeNodeLayout>> responses =
                new ArrayList<>();


        List<Tree<BoardState>> branches =
                tree.getRoot()
                        .getChildren();


        double totalWidth = 0;


        List<Double> branchWidths =
                new ArrayList<>();


        /*
         * Calculamos cuánto espacio necesita
         * cada familia.
         */
        for (
                Tree<BoardState> branch
                : branches
        ) {

            int responseCount =
                    branch.getRoot()
                            .getChildren()
                            .size();


            double branchWidth =
                    Math.max(
                            NODE_W,

                            responseCount == 0

                                    ? NODE_W

                                    : responseCount
                                            * NODE_W
                                            + Math.max(
                                                    0,
                                                    responseCount - 1
                                                            * H_GAP
                                            )
                    );


            branchWidths.add(
                    branchWidth
            );


            totalWidth +=
                    branchWidth;
        }


        if (!branches.isEmpty()) {

            totalWidth +=
                    (branches.size() - 1)
                            * H_GAP;
        }


        totalWidth =
                Math.max(
                        totalWidth,
                        NODE_W
                );


        double startX =
                PADDING;


        root.x =
                startX
                        + totalWidth / 2.0
                        - NODE_W / 2.0;


        double candidateY =
                y
                        + 35
                        + NODE_H
                        + V_GAP;


        double responseY =
                candidateY
                        + NODE_H
                        + V_GAP;


        double cursorX =
                startX;


        for (
                int i = 0;
                i < branches.size();
                i++
        ) {

            Tree<BoardState> branch =
                    branches.get(i);


            BoardState candidate =
                    branch.getRoot()
                            .getContent();


            boolean selected =
                    sameMove(
                            candidate.getMove(),
                            turn.getMove()
                    );


            double branchWidth =
                    branchWidths.get(i);


            double candidateX =
                    cursorX
                            + branchWidth / 2.0
                            - NODE_W / 2.0;


            TreeNodeLayout candidateNode =
                    new TreeNodeLayout(
                            candidate,
                            candidateX,
                            candidateY,
                            selected
                    );


            candidates.add(
                    candidateNode
            );


            List<TreeNodeLayout> responseNodes =
                    new ArrayList<>();


            List<Tree<BoardState>> children =
                    branch.getRoot()
                            .getChildren();


            double responseTotal =
                    children.size()
                            * NODE_W
                            + Math.max(
                                    0,
                                    children.size() - 1
                                            * H_GAP
                            );


            double responseCursor =
                    cursorX
                            + (branchWidth
                            - responseTotal)
                            / 2.0;


            for (
                    Tree<BoardState> child
                    : children
            ) {

                BoardState response =
                        child.getRoot()
                                .getContent();


                responseNodes.add(
                        new TreeNodeLayout(
                                response,
                                responseCursor,
                                responseY,
                                false
                        )
                );


                responseCursor +=
                        NODE_W
                                + H_GAP;
            }


            responses.add(
                    responseNodes
            );


            cursorX +=
                    branchWidth
                            + H_GAP;
        }


        double blockWidth =
                totalWidth
                        + PADDING * 2;


        double blockHeight =
                35
                        + NODE_H
                        + V_GAP
                        + NODE_H
                        + V_GAP
                        + NODE_H
                        + PADDING;


        return new TurnLayout(
                turnNumber,
                turn,
                root,
                candidates,
                responses,
                blockWidth,
                blockHeight,
                y
        );
    }


    /*
     * ------------------------------------------------------------
     * DIBUJAR TURNO
     * ------------------------------------------------------------
     */

    private void drawTurn(
            GraphicsContext gc,
            TurnLayout layout) {

        /*
         * Dibujamos las conexiones primero,
         * luego los tableros encima.
         */
        drawEdges(
                gc,
                layout.root,
                layout.candidates,
                layout.responses
        );


        drawNode(
                gc,
                layout.root,
                false
        );


        for (
                TreeNodeLayout candidate
                : layout.candidates
        ) {

            drawNode(
                    gc,
                    candidate,
                    candidate.selected
            );
        }


        for (
                List<TreeNodeLayout> responseList
                : layout.responses
        ) {

            for (
                    TreeNodeLayout response
                    : responseList
            ) {

                drawNode(
                        gc,
                        response,
                        false
                );
            }
        }
    }


    /*
     * ------------------------------------------------------------
     * CONEXIONES
     * ------------------------------------------------------------
     */

    private void drawEdges(
            GraphicsContext gc,
            TreeNodeLayout root,
            List<TreeNodeLayout> candidates,
            List<List<TreeNodeLayout>> responses) {

        double rootX =
                root.x
                        + NODE_W / 2.0;


        double rootY =
                root.y
                        + NODE_H;


        for (
                int i = 0;
                i < candidates.size();
                i++
        ) {

            TreeNodeLayout candidate =
                    candidates.get(i);


            drawEdge(
                    gc,
                    rootX,
                    rootY,
                    candidate.x
                            + NODE_W / 2.0,
                    candidate.y,
                    candidate.state
                            .getPlayerWhoMoved()
            );


            for (
                    TreeNodeLayout response
                    : responses.get(i)
            ) {

                drawEdge(
                        gc,

                        candidate.x
                                + NODE_W / 2.0,

                        candidate.y
                                + NODE_H,

                        response.x
                                + NODE_W / 2.0,

                        response.y,

                        response.state
                                .getPlayerWhoMoved()
                );
            }
        }
    }


    private void drawEdge(
            GraphicsContext gc,
            double x1,
            double y1,
            double x2,
            double y2,
            Mark player) {

        Color color =
                colorForPlayer(
                        player
                );


        gc.save();


        gc.setStroke(
                color.deriveColor(
                        0,
                        1,
                        1,
                        0.65
                )
        );


        gc.setLineWidth(2.0);


        gc.setLineCap(
                StrokeLineCap.ROUND
        );


        gc.strokeLine(
                x1,
                y1,
                x2,
                y2
        );


        gc.restore();
    }


    /*
     * ------------------------------------------------------------
     * DIBUJAR NODO
     * ------------------------------------------------------------
     */

    private void drawNode(
            GraphicsContext gc,
            TreeNodeLayout node,
            boolean selected) {

        Board board =
                node.state.getBoard();


        Color border =
                colorForPlayer(
                        node.state
                                .getPlayerWhoMoved()
                );


        if (
                node.state
                        .getPlayerWhoMoved()
                        == Mark.EMPTY
        ) {

            border =
                    BOARD_LINE;
        }


        gc.save();


        /*
         * La jugada realmente realizada
         * queda resaltada.
         */
        if (selected) {

            gc.setEffect(
                    new javafx.scene.effect.DropShadow(
                            16,
                            SELECTED
                    )
            );

            gc.setStroke(
                    SELECTED
            );

            gc.setLineWidth(
                    5
            );

        } else {

            gc.setStroke(
                    border
            );

            gc.setLineWidth(
                    2.5
            );
        }


        gc.setFill(
                Color.web(
                        "#0A1828",
                        0.97
                )
        );


        gc.fillRoundRect(
                node.x,
                node.y,
                NODE_W,
                NODE_H,
                10,
                10
        );


        gc.strokeRoundRect(
                node.x,
                node.y,
                NODE_W,
                NODE_H,
                10,
                10
        );


        drawBoard(
                gc,
                board,
                node.x + 10,
                node.y + 10,
                NODE_W - 20
        );


        gc.setEffect(null);

        gc.restore();
    }


    /*
     * ------------------------------------------------------------
     * DIBUJAR TABLERO 3x3
     * ------------------------------------------------------------
     */

    private void drawBoard(
            GraphicsContext gc,
            Board board,
            double x,
            double y,
            double size) {

        double cell =
                size / Board.SIZE;


        gc.save();


        gc.setStroke(
                BOARD_LINE
        );


        gc.setLineWidth(
                1.1
        );
        
        for (
                int i = 1;
                i < Board.SIZE;
                i++
        ) {

            gc.strokeLine(
                    x + i * cell,
                    y,
                    x + i * cell,
                    y + size
            );


            gc.strokeLine(
                    x,
                    y + i * cell,
                    x + size,
                    y + i * cell
            );
        }


        /*
         * Fichas.
         */
        for (
                int row = 0;
                row < Board.SIZE;
                row++
        ) {

            for (
                    int column = 0;
                    column < Board.SIZE;
                    column++
            ) {

                Mark mark =
                        board.get(
                                row,
                                column
                        );


                if (mark == Mark.EMPTY) {
                    continue;
                }


                double centerX =
                        x
                                + column * cell
                                + cell / 2.0;


                double centerY =
                        y
                                + row * cell
                                + cell / 2.0;


                double half =
                        cell * 0.27;


                gc.setStroke(
                        mark == Mark.X
                                ? PLAYER_1
                                : PLAYER_2
                );


                gc.setLineWidth(
                        3.2
                );


                gc.setLineCap(StrokeLineCap.ROUND);


                if (mark == Mark.X) {

                    gc.strokeLine(
                            centerX - half,
                            centerY - half,
                            centerX + half,
                            centerY + half
                    );


                    gc.strokeLine(
                            centerX + half,
                            centerY - half,
                            centerX - half,
                            centerY + half
                    );

                } else {

                    gc.strokeOval(
                            centerX - half,
                            centerY - half,
                            half * 2,
                            half * 2
                    );
                }
            }
        }


        gc.restore();
    }

    private boolean sameMove(Move first, Move second) {

        return first != null && second != null && first.equals(second);
    }


    private Color colorForPlayer(Mark mark) {

        if (mark == playerOneMark) {

            return PLAYER_1;
        }


        return PLAYER_2;
    }


    private double clamp(
            double value,
            double min,
            double max) {

        return Math.max(min, Math.min(max, value));
    }


    private void updateZoomLabel() {

        zoomLabel.setText(
                Math.round(
                        scale * 100
                ) + "%"
        );
    }
    
    private static final class TreeNodeLayout {

        private final BoardState state;

        private double x;

        private final double y;

        private final boolean selected;


        private TreeNodeLayout(
                BoardState state,
                double x,
                double y,
                boolean selected) {

            this.state = state;

            this.x = x;

            this.y = y;

            this.selected = selected;
        }
    }


    private static final class TurnLayout {

        private final int turnNumber;

        private final TurnRecord turn;

        private final TreeNodeLayout root;

        private final List<TreeNodeLayout> candidates;

        private final List<List<TreeNodeLayout>> responses;

        private final double width;

        private final double height;

        private final double y;


        private TurnLayout(int turnNumber, TurnRecord turn, TreeNodeLayout root, List<TreeNodeLayout> candidates, List<List<TreeNodeLayout>> responses, double width, double height, double y) {
            this.turnNumber = turnNumber;
            this.turn =turn;
            this.root = root;
            this.candidates = candidates;
            this.responses = responses;
            this.width = width;
            this.height = height;
            this.y = y;
        }
    }
}
