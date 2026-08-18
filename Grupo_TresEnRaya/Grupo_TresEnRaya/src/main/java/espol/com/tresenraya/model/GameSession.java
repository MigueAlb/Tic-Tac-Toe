package espol.com.tresenraya.model;

import espol.com.tresenraya.ai.Decision;
import espol.com.tresenraya.ai.MinimaxAI;

public final class GameSession {
    private final MinimaxAI ai = new MinimaxAI();
    private Board board;
    private GameMode gameMode;
    private Mark playerOneMark;
    private Mark playerTwoMark;
    private Mark currentTurn;
    private Decision lastDecision;

    public void start(GameMode gameMode, Mark playerOneMark, boolean playerOneStarts) {
        if (gameMode == null) {
            throw new IllegalArgumentException("El modo de juego no puede ser nulo");
        }
        if (playerOneMark == null || playerOneMark == Mark.EMPTY) {
            throw new IllegalArgumentException("Ficha inválida");
        }

        this.board = new Board();
        this.gameMode = gameMode;
        this.playerOneMark = playerOneMark;
        this.playerTwoMark = playerOneMark.opposite();
        this.currentTurn = playerOneStarts ? playerOneMark : playerTwoMark;
        this.lastDecision = null;
    }

    public void playHumanMove(Move move) {
        ensureStarted();
        if (isMachineTurn()) {
            throw new IllegalStateException("No es el turno de un jugador humano");
        }
        if (result().isFinished()) {
            throw new IllegalStateException("La partida ya terminó");
        }

        board = board.place(move, currentTurn);
        changeTurnIfGameContinues();
    }

    public Decision playMachineMove() {
        ensureStarted();
        if (!isMachineTurn()) {
            throw new IllegalStateException("No es el turno de la computadora");
        }
        if (result().isFinished()) {
            throw new IllegalStateException("La partida ya terminó");
        }

        Mark machineMark = currentTurn;
        lastDecision = ai.chooseMove(board, machineMark);
        board = board.place(lastDecision.getMove(), machineMark);
        changeTurnIfGameContinues();
        return lastDecision;
    }

    /** Alias de compatibilidad para código que todavía llame a computerMove(). */
    public Decision computerMove() {
        return playMachineMove();
    }

    /** Alias de compatibilidad para la versión anterior de GameView. */
    public void humanMove(Move move) {
        playHumanMove(move);
    }

    public boolean isMachineTurn() {
        ensureStarted();
        return gameMode.isMachine(currentTurn, machineMark());
    }

    public boolean isHumanTurn() {
        return !isMachineTurn();
    }

    public Board board() {
        ensureStarted();
        return board;
    }

    public GameMode gameMode() {
        ensureStarted();
        return gameMode;
    }

    public Mark playerOneMark() {
        ensureStarted();
        return playerOneMark;
    }

    public Mark playerTwoMark() {
        ensureStarted();
        return playerTwoMark;
    }

    public Mark currentTurn() {
        ensureStarted();
        return currentTurn;
    }

    public Mark humanMark() {
        ensureStarted();
        if (gameMode != GameMode.PLAYER_VS_MACHINE) {
            throw new IllegalStateException("Este modo no tiene un único jugador humano");
        }
        return humanMarkInternal();
    }

    public Mark computerMark() {
        ensureStarted();
        if (gameMode != GameMode.PLAYER_VS_MACHINE) {
            throw new IllegalStateException("Este modo no tiene una única computadora");
        }
        return machineMark();
    }

    public Mark humanMarkForCurrentMode() {
        ensureStarted();
        return gameMode == GameMode.PLAYER_VS_MACHINE ? humanMarkInternal() : Mark.EMPTY;
    }

    public GameResult result() {
        ensureStarted();
        return board.result();
    }

    public Decision lastDecision() {
        return lastDecision;
    }

    private Mark humanMarkInternal() {
        return playerOneMark == machineMark() ? playerTwoMark : playerOneMark;
    }

    private Mark machineMark() {
        if (gameMode == GameMode.PLAYER_VS_MACHINE) {
            // playerOneMark representa la ficha elegida por el usuario en este modo.
            // La máquina ocupa la ficha opuesta.
            return playerTwoMark;
        }
        return currentTurn;
    }

    private void changeTurnIfGameContinues() {
        if (!board.result().isFinished()) {
            currentTurn = currentTurn.opposite();
        }
    }

    private void ensureStarted() {
        if (board == null) {
            throw new IllegalStateException("Primero debe iniciar una partida");
        }
    }
}
