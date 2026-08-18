package espol.com.tresenraya.ai;

import espol.com.tresenraya.model.Board;
import espol.com.tresenraya.model.Mark;
import espol.com.tresenraya.model.Move;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinimaxAITest {
    private final MinimaxAI ai = new MinimaxAI();

    @Test
    void choosesCenterOnEmptyBoard() {
        Decision decision = ai.chooseMove(new Board(), Mark.X);
        assertEquals(new Move(1, 1), decision.getMove());
        assertEquals(82, decision.getDecisionTree().size());
    }

    @Test
    void takesImmediateWin() {
        Board board = new Board()
                .place(new Move(0, 0), Mark.O)
                .place(new Move(1, 0), Mark.X)
                .place(new Move(0, 1), Mark.O)
                .place(new Move(1, 1), Mark.X);
        assertEquals(new Move(0, 2), ai.chooseMove(board, Mark.O).getMove());
    }

    @Test
    void generatesNaryDecisionTree() {
        Decision decision = ai.chooseMove(new Board(), Mark.X);
        assertTrue(decision.getDecisionTree().size() > 1);
        assertEquals(9, decision.getCandidates().size());
    }
}

