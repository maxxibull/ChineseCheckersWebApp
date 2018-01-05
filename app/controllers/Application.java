package controllers;

import play.*;
import play.mvc.*;
import views.html.*;
import com.fasterxml.jackson.databind.JsonNode;
import models.*;

/**
 * This class is the main controller of all users' queries and calls.
 */
public class Application extends Controller {
    public static Result index() {
        return ok(index.render());
    }

    public static Result waitingRoom(String username) {
        if(username == null || username.trim().equals("")) {
            return redirect(routes.Application.index());
        }
        return ok(waitingRoom.render(username, WaitingRoom.getGames()));
    }

    public static Result gameRoom(String username, String gameName, int nP, int nB) {
        return ok(gameRoom.render(username, gameName, nP, nB));
    }

    public static Result gameRoomJs(String username, String nameGame, int nP, int nB) {
        return ok(views.js.gameRoom.render(username, nameGame, nP, nB));
    }

    public static Result newGame(String username) {
        return ok(newGame.render(username));
    }

    public static WebSocket<JsonNode> gameNew(final String username, final String nameGame, final int players, final int bots) {
        return new WebSocket<JsonNode>() {
            public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out){
                try {
                    WaitingRoom.join(username, true, nameGame, players, bots, in, out);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    public static WebSocket<JsonNode> game(final String username, final String nameGame) {
        return new WebSocket<JsonNode>() {
            public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out){
                try {
                    WaitingRoom.join(username, false, nameGame, 0, 0, in, out);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

}
