package espol.com.tresenraya.model;

public enum Mark {
    X("X"), O("O"), EMPTY("");

    private final String symbol;

    Mark(String symbol) {
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }

    public Mark opposite() {
        if (this == X) {
            return O;
        }
        if (this == O) {
            return X;
        }
        return EMPTY;
    }
}

