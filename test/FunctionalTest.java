package test;

import org.junit.*;

import play.mvc.*;
import play.test.*;
import play.libs.F.*;

import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;
import play.*;

import logic.*;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class FunctionalTest {

    @Test
    public void checkIfIndexPageWorks() {
        Result result = callAction(
                controllers.routes.ref.Application.index()
        );
        assertThat(status(result)).isEqualTo(OK);
        assertThat(contentType(result)).isEqualTo("text/html");
        assertThat(charset(result)).isEqualTo("utf-8");
        assertThat(contentAsString(result)).contains("Chinese Checkers");
    }

    @Test
    public void checkIfWaitingRoomRendersProperly() {
        Content html = views.html.waitingRoom.render("test", "game1");
        assertThat(contentType(html)).isEqualTo("text/html");
        assertThat(contentAsString(html)).contains("test");
    }

    @Test
    public void checkIfTheNewGameScreenRendersProperly() {
        Content html = views.html.newGame.render("test");
        assertThat(contentType(html)).isEqualTo("text/html");
        assertThat(contentAsString(html)).contains("Number of players");
    }

    @Test
    public void checkIfTheGameScreenRendersProperly() {
        Content html = views.html.gameRoom.render("test", "test room", 2, 1);
        assertThat(contentType(html)).isEqualTo("text/html");
        assertThat(contentAsString(html)).contains("test room");
    }

    @Test
    public void checkIfTheBadRouteDoesNotReturnAnyValueForWaitingRoom() {
        Result result = routeAndCall(fakeRequest(GET, "/waitingroom/users"));
        assertThat(result).isNull();
    }

    @Test
    public void checkIfTheBadRouteDoesNotReturnAnyValueForGameRoom() {
        Result result = routeAndCall(fakeRequest(GET, "/gameroom"));
        assertThat(result).isNull();
    }

    @Test
    public void checkIfTheBadRouteDoesNotReturnAnyValueForNewGameRoom() {
        Result result = routeAndCall(fakeRequest(GET, "/room/newgame/game1"));
        assertThat(result).isNull();
    }


}