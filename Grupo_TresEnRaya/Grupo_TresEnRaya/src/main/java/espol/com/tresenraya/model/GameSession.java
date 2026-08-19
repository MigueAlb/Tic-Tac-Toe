package espol.com.tresenraya.model;

import espol.com.tresenraya.ai.Decision;
import espol.com.tresenraya.ai.MinimaxAI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GameSession {
    private final MinimaxAI ai = new MinimaxAI();
    private Board board;
    private GameMode gameMode;
    private Mark playerOneMark;
    private Mark playerTwoMark;
    private Mark currentTurn;
    private Decision lastDecision;
    private boolean playerOneStarts;
    private final List<PlayedMove> moveHistory = new ArrayList<>();

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
        this.playerOneStarts = playerOneStarts;
        this.currentTurn = playerOneStarts ? playerOneMark : playerTwoMark;
        this.lastDecision = null;
        this.moveHistory.clear();
    }

    public void playHumanMove(Move move) {
        ensureStarted();
        if (isMachineTurn()) {
            throw new IllegalStateException("No es el turno de un jugador humano");
        }
        if (result().isFinished()) {
            throw new IllegalStateException("La partida ya terminó");
        }

        Mark playedMark = currentTurn;
        board = board.place(move, playedMark);
        moveHistory.add(new PlayedMove(move, playedMark));
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
        moveHistory.add(new PlayedMove(lastDecision.getMove(), machineMark));
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

    /** Calcula una recomendación para el jugador que tiene el turno sin modificar la partida. */
    public Decision recommendMove() {
        ensureStarted();
        if (!isHumanTurn()) {
            throw new IllegalStateException("No es el turno de un jugador humano");
        }
        if (result().isFinished()) {
            throw new IllegalStateException("La partida ya terminó");
        }
        return ai.chooseMove(board, currentTurn);
    }


    public List<PlayedMove> moveHistory() {
        ensureStarted();
        return Collections.unmodifiableList(moveHistory);
    }
    /**
     * Recalcula las decisiones de Minimax correspondientes a los movimientos
     * de máquina ya realizados. Se usa al reabrir una partida para no perder
     * el análisis de las decisiones anteriores.
     */
    public List<Decision> rebuildMachineDecisions() {
        ensureStarted();
        List<Decision> decisions = new ArrayList<>();
        Board replayBoard = new Board();
        Mark turn = playerOneStarts ? playerOneMark : playerTwoMark;

        for (PlayedMove playedMove : moveHistory) {
            boolean machineTurn = gameMode == GameMode.MACHINE_VS_MACHINE
                    || (gameMode == GameMode.PLAYER_VS_MACHINE && turn == playerTwoMark);
            if (machineTurn) {
                Decision decision = ai.chooseMove(replayBoard, turn);
                if (decision.getMove().equals(playedMove.toMove())) {
                    decisions.add(decision);
                }
            }
            replayBoard = replayBoard.place(playedMove.toMove(), playedMove.getMark());
            if (!replayBoard.result().isFinished()) {
                turn = turn.opposite();
            }
        }
        return decisions;
    }


    public boolean playerOneStarts() {
        ensureStarted();
        return playerOneStarts;
    }

    /** Restaura una partida guardada y deja el turno correcto listo para continuar. */
    public void restore(SavedGame savedGame) {
        if (savedGame == null) {
            throw new IllegalArgumentException("La partida guardada no puede ser nula");
        }
        if (savedGame.getGameMode() == null || savedGame.getPlayerOneMark() == null) {
            throw new IllegalArgumentException("La partida guardada está incompleta");
        }

        start(savedGame.getGameMode(), savedGame.getPlayerOneMark(), savedGame.isPlayerOneStarts());
        for (PlayedMove playedMove : savedGame.getMoves()) {
            if (result().isFinished()) {
                throw new IllegalArgumentException("La partida guardada contiene movimientos después de terminar");
            }
            if (playedMove.getMark() != currentTurn) {
                throw new IllegalArgumentException("El orden de turnos de la partida guardada es inválido");
            }
            board = board.place(playedMove.toMove(), playedMove.getMark());
            moveHistory.add(playedMove);
            changeTurnIfGameContinues();
        }
        lastDecision = null;
    }

    public SavedGame createSave(String ownerEmail, String playerName) {
        return createSave(ownerEmail, playerName, java.util.UUID.randomUUID().toString());
    }

    public SavedGame createSave(String ownerEmail, String playerName, String id) {
        ensureStarted();
        return new SavedGame(
                id,
                ownerEmail,
                playerName,
                gameMode,
                playerOneMark,
                playerOneStarts,
                moveHistory,
                java.time.LocalDateTime.now());
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