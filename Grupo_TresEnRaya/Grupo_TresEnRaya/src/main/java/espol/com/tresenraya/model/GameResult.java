package espol.com.tresenraya.model;

public enum GameResult {
    IN_PROGRESS, X_WINS, O_WINS, DRAW;

    public boolean isFinished() {
        return this != IN_PROGRESS;
    }
}

