package espol.com.tresenraya.model;

public class Move {
    private int row;
    private int column;

    public Move(int row, int column) {
        if (row < 0 || row > 2 || column < 0 || column > 2) {
            throw new IllegalArgumentException("La fila y columna deben estar entre 0 y 2");
        }
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public String display() {
        return "fila " + (row + 1) + ", columna " + (column + 1);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Move)) {
            return false;
        }
        Move other = (Move) object;
        return row == other.row && column == other.column;
    }

}

