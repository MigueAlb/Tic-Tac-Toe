package espol.com.tresenraya.model;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String email;
    private String password;
    private String name;
    private int wins;
    private int losses;
    private int draws;

    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.wins = 0;
        this.losses = 0;
        this.draws = 0;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getDraws() {
        return draws;
    }

    public int getTotalGames() {
        return wins + losses + draws;
    }

    public void addWin() {
        wins++;
    }

    public void addLoss() {
        losses++;
    }

    public void addDraw() {
        draws++;
    }
}
