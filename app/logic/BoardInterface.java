package logic;

import logic.exceptions.*;

import java.util.ArrayList;

public interface BoardInterface {

    void movePawn(BoardCoordinates oldPawnPosition, BoardCoordinates newPawnPosition, Player player)
            throws NullFieldException, WrongFieldStateException, WrongMoveException, WrongPawnColorException;

    boolean checkIfThePlayerIsAWinner(Player player);

    ArrayList<BoardCoordinates> getPawnsCoordinates();

    Color getPawnColor(BoardCoordinates pawnPosition) throws WrongFieldStateException;

}