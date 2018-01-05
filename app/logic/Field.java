package logic;

public abstract class Field {

    protected Pawn pawn;
    protected Color color;

    public boolean isOccupied() {
        return pawn != null;
    }

    public Pawn getPawn() {
        return pawn;
    }

    public void setPawn(Pawn pawn) {
        this.pawn = pawn;
    }

    public Color getColor() { return color; }
}