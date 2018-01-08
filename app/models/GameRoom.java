package models;

import logic.*;
import play.mvc.*;
import play.libs.*;
import play.libs.F.*;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import akka.actor.*;
import static akka.pattern.Patterns.ask;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.*;
import static java.util.concurrent.TimeUnit.*;

/**
 * An actor class for the game. It control players, their movements, turn etc.
 */
public class GameRoom extends UntypedActor {
    private Map<String, WebSocket.Out<JsonNode>> members = new HashMap<String, WebSocket.Out<JsonNode>>(); // websockets
    private Map<String, Player> players = new HashMap<String, Player>(); // players
    private Making info; // info about the game - name, number of players and bots
    private ArrayList<String> colors; // colors in the game
    private int playersInGame = 0; // current number of players
    private Board board; // board for the game
    private LinkedList<String> currentTurn; // ordered list of colors

    // choose colors in comparision to number of players
    private void chooseColors(int numberOfPlayers) {
        colors = new ArrayList<>();
        currentTurn = new LinkedList<>();
        switch (numberOfPlayers) {
            case 2:
                colors.add("red");
                colors.add("yellow");
                currentTurn.addFirst("red");
                currentTurn.addLast("yellow");
                break;
            case 3:
                colors.add("red");
                colors.add("blue");
                colors.add("orange");
                currentTurn.addFirst("red");
                currentTurn.addLast("blue");
                currentTurn.addLast("orange");
                break;
            case 4:
                colors.add("green");
                colors.add("black");
                colors.add("orange");
                colors.add("blue");
                currentTurn.addFirst("green");
                currentTurn.addLast("blue");
                currentTurn.addLast("orange");
                currentTurn.addLast("black");
                break;
            case 6:
                colors.add("green");
                colors.add("black");
                colors.add("orange");
                colors.add("blue");
                colors.add("red");
                colors.add("yellow");
                currentTurn.addFirst("red");
                currentTurn.addLast("green");
                currentTurn.addLast("blue");
                currentTurn.addLast("yellow");
                currentTurn.addLast("orange");
                currentTurn.addLast("black");
        }
        for(int i = 0; i < 20; i++) {
            Collections.shuffle(colors); // shuffling is for random color of player
        }
    }

    // convert string into Color object
    private Color getColorAsEnum(String color) {
        switch(color) {
            case "green":
                return Color.Green;
            case "blue":
                return Color.Blue;
            case "red":
                return Color.Red;
            case "orange":
                return Color.Orange;
            case "yellow":
                return Color.Yellow;
            case "black":
                return Color.Black;
            default:
                return null;
        }
    }

    // create appropriate number of bots
    private void createBots() {
        for(int i = 0; i < info.numberOfBots; i++) {
            players.put("Bot" + i, new Player(getColorAsEnum(colors.get(playersInGame)), "Bot" + i, true));
            playersInGame++; 
        }
    }

    // check that next turn is bot's turn
    private Player checkNextTurnIsBot() {
        Player bot = null;

        for(Player p : players.values()) {
            if(p.getColor().equals(getColorAsEnum(currentTurn.getFirst())) && p.checkPlayerIsBot()) {
                bot = p;
                break;
            }
        }

        return bot;
    }

    // perform bot move if its turn
    private void performBotMoveIfItsTurn() {
        Player bot = checkNextTurnIsBot();

        if(bot != null) {
            ArrayList<BoardCoordinates> makeMove = board.performBotMove(bot);
            getSelf().tell(new Move(bot.getUsername(), makeMove.get(0).getRow(), makeMove.get(0).getColumn(),
                    makeMove.get(1).getRow(), makeMove.get(1).getColumn()), null);
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if(message instanceof Making) { // makes new game
            Making making = (Making) message;
            info = making;
            board = new Board(info.numberOfPlayers);
            chooseColors(info.numberOfPlayers);

            if(info.numberOfBots > 0) {
                createBots();
            }
        }
        else if(message instanceof Join) { // adds players to game
            if(playersInGame < info.numberOfPlayers) {
                Join join = (Join) message;
                members.put(join.username, join.channel);

                ObjectNode event = Json.newObject();
                event.put("user", join.username);
                event.put("color", colors.get(playersInGame));
                event.put("name", info.name);
                event.put("numberOfPlayers", info.numberOfPlayers);
                join.channel.write(event);

                players.put(join.username, new Player(getColorAsEnum(colors.get(playersInGame)), join.username, false));
                playersInGame++;

                if(playersInGame == info.numberOfPlayers) {
                    getSender().tell(new WaitingRoom.Full(info.name), null);
                    notifyAll("start", currentTurn.getFirst(), "server", 0, 0, 0, 0);

                    performBotMoveIfItsTurn();
                }
            } 
            else {
                Join join = (Join) message;
                ObjectNode event = Json.newObject();
                event.put("error", "Too many players in the game. You'll back to home page.");
                join.channel.write(event);
                getSender().tell(new WaitingRoom.RemovalPlayer(join.username), null);
            }
        } 
        else if(message instanceof Move) { // makes move
            Move move = (Move) message;   
            Player bot = checkNextTurnIsBot();

            try {
                board.movePawn(new BoardCoordinates(move.oldRow, move.oldCol), new BoardCoordinates(move.newRow, move.newCol), 
                            players.get(move.username));
    
                if(!board.checkIfThePlayerIsAWinner(players.get(move.username))) {
                    currentTurn.offerLast(currentTurn.getFirst());
                }
                else {
                    notifyAll("winner", currentTurn.getFirst(), move.username, 0, 0, 0, 0);
                }
    
                currentTurn.removeFirst();
                notifyAll("move", currentTurn.getFirst(), move.username, move.oldRow, move.oldCol, move.newRow, move.newCol);
    
                performBotMoveIfItsTurn();
            } catch(Exception ex) {
                if(bot != null) {
                    getSelf().tell(new Skip(bot.getUsername(), currentTurn.getFirst()), null);
                }
            }
        } 
        else if(message instanceof Skip) { // receive and send info about skipping turn
            Skip skip = (Skip) message;
            
            if(currentTurn.getFirst().equals(skip.color)) {
                currentTurn.offerLast(currentTurn.getFirst());
                currentTurn.removeFirst();
                notifyAll("skipTurn", currentTurn.getFirst(), skip.username, 0, 0, 0, 0);
            }

            performBotMoveIfItsTurn();
        }
        else if(message instanceof Quit)  { //ends game if user left it
            Quit quit = (Quit) message;
            
            if(members.containsKey(quit.username)) {
                members.remove(quit.username);
                sendError("User " + quit.username + " has left the game");
                for(String m : members.keySet()) {
                    getSender().tell(new WaitingRoom.RemovalPlayer(m), null);
                }
                getContext().stop(getSelf());
            }
        } else {
            unhandled(message);
        }

    }

    // sends error to all members
    private void sendError(String message) {
        ObjectNode event = Json.newObject();
        event.put("error", message);

        for(WebSocket.Out<JsonNode> channel: members.values()) {
            channel.write(event);
        }
    }

    // sends a Json event to all members
    private void notifyAll(String kind, String turn, String user, int oldRow, int oldCol, int newRow, int newCol) {
        for(WebSocket.Out<JsonNode> channel: members.values()) {
            ObjectNode event = Json.newObject();
            event.put("kind", kind);
            event.put("turn", turn);
            event.put("user", user);
            event.put("oldRow", oldRow);
            event.put("oldCol", oldCol);
            event.put("newRow", newRow);
            event.put("newCol", newCol);
            channel.write(event);
        }
    }

    // Protocol - classes are used to communicate between actors

    public static class Making {

        final String name;
        final int numberOfPlayers;
        final int numberOfBots;

        public Making(String name, int numberOfPlayers, int numberOfBots) {
            this.name = name;
            this.numberOfBots = numberOfBots;
            this.numberOfPlayers = numberOfPlayers;
        }
    }

    public static class Join {

        final String username;
        final WebSocket.Out<JsonNode> channel;

        public Join(String username, WebSocket.Out<JsonNode> channel) {
            this.username = username;
            this.channel = channel;
        }

    }

    public static class Move {

        final String username;
        final int oldRow;
        final int oldCol;
        final int newRow;
        final int newCol;

        public Move(String username, int oldRow, int oldCol, int newRow, int newCol) {
            this.username = username;
            this.newRow = newRow;
            this.newCol = newCol;
            this.oldRow = oldRow;
            this.oldCol = oldCol;
        }

    }

    public static class Skip {
        final String color;
        final String username;

        public Skip(String username, String color) {
            this.username = username;
            this.color = color;
        }
    }

    public static class Quit {

        final String username;

        public Quit(String username) {
            this.username = username;
        }

    }

}
