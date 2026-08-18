package espol.com.tresenraya.ai;

import espol.com.tresenraya.model.Board;
import espol.com.tresenraya.model.Mark;
import espol.com.tresenraya.model.Move;
import espol.com.tresenraya.structures.Tree;

import java.util.ArrayList;
import java.util.List;

public class MinimaxAI {
    private static final int WIN_SCORE = 100;

    public Decision chooseMove(Board board, Mark computer) {
        if (board.result().isFinished()) {
            throw new IllegalStateException("La partida ya terminó");
        }

        Mark human = computer.opposite();
        int initialUtility = UtilityCalculator.calculate(board, computer);
        BoardState initialState = new BoardState(board, Mark.EMPTY, null, initialUtility);
        Tree<BoardState> tree = new Tree<>(initialState);
        ArrayList<Decision.CandidateScore> scores = new ArrayList<>();
        List<Move> computerMoves = orderMoves(board.availableMoves());

        for (Move computerMove : computerMoves) {
            Board afterComputer = board.place(computerMove, computer);
            int computerUtility = UtilityCalculator.calculate(afterComputer, computer);
            BoardState computerState = new BoardState(afterComputer, computer, computerMove, computerUtility);
            Tree<BoardState> computerBranch = tree.addChild(computerState);

            int worstCase;
            if (afterComputer.winner() == computer) {
                worstCase = WIN_SCORE;
            } else {
                List<Move> humanMoves = orderMoves(afterComputer.availableMoves());
                if (humanMoves.isEmpty()) {
                    worstCase = UtilityCalculator.calculate(afterComputer, computer);
                } else {
                    worstCase = Integer.MAX_VALUE;
                }

                for (Move humanMove : humanMoves) {
                    Board afterHuman = afterComputer.place(humanMove, human);
                    int utility;
                    if (afterHuman.winner() == human) {
                        utility = -WIN_SCORE;
                    } else {
                        utility = UtilityCalculator.calculate(afterHuman, computer);
                    }

                    BoardState humanState = new BoardState(afterHuman, human, humanMove, utility);
                    computerBranch.addChild(humanState);
                    if (utility < worstCase) {
                        worstCase = utility;
                    }
                }
            }

            scores.add(new Decision.CandidateScore(computerMove, worstCase));
        }

        Decision.CandidateScore best = scores.get(0);
        for (Decision.CandidateScore candidate : scores) {
            if (candidate.getWorstUtility() > best.getWorstUtility()) {
                best = candidate;
            }
        }

        return new Decision(best.getMove(), best.getWorstUtility(), tree, scores);
    }

    private List<Move> orderMoves(List<Move> moves) {
        ArrayList<Move> ordered = new ArrayList<>(moves);

        for (int i = 0; i < ordered.size(); i++) {
            for (int j = i + 1; j < ordered.size(); j++) {
                if (priority(ordered.get(j)) < priority(ordered.get(i))) {
                    Move temporary = ordered.get(i);
                    ordered.set(i, ordered.get(j));
                    ordered.set(j, temporary);
                }
            }
        }

        return ordered;
    }

    private int priority(Move move) {
        if (move.getRow() == 1 && move.getColumn() == 1) {
            return 0;
        }
        if ((move.getRow() + move.getColumn()) % 2 == 0) {
            return 1;
        }
        return 2;
    }
}

