package espol.com.tresenraya.ui;

import espol.com.tresenraya.ai.BoardState;
import espol.com.tresenraya.ai.Decision;
import espol.com.tresenraya.model.Board;
import espol.com.tresenraya.model.Mark;
import espol.com.tresenraya.structures.Tree;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Muestra los tableros considerados por Minimax en una decisión concreta.
 *
 * Primer nivel: movimientos posibles de la computadora y su valor minimax.
 * Segundo nivel: respuestas posibles del oponente y su utilidad.
 */
public final class DecisionAnalysisView {
    private static final String BG = "#06101D";
    private static final String PANEL = "#0B1B2D";
    private static final String GRID = "#DDE7F2";
    private static final String X_COLOR = "#66D9FF";
    private static final String O_COLOR = "#FF5964";

    private DecisionAnalysisView() { }

    public static void show(String title, Decision decision, Mark computerMark) {
        if (decision == null || decision.getDecisionTree() == null
                || decision.getDecisionTree().isEmpty()) {
            return;
        }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Análisis Minimax");
        stage.setMaximized(true);

        BoardState root = decision.getDecisionTree().getRoot().getContent();

        Label heading = new Label("TABLEROS INTERMEDIOS · MINIMAX");
        heading.setStyle("-fx-text-fill: #EAF4FF; -fx-font-size: 22px; -fx-font-weight: bold;");

        Label info = new Label(title + " · " + computerMark.symbol()
                + " · valor escogido = " + decision.getMinimaxValue());
        info.setStyle("-fx-text-fill: #8EA8BF; -fx-font-size: 13px;");

        Label help = new Label(
                "Celeste: Jugador X · Rojo: Jugador O · Verde: movimiento elegido · "
                        + "Rojo oscuro: mínimo de la familia");
        help.setStyle("-fx-text-fill: #8EA8BF; -fx-font-size: 12px;");

        VBox header = new VBox(4, heading, info, help);
        header.setPadding(new Insets(20, 24, 12, 24));

        FlowPane families = new FlowPane();
        families.setHgap(16);
        families.setVgap(18);
        families.setPadding(new Insets(12, 24, 30, 24));
        families.setAlignment(Pos.TOP_LEFT);

        for (Tree<BoardState> branch : decision.getDecisionTree().getRoot().getChildren()) {
            BoardState candidate = branch.getRoot().getContent();
            boolean selected = candidate.getMove() != null
                    && candidate.getMove().equals(decision.getMove());

            VBox family = new VBox(8);
            family.setPadding(new Insets(12));
            family.setMinWidth(210);
            family.setStyle("-fx-background-color: " + PANEL + "; -fx-background-radius: 10;"
                    + " -fx-border-color: " + (selected ? "#7CFFB2" : "#29435A") + ";"
                    + " -fx-border-radius: 10; -fx-border-width: " + (selected ? "3" : "1") + ";");

            String candidateText = "Movimiento: " + candidate.getMove().display()
                    + "\nValor minimax: " + candidateValue(decision, candidate);
            if (selected) {
                candidateText += "\n✓ ELEGIDO";
            }
            Label candidateLabel = new Label(candidateText);
            candidateLabel.setStyle("-fx-text-fill: #EAF4FF; -fx-font-weight: bold; -fx-font-size: 12px;");
            candidateLabel.setWrapText(true);

            family.getChildren().addAll(
                    candidateLabel,
                    createBoardCard(candidate.getBoard(), "Estado después de la jugada", selected, null));

            for (Tree<BoardState> leaf : branch.getRoot().getChildren()) {
                BoardState response = leaf.getRoot().getContent();
                int minimum = minimumUtility(branch);
                boolean isMinimum = response.getUtility() == minimum;
                family.getChildren().add(
                        createBoardCard(
                                response.getBoard(),
                                "Respuesta " + response.getPlayerWhoMoved().symbol()
                                        + " · utilidad = " + response.getUtility()
                                        + (isMinimum ? " · MÍNIMO" : ""),
                                false,
                                isMinimum));
            }

            families.getChildren().add(family);
        }

        ScrollPane scroll = new ScrollPane(families);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + BG + "; -fx-background-color: " + BG + ";");

        BorderPane rootPane = new BorderPane();
        rootPane.setStyle("-fx-background-color: " + BG + ";");
        rootPane.setTop(header);
        rootPane.setCenter(scroll);

        stage.setScene(new Scene(rootPane, 1400, 850, javafx.scene.paint.Color.web(BG)));
        stage.showAndWait();
    }

    private static int candidateValue(Decision decision, BoardState candidate) {
        for (Decision.CandidateScore score : decision.getCandidates()) {
            if (score.getMove().equals(candidate.getMove())) {
                return score.getWorstUtility();
            }
        }
        return candidate.getUtility();
    }

    private static int minimumUtility(Tree<BoardState> branch) {
        if (branch.getRoot().getChildren().isEmpty()) {
            return branch.getRoot().getContent().getUtility();
        }
        int minimum = Integer.MAX_VALUE;
        for (Tree<BoardState> leaf : branch.getRoot().getChildren()) {
            minimum = Math.min(minimum, leaf.getRoot().getContent().getUtility());
        }
        return minimum;
    }

    private static VBox createBoardCard(Board board, String caption,
                                        boolean selected, Boolean minimum) {
        Label label = new Label(caption);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: #B8C9D8; -fx-font-size: 11px;");

        GridPane grid = new GridPane();
        grid.setPrefSize(126, 126);
        grid.setMinSize(126, 126);
        grid.setMaxSize(126, 126);
        grid.setHgap(0);
        grid.setVgap(0);
        grid.setStyle("-fx-border-color: " + borderColor(board, selected, minimum)
                + "; -fx-border-width: 2; -fx-background-color: #071522;");

        for (int row = 0; row < Board.SIZE; row++) {
            for (int col = 0; col < Board.SIZE; col++) {
                Label cell = new Label(board.get(row, col).symbol());
                cell.setPrefSize(42, 42);
                cell.setAlignment(Pos.CENTER);
                cell.setStyle("-fx-text-fill: " + markColor(board.get(row, col))
                        + "; -fx-font-size: 22px; -fx-font-weight: bold;"
                        + " -fx-border-color: " + GRID + "; -fx-border-width: 0.5;");
                grid.add(cell, col, row);
            }
        }

        VBox box = new VBox(5, grid, label);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private static String borderColor(Board board, boolean selected, Boolean minimum) {
        if (selected) return "#7CFFB2";
        if (Boolean.TRUE.equals(minimum)) return "#FF5964";
        return "#29435A";
    }

    private static String markColor(Mark mark) {
        if (mark == Mark.X) return X_COLOR;
        if (mark == Mark.O) return O_COLOR;
        return "#29435A";
    }
}
