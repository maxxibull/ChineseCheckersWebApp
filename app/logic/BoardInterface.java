package logic;

import logic.exceptions.*;
import java.util.ArrayList;

/**
 * The interface of board for chinese checkers.
 */
public interface BoardInterface {

    /**
     * The method moves the pawn into its new field.
     *
     * @param oldPawnPosition previous position of the pawn
     * @param newPawnPosition new position of the pawn
     * @param player          reference to player who wants to move the pawn
     * @throws NullFieldException       thrown if one or both of coordinates point to null
     * @throws WrongFieldStateException thrown if there is a problem with pawn
     * @throws WrongMoveException       thrown if move is incorrect
     * @throws WrongPawnColorException  thrown if players wants to move somebody's else pawn
     */
    void movePawn(BoardCoordinates oldPawnPosition, BoardCoordinates newPawnPosition, Player player)
            throws NullFieldException, WrongFieldStateException, WrongMoveException, WrongPawnColorException;

    /**
     * The method checks if the player is the winner.
     *
     * @param player the player whose result we want to check
     * @return true if all pawns of player are in the opposite base
     */
    boolean checkIfThePlayerIsAWinner(Player player);

    /**
     * The method returns current coordinates of all pawns.
     *
     * @return array of coordinates of pawns
     */
    ArrayList<BoardCoordinates> getPawnsCoordinates();

    /**
     * Performs the movement of the randomly chosen pawn owned by the player which is handled by the bot.
     * 
     * @param bot player that is a bot
     * @return array of coordinates
     */
    ArrayList<BoardCoordinates> performBotMove(Player bot);
}