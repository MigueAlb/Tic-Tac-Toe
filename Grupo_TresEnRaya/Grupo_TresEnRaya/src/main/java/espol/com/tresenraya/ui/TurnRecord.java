package espol.com.tresenraya.ui;

import espol.com.tresenraya.ai.Decision;
import espol.com.tresenraya.model.Board;
import espol.com.tresenraya.model.Mark;
import espol.com.tresenraya.model.Move;

/**
 * Representa un turno real de la partida.
 *
 * Se utiliza para poder reconstruir el árbol de decisiones completo
 * independientemente de quién haya realizado el movimiento:
 *
 * - Jugador 1
 * - Jugador 2
 * - Mishi X
 * - Mishi O
 *
 * Si minimaxDecision no es null, significa que ese movimiento fue
 * calculado por Minimax y podemos utilizar directamente el árbol
 * generado por la IA.
 */
public final class TurnRecord {

    private final Board boardBefore;
    private final Mark player;
    private final Move move;
    private final Decision minimaxDecision;

    public TurnRecord(
            Board boardBefore,
            Mark player,
            Move move,
            Decision minimaxDecision) {

        this.boardBefore = boardBefore;
        this.player = player;
        this.move = move;
        this.minimaxDecision = minimaxDecision;
    }

    public Board getBoardBefore() {
        return boardBefore;
    }

    public Mark getPlayer() {
        return player;
    }

    public Move getMove() {
        return move;
    }

    public Decision getMinimaxDecision() {
        return minimaxDecision;
    }

    public boolean wasCalculatedByMinimax() {
        return minimaxDecision != null;
    }
}
