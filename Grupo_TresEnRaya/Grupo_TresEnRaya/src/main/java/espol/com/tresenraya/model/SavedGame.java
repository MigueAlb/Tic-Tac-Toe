package espol.com.tresenraya.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Datos persistentes necesarios para reanudar una partida. */
public final class SavedGame implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String ownerEmail;
    private final String playerName;
    private final GameMode gameMode;
    private final Mark playerOneMark;
    private final boolean playerOneStarts;
    private final List<PlayedMove> moves;
    private final LocalDateTime savedAt;

    public SavedGame(String id, String ownerEmail, String playerName,
                     GameMode gameMode, Mark playerOneMark,
                     boolean playerOneStarts, List<PlayedMove> moves,
                     LocalDateTime savedAt) {
        this.id = id;
        this.ownerEmail = ownerEmail;
        this.playerName = playerName;
        this.gameMode = gameMode;
        this.playerOneMark = playerOneMark;
        this.playerOneStarts = playerOneStarts;
        this.moves = new ArrayList<>(moves);
        this.savedAt = savedAt;
    }

    public String getId() { return id; }
    public String getOwnerEmail() { return ownerEmail; }
    public String getPlayerName() { return playerName; }
    public GameMode getGameMode() { return gameMode; }
    public Mark getPlayerOneMark() { return playerOneMark; }
    public boolean isPlayerOneStarts() { return playerOneStarts; }
    public List<PlayedMove> getMoves() { return Collections.unmodifiableList(moves); }
    public LocalDateTime getSavedAt() { return savedAt; }

    public int getMoveCount() { return moves.size(); }

    @Override
    public String toString() {
        String mode = gameMode == null ? "Partida" : gameMode.displayName();
        String date = savedAt == null ? "" : savedAt.toString().replace('T', ' ');
        return mode + " · " + moves.size() + " movimientos · " + date;
    }
}

