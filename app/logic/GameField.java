package logic;

/**
 * Field for chinese checkers and board
 */
public class GameField extends Field {
    /**
     * The constructor for neutral fields (not in base)
     */
    GameField() {
        color = Color.Neutral;
    }

    /**
     * The constructor that sets particular color for field
     * 
     * @param color color of field
     */
    GameField(Color color) {
        this.color = color;
    }
}