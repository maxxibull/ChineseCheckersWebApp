package logic;

/**
 * Coordinates for board.
 */
public class BoardCoordinates {
    private int row;
    private int column;

    /**
     * The main constructor
     * 
     * @param row row om board
     * @param column column on board
     */
    public BoardCoordinates(int row, int column) {
        this.row = row;
        this.column = column;
    }

    /**
     * Returns row
     * 
     * @return row 
     */
    public int getRow() {
        return row;
    }

    /**
     * Sets row
     * 
     * @param row 
     */
    public void setRow(int row) {
        this.row = row;
    }

    /**
     * Returns column
     * 
     * @return column 
     */
    public int getColumn() {
        return column;
    }

    /**
     * Sets column
     * 
     * @param column 
     */
    public void setColumn(int column) {
        this.column = column;
    }
}