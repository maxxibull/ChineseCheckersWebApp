package models;

import java.util.ArrayList;
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

public class GameRoom extends UntypedActor {
    Map<String, WebSocket.Out<JsonNode>> members = new HashMap<String, WebSocket.Out<JsonNode>>();
    Making info;
    ArrayList<String> colors;
    int playersInGame = 0;

    public void onReceive(Object message) throws Exception {

        if(message instanceof Making) {
            Making making = (Making) message;
            info = making;
            colors = new ArrayList<String>();
            colors.add("red");
            colors.add("blue");
            colors.add("green");
            colors.add("black");
            colors.add("orange");
            colors.add("yellow");
            for(int i = 0; i < 20; i++) {
                Collections.shuffle(colors);
            }
        }
        else if(message instanceof Join) {
            Join join = (Join) message;
            members.put(join.username, join.channel);
            ObjectNode event = Json.newObject();
            event.put("user", join.username);
            event.put("color", colors.get(playersInGame));
            event.put("name", info.name);
            playersInGame++;
            join.channel.write(event);
        } 
        else if(message instanceof Move)  {
            Move move = (Move) message;
            notifyAll("move", move.username, move.oldRow, move.oldCol, move.newRow, move.newCol);
        } 
        else if(message instanceof Skip) {
            Skip skip = (Skip) message;
            for(WebSocket.Out<JsonNode> channel: members.values()) {
                ObjectNode event = Json.newObject();
                event.put("user", skip.username); //color?
                event.put("skip", "true");
                channel.write(event);
            }
        }
        else if(message instanceof Quit)  {
            Quit quit = (Quit) message; //what with it?
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

            /*ArrayNode m = event.putArray("members");
            for(String u: members.keySet()) {
                m.add(u);
            }*/

            channel.write(event);
        }
    }

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

        final String username;

        public Skip(String username) {
            this.username = username;
        }
    }

    public static class Quit {

        final String username;

        public Quit(String username) {
            this.username = username;
        }

    }

}
