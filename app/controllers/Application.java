package controllers;

import play.*;
import play.mvc.*;

import views.html.*;

import com.fasterxml.jackson.databind.JsonNode;
import models.*;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render());
    }

    /**
     * Display the chat room.
     */
    public static Result gameRoom(String username) {
        if(username == null || username.trim().equals("")) {
            return redirect(routes.Application.index());
        }
        return ok(gameRoom.render(username));
    }

    public static Result gameRoomJs(String username) {
        return ok(views.js.gameRoom.render(username));
    }

    /**
     * Handle the chat websocket.
     */
    public static WebSocket<JsonNode> game(final String username) {
        return new WebSocket<JsonNode>() {

            // Called when the Websocket Handshake is done.
            public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out){

                // Join the chat room.
                try {
                    GameRoom.join(username, in, out);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

}
