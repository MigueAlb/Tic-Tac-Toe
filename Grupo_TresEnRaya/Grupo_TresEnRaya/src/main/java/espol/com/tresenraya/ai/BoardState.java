package espol.com.tresenraya.ai;

import espol.com.tresenraya.model.Board;
import espol.com.tresenraya.model.Mark;
import espol.com.tresenraya.model.Move;

public class BoardState {
    private Board board;
    private Mark playerWhoMoved;
    private Move move;
    private int utility;

    public BoardState(Board board, Mark playerWhoMoved, Move move, int utility) {
        this.board = board;
        this.playerWhoMoved = playerWhoMoved;
        this.move = move;
        this.utility = utility;
    }

    public Board getBoard() {
        return board;
    }

    public Mark getPlayerWhoMoved() {
        return playerWhoMoved;
    }

    public Move getMove() {
        return move;
    }

    public int getUtility() {
        return utility;
    }
}

