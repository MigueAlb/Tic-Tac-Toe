package espol.com.tresenraya.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Board implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    public static final int SIZE = 3;
    private final Mark[][] cells;

    public Board() {
        cells = new Mark[SIZE][SIZE];

        for (int row = 0; row < SIZE; row++) {
            Arrays.fill(cells[row], Mark.EMPTY);
        }
    }

    public Board(Board other) {
        cells = new Mark[SIZE][SIZE];

        for (int row = 0; row < SIZE; row++) {
            cells[row] = Arrays.copyOf(other.cells[row], SIZE);
        }
    }

    public Mark get(int row, int column) {
        validatePosition(row, column);
        return cells[row][column];
    }

    public boolean isEmpty(int row, int column) {
        return get(row, column) == Mark.EMPTY;
    }

    public Board place(Move move, Mark mark) {
        if (move == null) {
            throw new IllegalArgumentException("El movimiento no puede ser nulo");
        }

        if (mark == null || mark == Mark.EMPTY) {
            throw new IllegalArgumentException("La ficha debe ser X u O");
        }

        if (!isEmpty(move.getRow(), move.getColumn())) {
            throw new IllegalArgumentException("La casilla ya está ocupada");
        }

        Board copy = new Board(this);

        copy.cells[move.getRow()][move.getColumn()] = mark;

        return copy;
    }

    public List<Move> availableMoves() {
        List<Move> moves = new ArrayList<>();

        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {

                if (cells[row][column] == Mark.EMPTY) {
                    moves.add(new Move(row, column));
                }
            }
        }

        return moves;
    }

    public boolean isRowComplete(int row, Mark mark) {
        validatePosition(row, 0);

        for (int column = 0; column < SIZE; column++) {

            if (cells[row][column] != mark) {
                return false;
            }
        }

        return true;
    }

    public boolean isColumnComplete(int column, Mark mark) {
        validatePosition(0, column);

        for (int row = 0; row < SIZE; row++) {

            if (cells[row][column] != mark) {
                return false;
            }
        }

        return true;
    }

    public boolean isDiagonalComplete(Mark mark) {
        boolean mainDiagonal = true;
        boolean secondaryDiagonal = true;

        for (int i = 0; i < SIZE; i++) {

            if (cells[i][i] != mark) {
                mainDiagonal = false;
            }

            if (cells[i][SIZE - 1 - i] != mark) {
                secondaryDiagonal = false;
            }
        }

        return mainDiagonal || secondaryDiagonal;
    }

    public boolean hasWinningRow(Mark mark) {

        for (int row = 0; row < SIZE; row++) {

            if (isRowComplete(row, mark)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasWinningColumn(Mark mark) {

        for (int column = 0; column < SIZE; column++) {

            if (isColumnComplete(column, mark)) {
                return true;
            }
        }

        return false;
    }

    public Mark winner() {

        if (hasWinningRow(Mark.X)
                || hasWinningColumn(Mark.X)
                || isDiagonalComplete(Mark.X)) {

            return Mark.X;
        }

        if (hasWinningRow(Mark.O)
                || hasWinningColumn(Mark.O)
                || isDiagonalComplete(Mark.O)) {

            return Mark.O;
        }

        return Mark.EMPTY;
    }

    public int availableLinesFor(Mark player) {

        if (player == null || player == Mark.EMPTY) {
            return 0;
        }

        int count = 0;

        for (int i = 0; i < SIZE; i++) {

            if (isRowAvailable(i, player)) {
                count++;
            }

            if (isColumnAvailable(i, player)) {
                count++;
            }
        }

        count += availableDiagonalsFor(player);

        return count;
    }

    private boolean isRowAvailable(int row, Mark player) {
        Mark opponent = player.opposite();

        for (int column = 0; column < SIZE; column++) {

            if (cells[row][column] == opponent) {
                return false;
            }
        }

        return true;
    }

    private boolean isColumnAvailable(int column, Mark player) {
        Mark opponent = player.opposite();

        for (int row = 0; row < SIZE; row++) {

            if (cells[row][column] == opponent) {
                return false;
            }
        }

        return true;
    }

    private int availableDiagonalsFor(Mark player) {
        Mark opponent = player.opposite();

        boolean mainDiagonal = true;
        boolean secondaryDiagonal = true;

        for (int i = 0; i < SIZE; i++) {

            if (cells[i][i] == opponent) {
                mainDiagonal = false;
            }

            if (cells[i][SIZE - 1 - i] == opponent) {
                secondaryDiagonal = false;
            }
        }

        int count = 0;

        if (mainDiagonal) {
            count++;
        }

        if (secondaryDiagonal) {
            count++;
        }

        return count;
    }

    public GameResult result() {
        Mark winner = winner();

        if (winner == Mark.X) {
            return GameResult.X_WINS;
        }

        if (winner == Mark.O) {
            return GameResult.O_WINS;
        }

        if (isFull()) {
            return GameResult.DRAW;
        }

        return GameResult.IN_PROGRESS;
    }

    public boolean isFull() {

        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {

                if (cells[row][column] == Mark.EMPTY) {
                    return false;
                }
            }
        }

        return true;
    }

    public static Board fromCompact(String compact) {
        if (compact == null || compact.length() != SIZE * SIZE) {
            throw new IllegalArgumentException("Representación de tablero inválida");
        }

        Board board = new Board();
        for (int index = 0; index < compact.length(); index++) {
            char value = compact.charAt(index);
            Mark mark;
            if (value == '-') {
                mark = Mark.EMPTY;
            } else if (value == 'X') {
                mark = Mark.X;
            } else if (value == 'O') {
                mark = Mark.O;
            } else {
                throw new IllegalArgumentException("Símbolo de tablero inválido: " + value);
            }
            if (mark != Mark.EMPTY) {
                int row = index / SIZE;
                int column = index % SIZE;
                board = board.place(new Move(row, column), mark);
            }
        }
        return board;
    }

    public String compact() {
        StringBuilder text =
                new StringBuilder(SIZE * SIZE);

        for (int row = 0; row < SIZE; row++) {
            for (int column = 0;
                 column < SIZE;
                 column++) {

                Mark mark = cells[row][column];

                if (mark == Mark.EMPTY) {
                    text.append('-');
                } else {
                    text.append(mark.symbol());
                }
            }
        }

        return text.toString();
    }

    private void validatePosition(int row, int column) {

        if (row < 0
                || row >= SIZE
                || column < 0
                || column >= SIZE) {

            throw new IndexOutOfBoundsException(
                    "Posición fuera del tablero");
        }
    }

    @Override
    public boolean equals(Object object) {

        if (this == object) {
            return true;
        }

        if (!(object instanceof Board)) {
            return false;
        }

        Board other = (Board) object;

        return Arrays.deepEquals(cells, other.cells);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(cells);
    }
}

