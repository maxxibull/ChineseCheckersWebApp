package logic;

/**
 * Includes info about color of pawn
 */
public class Pawn {
    private Color color;

    /**
     * The main constructor, sets color
     * 
     * @param color color of pawn
     */
    public Pawn(Color color) {
        this.color = color;
    }

    /** 
     * Returns color of pawn
     * 
     * @return color of pawn
     */
    public Color getColor() {
        return color;
    }
}