package espol.com.tresenraya.model;

import java.io.Serializable;

/** Un movimiento real realizado durante una partida. */
public final class PlayedMove implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int row;
    private final int column;
    private final Mark mark;

    public PlayedMove(int row, int column, Mark mark) {
        if (row < 0 || row >= Board.SIZE || column < 0 || column >= Board.SIZE) {
            throw new IllegalArgumentException("Posición fuera del tablero");
        }
        if (mark == null || mark == Mark.EMPTY) {
            throw new IllegalArgumentException("La ficha debe ser X u O");
        }
        this.row = row;
        this.column = column;
        this.mark = mark;
    }

    public PlayedMove(Move move, Mark mark) {
        this(move.getRow(), move.getColumn(), mark);
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public Mark getMark() {
        return mark;
    }

    public Move toMove() {
        return new Move(row, column);
    }
}

