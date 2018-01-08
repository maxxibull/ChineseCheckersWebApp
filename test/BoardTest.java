package test;

import org.junit.*;

import play.mvc.*;
import play.test.*;
import play.libs.F.*;

import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;

import logic.exceptions.WrongFieldStateException;
import logic.exceptions.WrongMoveException;

import logic.*;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;


public class BoardTest {

    private Board board;
    private ArrayList<BoardCoordinates> map;
    private Player player;

    @Before
    public void setUp() throws Exception {
        this.board = new Board(6);
        this.map = board.getPawnsCoordinates();
        this.player = new Player(logic.Color.Red, "Stevie", false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void checkIfGameDoesNotWorkIfInvalidPlayersNumberIsProvided() throws IllegalArgumentException {
        Board board = new Board(10);
    }

    @Test
    public void checkIfPawnMoves() throws Exception {
        board.movePawn(new BoardCoordinates(4, 5), new BoardCoordinates(5, 6), player);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void checkIfPawnDoesNotMoveToInvalidPosition() throws Exception {
        board.movePawn(new BoardCoordinates(4, 5), new BoardCoordinates(400, 500), player);
    }

    @Test
    public void checkIfPawnMovesToDesiredPosition() throws Exception {
        board.movePawn(new BoardCoordinates(4, 5), new BoardCoordinates(5, 6), player);
        assertEquals(logic.Color.Red, board.getPawnColor(new BoardCoordinates(5, 6)));
    }

    @Test
    public void checkIfPawnMovesOverOtherPawns() throws Exception {
        board.movePawn(new BoardCoordinates(3, 6), new BoardCoordinates(5, 8), player);
        assertEquals(logic.Color.Red, board.getPawnColor(new BoardCoordinates(5, 8)));
    }

    @Test(expected = WrongFieldStateException.class)
    public void checkIfCannotPerformMoveOnFieldWithNoPawn() throws Exception {
        board.movePawn(new BoardCoordinates(7, 5), new BoardCoordinates(5, 8), player);
        assertEquals(logic.Color.Red, board.getPawnColor(new BoardCoordinates(5, 8)));
    }

    @Test(expected = WrongMoveException.class)
    public void checkIfDoesNotMoveOutOfMoveRange() throws Exception {
        board.movePawn(new BoardCoordinates(4, 5), new BoardCoordinates(6, 7), player);
    }

    @Test(expected = WrongMoveException.class)
    public void checkIfPawnDoesNotMoveOutsideOfEnemyBase() throws Exception {

        // Creates new Blue player
        Player yellowPlayer = new Player(logic.Color.Yellow, "Jackie", false);
        int yellowRow = 14;
        int yellowColumn = 13;

        // Moves field from (4, 7) near the enemy's base to (10, 13)
        int row = 4;
        int column = 5;

        do {
            int nextRow = row + 1, nextColumn = column + 1;

            board.movePawn(new BoardCoordinates(row, column), new BoardCoordinates(nextRow, nextColumn), player);
            row++;
            column++;
        }
        while (row != 12 && column != 13);

        board.movePawn(new BoardCoordinates(12, 13), new BoardCoordinates(13, 13), player);

        // Checks if movement has been performed correctly
        assertEquals(logic.Color.Red, board.getPawnColor(new BoardCoordinates(13, 13)));


        do {
            int nextRow = yellowRow - 1, nextColumn =  yellowColumn - 1;
            //System.out.println("Row: " + blueRow + ", column: " + blueColumn);
            board.movePawn(new BoardCoordinates(yellowRow, yellowColumn), new BoardCoordinates(nextRow, nextColumn), yellowPlayer);
            yellowRow--;
            yellowColumn--;
        }
        while (yellowRow != 6 && yellowColumn != 5);

        board.movePawn(new BoardCoordinates(6, 5), new BoardCoordinates(5, 5), yellowPlayer);

        // Checks if movement has been performed correctly
        assertEquals(logic.Color.Yellow, board.getPawnColor(new BoardCoordinates(5, 5)));

        // Let's move two pawns inside each other's bases
        board.movePawn(new BoardCoordinates(5, 5), new BoardCoordinates(4, 5), yellowPlayer);
        board.movePawn(new BoardCoordinates(13, 13), new BoardCoordinates(14,13), player);

        assertEquals(logic.Color.Yellow, board.getPawnColor(new BoardCoordinates(4, 5)));
        assertEquals(logic.Color.Red, board.getPawnColor(new BoardCoordinates(14, 13)));

        // Finally, let's see if we can move the pawn that's already in enemy's base outside of it
        board.movePawn(new BoardCoordinates(4, 5), new BoardCoordinates(5, 5), yellowPlayer);
        board.movePawn(new BoardCoordinates(14, 13), new BoardCoordinates(13,13), player);
    }

    @org.junit.After
    public void tearDown() throws Exception {
    }

}