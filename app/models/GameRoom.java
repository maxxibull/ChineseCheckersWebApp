package models;

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
 * A chat room is an Actor.
 */
public class GameRoom extends UntypedActor {

    // Default room.
    static ActorRef defaultRoom = Akka.system().actorOf(Props.create(GameRoom.class));

    /**
     * Join the default room.
     */
    public static void join(final String username, WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) throws Exception{

        // Send the Join message to the room
        String result = (String)Await.result(ask(defaultRoom,new Join(username, out), 1000), Duration.create(1, SECONDS));

        if("OK".equals(result)) {

            // For each event received on the socket,
            in.onMessage(new Callback<JsonNode>() {
                public void invoke(JsonNode event) {

                    // Send a Talk message to the room.
                    defaultRoom.tell(new Move(username, event.get("oldRow").asInt(), event.get("oldCol").asInt(),
                            event.get("newRow").asInt(), event.get("newCol").asInt()), null);

                }
            });

            // When the socket is closed.
            in.onClose(new Callback0() {
                public void invoke() {

                    // Send a Quit message to the room.
                    defaultRoom.tell(new Quit(username), null);

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
    Map<String, WebSocket.Out<JsonNode>> members = new HashMap<String, WebSocket.Out<JsonNode>>();

    public void onReceive(Object message) throws Exception {

        if(message instanceof Join) {

            // Received a Join message
            Join join = (Join)message;

            // Check if this username is free.
            if(members.containsKey(join.username)) {
                getSender().tell("This username is already used", getSelf());
            } else {
                members.put(join.username, join.channel);
                getSender().tell("OK", getSelf());
            }

        } else if(message instanceof Move)  {

            // Received a Talk message
            Move move = (Move)message;

            notifyAll("move", move.username, move.oldRow, move.oldCol, move.newRow, move.newCol);

        } else if(message instanceof Quit)  {

            // Received a Quit message
            Quit quit = (Quit)message;

            members.remove(quit.username);

            //notifyAll("quit", quit.username, "has left the room");

        } else {
            unhandled(message);
        }

    }

    // Send a Json event to all members
    public void notifyAll(String kind, String user, int oldRow, int oldCol, int newRow, int newCol) {
        for(WebSocket.Out<JsonNode> channel: members.values()) {

            ObjectNode event = Json.newObject();
            event.put("kind", kind);
            event.put("user", user);
            event.put("oldRow", oldRow);
            event.put("oldCol", oldCol);
            event.put("newRow", newRow);
            event.put("newCol", newCol);

            ArrayNode m = event.putArray("members");
            for(String u: members.keySet()) {
                m.add(u);
            }

            channel.write(event);
        }
    }

    // -- Messages

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

        final String username;

        public Quit(String username) {
            this.username = username;
        }

    }

}
