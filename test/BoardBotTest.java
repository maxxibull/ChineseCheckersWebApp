package test;

import org.junit.*;

import play.mvc.*;
import play.test.*;
import play.libs.F.*;

import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;

import logic.*;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class BoardBotTest {

    private Board board;
    private ArrayList<BoardCoordinates> map;
    private Player player;

    @Before
    public void setUp() throws Exception {
        this.board = new Board(6);
        this.map = board.getPawnsCoordinates();
        this.player = new Player(logic.Color.Red, "Stevie", false);
    }

    @Test
    public void checkIfPawnIsFoundCorrectlyForTheBlackPlayer() throws Exception {
        Player player = new Player(logic.Color.Black, "Julie", true);

        board.movePawn(board.performBotMove(player).get(0), board.performBotMove(player).get(1), player);
    }

    @Test
    public void checkIfBotRandomizesPawnToMoveAndItsNewPositionCorrectly() {
        assertTrue(!performBotMove(player).isEmpty());
    }
}
