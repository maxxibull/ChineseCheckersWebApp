import org.junit.Test;
import org.junit.Before;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

public class BoardBotTest {

    private Board board;
    private ArrayList<BoardCoordinates> map;
    private Player player;

    @Before
    public void setUp() throws Exception {
        this.board = new Board(6);
        this.map = board.getPawnsCoordinates();
        this.player = new Player(Color.Red, "Stevie");
    }

    @Test
    public void checkIfBotRandomizesPawnToMoveAndItsNewPositionCorrectly() {
        assertTrue(!performBotMove(player).isEmpty());
    }
}
