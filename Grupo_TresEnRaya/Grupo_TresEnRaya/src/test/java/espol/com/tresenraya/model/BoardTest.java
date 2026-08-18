package espol.com.tresenraya.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BoardTest {
    @Test
    void detectsRowWinner() {
        Board board = new Board()
                .place(new Move(0, 0), Mark.X)
                .place(new Move(0, 1), Mark.X)
                .place(new Move(0, 2), Mark.X);
        assertEquals(Mark.X, board.winner());
        assertEquals(GameResult.X_WINS, board.result());
    }

    @Test
    void doesNotOverwriteCell() {
        Board board = new Board().place(new Move(1, 1), Mark.O);
        assertThrows(IllegalArgumentException.class, () -> board.place(new Move(1, 1), Mark.X));
    }

    @Test
    void emptyBoardHasEightAvailableLines() {
        Board board = new Board();
        assertEquals(8, board.availableLinesFor(Mark.X));
        assertEquals(8, board.availableLinesFor(Mark.O));
    }
}

