package espol.com.tresenraya.ai;

import espol.com.tresenraya.model.Board;
import espol.com.tresenraya.model.Mark;

public final class UtilityCalculator {
    private UtilityCalculator() {
    }

    public static int calculate(Board board, Mark player) {
        return board.availableLinesFor(player) - board.availableLinesFor(player.opposite());
    }
}

