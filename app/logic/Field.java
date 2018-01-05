package logic;

/**
 * Abstract class for field with pawn and color;
 */
public abstract class Field {
    protected Pawn pawn;
    protected Color color;

    /**
     * The method checks that there is a pawn on the field
     * 
     * @return true if field occupied
     */
    public boolean isOccupied() {
        return pawn != null;
    }

    /**
     * The method return pawn
     * 
     * @return pawn
     */
    public Pawn getPawn() {
        return pawn;
    }

    /**
     * The method set pawn on field
     * 
     * @param pawn new pawn
     */
    public void setPawn(Pawn pawn) {
        this.pawn = pawn;
    }

    /**
     * The method return color of field
     * 
     * @return color of field
     */
    public Color getColor() { return color; }
}