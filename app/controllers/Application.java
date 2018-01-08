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
    /**
     * Renders main page
     */
    public static Result index() {
        return ok(index.render());
    }

    /**
     * Renders waiting page (with 'new game' and current games)
     */
    public static Result waitingRoom(String username) {
        if(username == null || username.trim().equals("")) {
            return redirect(routes.Application.index());
        }
        return ok(waitingRoom.render(username, WaitingRoom.getGames()));
    }

    public static Result about() {
        return ok(about.render());
    }

    /**
     * Renders page with board (html)
     */
    public static Result gameRoom(String username, String gameName, int nP, int nB) {
        return ok(gameRoom.render(username, gameName, nP, nB));
    }

    /**
     * Renders page with board (js - with websockets)
     */
    public static Result gameRoomJs(String username, String nameGame, int nP, int nB) {
        return ok(views.js.gameRoom.render(username, nameGame, nP, nB));
    }

    /**
     * Renders page with settings for new games
     */
    public static Result newGame(String username) {
        return ok(newGame.render(username));
    }

    /**
     * Services websockets for new game (websocket is from creator)
     */
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

    /**
     * Services websockets for existing game
     */
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
