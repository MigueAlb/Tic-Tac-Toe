package espol.com.tresenraya.model;

public enum GameMode {
    PLAYER_VS_PLAYER("Jugador vs Jugador", "Dos tripulantes se enfrentan cara a cara.", "👥"),
    PLAYER_VS_MACHINE("Jugador vs Mishi", "Juega contra la inteligencia de Mishi Cósmico.", "🤖"),
    MACHINE_VS_MACHINE("Mishi vs Mishi", "Observa a dos inteligencias enfrentarse.", "⚔");

    private final String displayName;
    private final String description;
    private final String icon;

    GameMode(String displayName, String description, String icon) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }

    public String icon() {
        return icon;
    }

    public boolean isMachine(Mark mark, Mark machineMark) {
        if (this == MACHINE_VS_MACHINE) {
            return true;
        }
        if (this == PLAYER_VS_PLAYER) {
            return false;
        }
        return mark == machineMark;
    }
}