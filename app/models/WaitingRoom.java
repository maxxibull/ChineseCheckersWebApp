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
 * A chat room is an Actor.
 */
public class WaitingRoom extends UntypedActor {

    // Default room.
    static ActorRef defaultRoom = Akka.system().actorOf(Props.create(WaitingRoom.class));

    static Map<String, ActorRef> games = new HashMap<String, ActorRef>();

    /**
     * Join the default room.
     */
    public static void join(final String username, final Boolean isNewGame, final String nameGame, final int players, final int bots, WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) throws Exception{

        // Send the Join message to the room
        String result = (String)Await.result(ask(defaultRoom, new Join(username, out), 1000), Duration.create(1, SECONDS));

        if("OK".equals(result)) {
            if(isNewGame && !games.containsKey(nameGame)) {
                ActorRef newGame = Akka.system().actorOf(Props.create(GameRoom.class));
                games.put(nameGame, newGame);
                newGame.tell(new GameRoom.Making(nameGame, players, bots), null);
                newGame.tell(new GameRoom.Join(username, members.get(username)), null);
            }
            else if(isNewGame && games.containsKey(nameGame)) {
                ObjectNode event = Json.newObject();
                event.put("error", "This name of game was used. You'll back to home page.");
                out.write(event);
            }
            else {
                ActorRef currentGame = games.get(nameGame);
                currentGame.tell(new GameRoom.Join(username, members.get(username)), null);
            }

            // For each event received on the socket,
            in.onMessage(new Callback<JsonNode>() {
                public void invoke(JsonNode event) {
                    if(event.has("nameOfGame") && games.containsKey(event.get("nameOfGame").toString().replace("\"", "")) &&
                                event.has("skipTurn")) {
                        ActorRef currentGame = games.get(event.get("nameOfGame").toString().replace("\"", ""));
                        currentGame.tell(new GameRoom.Skip(username, event.get("color").toString().replace("\"", "")), null);
                    }
                    else if(event.has("nameOfGame") && games.containsKey(event.get("nameOfGame").toString().replace("\"", ""))) {
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

        } else {

            // Cannot connect, create a Json error.
            ObjectNode error = Json.newObject();
            error.put("error", result);

            // Send the error to the socket.
            out.write(error);

        }

    }

    // Members of this room.
    static Map<String, WebSocket.Out<JsonNode>> members = new HashMap<String, WebSocket.Out<JsonNode>>();

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

    public static String getGames() {
        String newJSON = "{games:[";
        for(String key : games.keySet()) {
            newJSON += "`" + key + "`,";
        }
        newJSON += "]}";
        return newJSON;
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
