package models;

import play.mvc.*;
import play.libs.*;
import play.libs.F.*;
import java.util.ArrayList;
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
 * Waiting room is the most important actor, receives messages from websockets and creates games.
 */
public class WaitingRoom extends UntypedActor {
    static ActorRef defaultRoom = Akka.system().actorOf(Props.create(WaitingRoom.class)); // Default room
    static Map<String, ActorRef> games = new HashMap<String, ActorRef>(); // existing games
    static Map<String, WebSocket.Out<JsonNode>> members = new HashMap<String, WebSocket.Out<JsonNode>>(); // members that joined

    /**
     * Joins to existing game or creates new one. Manages all websockets.
     * 
     * @param username name of player
     * @param isNewGame true if new websocket is from creator
     * @param nameGame name of game
     * @param players number of players for game (includes number of bots)
     * @param bots number of bots in game
     * @param in channel to receive messages from websocket 
     * @param out channel to connect with weboscket
     */
    public static void join(final String username, final Boolean isNewGame, final String nameGame, final int players, final int bots, WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) throws Exception {
        String result = (String) Await.result(ask(defaultRoom, new Join(username, out), 1000), Duration.create(1, SECONDS)); // checks name of player is free

        if("OK".equals(result)) {
            if(isNewGame && !games.containsKey(nameGame)) { // creates new game
                ActorRef newGame = Akka.system().actorOf(Props.create(GameRoom.class));
                games.put(nameGame, newGame);
                newGame.tell(new GameRoom.Making(nameGame, players, bots), null);
                newGame.tell(new GameRoom.Join(username, members.get(username)), null);
            }
            else if(isNewGame && games.containsKey(nameGame)) { // if name of games was used earlier
                ObjectNode event = Json.newObject();
                event.put("error", "This name of game was used. You'll back to home page.");
                out.write(event);
            }
            else { // joins to existing name
                ActorRef currentGame = games.get(nameGame); 
                currentGame.tell(new GameRoom.Join(username, members.get(username)), null);
            }

            // For each event received on the socket,
            in.onMessage(new Callback<JsonNode>() {
                public void invoke(JsonNode event) {
                    if(event.has("nameOfGame") && games.containsKey(event.get("nameOfGame").toString().replace("\"", "")) &&
                                event.has("skipTurn")) { // skips turn
                        ActorRef currentGame = games.get(event.get("nameOfGame").toString().replace("\"", ""));
                        currentGame.tell(new GameRoom.Skip(username, event.get("color").toString().replace("\"", "")), null);
                    }
                    else if(event.has("nameOfGame") && games.containsKey(event.get("nameOfGame").toString().replace("\"", ""))) { // makes move
                        ActorRef currentGame = games.get(event.get("nameOfGame").toString().replace("\"", ""));
                        currentGame.tell(new GameRoom.Move(username, event.get("oldRow").asInt(), event.get("oldCol").asInt(),
                            event.get("newRow").asInt(), event.get("newCol").asInt()), null);
                    }
                }
            });

            // When the socket is closed.
            in.onClose(new Callback0() {
                public void invoke() {
                    ActorRef currentGame = games.get(nameGame);
                    currentGame.tell(new GameRoom.Quit(username), defaultRoom);
                    defaultRoom.tell(new Quit(nameGame, username), null);
                }
            });
        } 
        else {
            // Cannot connect, creates a Json error.
            ObjectNode error = Json.newObject();
            error.put("error", result);
            out.write(error);
        }

    }

    @Override
    public void onReceive(Object message) throws Exception {
        if(message instanceof Join) {
            Join join = (Join) message;

            if(members.containsKey(join.username)) {
                getSender().tell("This username is already used. You'll back to home page.", getSelf());
            } else {
                members.put(join.username, join.channel);
                getSender().tell("OK", getSelf());
            }
        }
        else if(message instanceof Quit)  {
            Quit quit = (Quit) message;

            members.remove(quit.username);
            games.remove(quit.nameOfGame);
        } 
        else if(message instanceof RemovalPlayer) {
            RemovalPlayer removal = (RemovalPlayer) message;

            members.remove(removal.username);
        }
        else {
            unhandled(message);
        }

    }

    /**
     * Returns json (as String) with names of existing games
     * 
     * @return names of existing names (String - json)
     */
    public static String getGames() {
        String newJSON = "{games:[";
        for(String key : games.keySet()) {
            newJSON += "`" + key + "`,";
        }
        newJSON += "]}";
        return newJSON;
    }

    // Protocol - classes are used to communicate between actors
    
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

    public static class Quit {
        final String nameOfGame;
        final String username;

        public Quit(String nameOfGame, String username) {
            this.nameOfGame = nameOfGame;
            this.username = username;
        }
    }

    public static class RemovalPlayer {
        final String username;

        public RemovalPlayer(String username) {
            this.username = username;
        }
    }
}
