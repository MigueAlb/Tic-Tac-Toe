package espol.com.tresenraya.ui;

import espol.com.tresenraya.ai.BoardState;
import espol.com.tresenraya.ai.Decision;
import espol.com.tresenraya.model.Board;
import espol.com.tresenraya.model.GameMode;
import espol.com.tresenraya.model.Mark;
import espol.com.tresenraya.structures.Tree;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

/**
 * Visualiza al final de la partida los árboles de decisión que Minimax
 * generó en cada turno de la computadora.
 *
 * Cada decisión conserva la estructura n-aria construida por Minimax:
 * raíz -> movimientos de la máquina -> respuestas hipotéticas del oponente.
 */
public final class DecisionTreeView {
    private final String playerName;
    private final GameMode gameMode;
    private final List<Decision> decisions;

    public DecisionTreeView(String playerName, GameMode gameMode, List<Decision> decisions) {
        this.playerName = playerName;
        this.gameMode = gameMode;
        this.decisions = decisions;
    }

    public void show() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Árbol de decisiones · Mishi Cósmico");
        stage.setMinWidth(900);
        stage.setMinHeight(650);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(18));

        Label title = new Label("ÁRBOL DE DECISIONES");
        title.getStyleClass().add("title");
        Label subtitle = new Label(
                "Minimax · " + gameMode.displayName()
                        + " · " + decisions.size() + " decisión(es) de la computadora");
        subtitle.getStyleClass().add("subtitle");
        Label explanation = new Label(
                "Cada rama muestra los movimientos posibles considerados por Minimax. "
                + "Las hojas muestran la utilidad del tablero; [MEJOR] identifica la jugada escogida.");
        explanation.setWrapText(true);
        explanation.getStyleClass().add("panel-copy");

        VBox header = new VBox(4, title, subtitle, explanation);
        header.setPadding(new Insets(0, 0, 14, 0));
        root.setTop(header);

        TreeItem<String> gameRoot = new TreeItem<>(
                "PARTIDA · " + playerName + " · " + gameMode.displayName());
        gameRoot.setExpanded(true);

        for (int i = 0; i < decisions.size(); i++) {
            Decision decision = decisions.get(i);
            Mark machine = machineForDecision(decision);
            TreeItem<String> decisionItem = new TreeItem<>(
                    "DECISIÓN " + (i + 1) + " · Mishi " + machine.symbol()
                    + " · eligió " + decision.getMove().display()
                    + " · minimax = " + decision.getMinimaxValue());
            decisionItem.setExpanded(true);
            addDecisionTree(decisionItem, decision);
            gameRoot.getChildren().add(decisionItem);
        }

        TreeView<String> tree = new TreeView<>(gameRoot);
        tree.setShowRoot(true);
        tree.setPrefHeight(540);
        tree.getStyleClass().add("decision-tree");

        ScrollPane scroll = new ScrollPane(tree);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        root.setCenter(scroll);

        HBox footer = new HBox(new Label(
                "Estructura: árbol n-ario · utilidad = líneas disponibles de jugador − oponente"));
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(12, 0, 0, 0));
        footer.getStyleClass().add("panel-copy");
        root.setBottom(footer);

        Scene scene = new Scene(root, 1100, 720);
        String css = getClass().getResource("/styles/game.css") == null
                ? null : getClass().getResource("/styles/game.css").toExternalForm();
        if (css != null) {
            scene.getStylesheets().add(css);
        }
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void addDecisionTree(TreeItem<String> decisionItem, Decision decision) {
        Tree<BoardState> tree = decision.getDecisionTree();
        if (tree == null || tree.isEmpty()) {
            return;
        }

        BoardState rootState = tree.getRoot().getContent();
        TreeItem<String> rootItem = new TreeItem<>(
                "ESTADO INICIAL · utilidad = " + rootState.getUtility()
                + "\n" + boardText(rootState.getBoard()));
        rootItem.setExpanded(true);
        decisionItem.getChildren().add(rootItem);

        for (Tree<BoardState> branch : tree.getRoot().getChildren()) {
            BoardState candidate = branch.getRoot().getContent();
            boolean selected = candidate.getMove() != null
                    && candidate.getMove().equals(decision.getMove());

            String label = "Mishi " + candidate.getPlayerWhoMoved().symbol()
                    + " → " + candidate.getMove().display()
                    + " · utilidad = " + candidate.getUtility();
            if (selected) {
                label += " · [MEJOR]";
            }

            TreeItem<String> candidateItem = new TreeItem<>(
                    label + "\n" + boardText(candidate.getBoard()));
            candidateItem.setExpanded(selected);
            rootItem.getChildren().add(candidateItem);

            int minimum = Integer.MAX_VALUE;
            for (Tree<BoardState> leaf : branch.getRoot().getChildren()) {
                BoardState state = leaf.getRoot().getContent();
                minimum = Math.min(minimum, state.getUtility());
            }
            if (!branch.getRoot().getChildren().isEmpty()) {
                candidateItem.setValue(candidateItem.getValue()
                        + " · mínimo = " + minimum);
            }

            for (Tree<BoardState> leaf : branch.getRoot().getChildren()) {
                BoardState response = leaf.getRoot().getContent();
                TreeItem<String> responseItem = new TreeItem<>(
                        "Respuesta " + response.getPlayerWhoMoved().symbol()
                        + " → " + response.getMove().display()
                        + " · utilidad = " + response.getUtility()
                        + "\n" + boardText(response.getBoard()));
                candidateItem.getChildren().add(responseItem);
            }
        }
    }

    private Mark machineForDecision(Decision decision) {
        if (decision.getDecisionTree() == null || decision.getDecisionTree().isEmpty()) {
            return Mark.EMPTY;
        }
        for (Tree<BoardState> child : decision.getDecisionTree().getRoot().getChildren()) {
            BoardState state = child.getRoot().getContent();
            if (state.getMove() != null && state.getMove().equals(decision.getMove())) {
                return state.getPlayerWhoMoved();
            }
        }
        return Mark.EMPTY;
    }

    private String boardText(Board board) {
        StringBuilder text = new StringBuilder();
        for (int row = 0; row < Board.SIZE; row++) {
            text.append("[");
            for (int column = 0; column < Board.SIZE; column++) {
                Mark mark = board.get(row, column);
                text.append(mark == Mark.EMPTY ? "·" : mark.symbol());
                if (column < Board.SIZE - 1) {
                    text.append(" ");
                }
            }
            text.append("]");
            if (row < Board.SIZE - 1) {
                text.append("\n");
            }
        }
        return text.toString();
    }
}
