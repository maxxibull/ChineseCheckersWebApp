package logic;

import logic.BoardCoordinates;
import logic.exceptions.*;
import java.util.*;
import java.util.ArrayList;

/**
 * The class creates board for chinese checkers, manages it, checks correctness of movements and makes movements for bots.
 */
public class Board implements BoardInterface {
    private int numberOfPlayers; // number of players - with bots
    private ArrayList<ArrayList<Field>> gameBoard; // array for GameField objects
    private ArrayList<Color> colorsOfPlayersOnBoard; // colors of pawns
    private ArrayList<BoardCoordinates> jumps; // array for checkPossibleWaysForPawn and checkMultiMoveIsCorrect methods

    /**
     * The class has only one constructor that creates gameBoard with GameField objects and Pawn objects.
     * What is more, the constructor choose colors which will be used by players.
     * 
     * @param numberOfPlayers number of players with bots
     * @throws IllegalArgumentException thrown when number of players is different than 2, 3, 4 or 6
     */
    public Board(int numberOfPlayers) throws IllegalArgumentException {

        if (numberOfPlayers != 2 && numberOfPlayers != 3 &&
                numberOfPlayers != 4 && numberOfPlayers != 6) {
            throw new IllegalArgumentException();
        }

        this.numberOfPlayers = numberOfPlayers;
        this.gameBoard = assignFieldsToBoard();
        colorsOfPlayersOnBoard = setColorsOfPlayers();

        assignPawnsToBaseFields();
    }

    /*
    The gameBoard (rectangular 2D array of size 19x19) is like:
    ["null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"],
    ["null", "null", "null", "null", "null", "RED_", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"],
    ["null", "null", "null", "null", "null", "RED_", "RED_", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"],
    ["null", "null", "null", "null", "null", "RED_", "RED_", "RED_", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"],
    ["null", "null", "null", "null", "null", "RED_", "RED_", "RED_", "RED_", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"],
    ["null", "BLAC", "BLAC", "BLAC", "BLAC", "EMPT", "EMPT", "EMPT", "EMPT", "EMPT", "GREE", "GREE", "GREE", "GREE", "null", "null", "null", "null", "null"],
    ["null", "null", "BLAC", "BLAC", "BLAC", "EMPT", "EMPT", "EMPT", "EMPT", "EMPT", "EMPT", "GREE", "GREE", "GREE", "null", "null", "null", "null", "null"],
    ["null", "null", "null", "BLAC", "BLAC", "EMPT", "EMPT", "EMPT", "EMPT", "EMPT", "EMPT", "EMPT", "GREE", "GREE", "null", "null", "null", "null", "null"],
    ["null", "null", "null", "null", "BLAC", "EMPT", "EMPT", "EMPT", "EMPT", "EMPT", "EMPT", "EMPT", "EMPT", "GREE", "null", "null", "null", "null", "null"],
    ["null", "null", "null", "null", "null", "EMPT", "EMPT", "EMPT", "EMPT", "EMPT", "EMPT", "EMPT", "EMPT", "EMPT", "null", "null", "null", "null", "null"],
    ["null", "null", "null", "null", "null", "ORAN", "EMPT", "EMPT", "EMPT", "EMPT", "EMPT", "EMPT", "EMPT", "EMPT", "BLUE", "null", "null", "null", "null"],
    ["null", "null", "null", "null", "null", "ORAN", "ORAN", "EMPT", "EMPT", "EMPT", "EMPT", "EMPT", "EMPT", "EMPT", "BLUE", "BLUE", "null", "null", "null"],
    ["null", "null", "null", "null", "null", "ORAN", "ORAN", "ORAN", "EMPT", "EMPT", "EMPT", "EMPT", "EMPT", "EMPT", "BLUE", "BLUE", "BLUE", "null", "null"],
    ["null", "null", "null", "null", "null", "ORAN", "ORAN", "ORAN", "ORAN", "EMPT", "EMPT", "EMPT", "EMPT", "EMPT", "BLUE", "BLUE", "BLUE", "BLUE", "null"],
    ["null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "YELL", "YELL", "YELL", "YELL", "null", "null", "null", "null", "null"],
    ["null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "YELL", "YELL", "YELL", "null", "null", "null", "null", "null"],
    ["null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "YELL", "YELL", "null", "null", "null", "null", "null"],
    ["null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "YELL", "null", "null", "null", "null", "null"],
    ["null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"],
    RED_, BLAC, GREE, ORAN, YELL and BLUE are colors of BaseFields and EMPT is NeutralField.
    Null means that there is no Field.
     */

    // Creates an array of fields (GameField)
    private ArrayList<ArrayList<Field>> assignFieldsToBoard() {
        ArrayList<ArrayList<Field>> newGameBoard = new ArrayList<>();
        ArrayList<Field> newRow;
        for (int i = 0; i < 19; i++) {
            newRow = new ArrayList<>();
            for (int j = 0; j < 19; j++) {
                if (i < 5 && j > 4 && j < i + 5) {
                    newRow.add(j, new GameField(Color.Red));
                } else if (i >= 5 && i < 9) {
                    if (j > i - 5 && j < 5) {
                        newRow.add(j, new GameField(Color.Black));
                    } else if (j >= 5 && j < i + 5) {
                        newRow.add(j, new GameField());
                    } else if (j >= i + 5 && j < 14) {
                        newRow.add(j, new GameField(Color.Green));
                    } else {
                        newRow.add(j, null);
                    }
                } else if (i == 9 && j > 4 && j < 14) {
                    newRow.add(j, new GameField());
                } else if (i > 9 && i < 14) {
                    if (j > 4 && j < i - 4) {
                        newRow.add(j, new GameField(Color.Orange));
                    } else if (j >= i - 4 && j < 14) {
                        newRow.add(j, new GameField());
                    } else if (j >= 14 && j < i + 5) {
                        newRow.add(j, new GameField(Color.Blue));
                    } else {
                        newRow.add(j, null);
                    }
                } else if (i >= 14 && j > i - 5 && j < 14) {
                    newRow.add(j, new GameField(Color.Yellow));
                } else {
                    newRow.add(j, null);
                }
            }
            newGameBoard.add(i, newRow);
        }
        return newGameBoard;
    }

    // Creates new pawns and places it on appropriate field
    private void assignPawnsToBaseFields() {
        ArrayList<Field> list;
        for (int i = 0; i < 19; i++) {
            list = gameBoard.get(i);
            for (Field f : list) {
                if (f != null && f.getColor() != null && colorsOfPlayersOnBoard.contains(f.getColor())) {
                    f.setPawn(new Pawn(f.getColor()));
                }
            }
        }
    }

    // Chooses colors for players
    private ArrayList<Color> setColorsOfPlayers() {
        ArrayList<Color> colors = new ArrayList<>();

        switch (numberOfPlayers) {
            case 2:
                colors.add(Color.Red);
                colors.add(Color.Yellow);
                break;
            case 3:
                colors.add(Color.Red);
                colors.add(Color.Blue);
                colors.add(Color.Orange);
                break;
            case 4:
                colors.add(Color.Green);
                colors.add(Color.Black);
                colors.add(Color.Orange);
                colors.add(Color.Blue);
                break;
            case 6:
                colors.add(Color.Green);
                colors.add(Color.Black);
                colors.add(Color.Orange);
                colors.add(Color.Blue);
                colors.add(Color.Red);
                colors.add(Color.Yellow);
        }

        return colors;
    }

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
    @Override
    public void movePawn(BoardCoordinates oldPawnPosition, BoardCoordinates newPawnPosition, Player player)
            throws NullFieldException, WrongFieldStateException, WrongMoveException, WrongPawnColorException {
        Field newFieldForPawn = gameBoard.get(newPawnPosition.getRow()).get(newPawnPosition.getColumn());
        Field oldFieldForPawn = gameBoard.get(oldPawnPosition.getRow()).get(oldPawnPosition.getColumn());

        if (newFieldForPawn == null || oldFieldForPawn == null) {
            throw new NullFieldException();
        }
        else if (newFieldForPawn.isOccupied() || !oldFieldForPawn.isOccupied()) {
            throw new WrongFieldStateException();
        }
        else if (!oldFieldForPawn.getPawn().getColor().equals(player.getColor())) {
            throw new WrongPawnColorException();
        }
        else if (checkIfPawnInEnemyBaseCanMoveToNewField(oldPawnPosition, newPawnPosition)) {
            if (checkDistanceIsOneField(oldPawnPosition, newPawnPosition)) {
                movePawnToNewField(oldPawnPosition, newPawnPosition);
            }
            else if (checkMultiMoveIsCorrect(oldPawnPosition, newPawnPosition)) {
                movePawnToNewField(oldPawnPosition, newPawnPosition);
            }
            else {
                throw new WrongMoveException();
            }
        }
        else {
            throw new WrongMoveException();
        }
    }

    // Can be use when we know the move is correct
    private void movePawnToNewField(BoardCoordinates oldField, BoardCoordinates newField) {
        gameBoard.get(newField.getRow()).get(newField.getColumn())
                .setPawn(gameBoard.get(oldField.getRow()).get(oldField.getColumn()).getPawn());
        gameBoard.get(oldField.getRow()).get(oldField.getColumn()).setPawn(null);
    }

    // Checks whether we move the pawn by one field
    private boolean checkDistanceIsOneField(BoardCoordinates oldField, BoardCoordinates newField) {
        return (Math.abs(oldField.getRow() - newField.getRow()) + Math.abs(oldField.getColumn() - newField.getColumn())) == 1 ||
                ((oldField.getColumn() - newField.getColumn()) == 1 && (oldField.getRow() - newField.getRow()) == 1) ||
                ((oldField.getColumn() - newField.getColumn()) == -1 && (oldField.getRow() - newField.getRow()) == -1);
    }

    // Checks if the pawn is in enemy base
    private boolean checkIfPawnIsInEnemyBase(BoardCoordinates oldField) {
        Color oldFieldColor = gameBoard.get(oldField.getRow()).get(oldField.getColumn()).getColor(),
                pawnColor = gameBoard.get(oldField.getRow()).get(oldField.getColumn()).getPawn().getColor();

        if (oldFieldColor != null) {
            return oldFieldColor == getOppositeColor(pawnColor);
        }
        else {
            return false;
        }
    }

    // Checks that the movement can be executed if pawn is in enemy base (it's not possible to get out of enemy's base)
    private boolean checkIfPawnInEnemyBaseCanMoveToNewField(BoardCoordinates oldField, BoardCoordinates newField) {
        Color oldFieldColor = gameBoard.get(oldField.getRow()).get(oldField.getColumn()).getColor(),
                newFieldColor = gameBoard.get(newField.getRow()).get(newField.getColumn()).getColor();

        if(checkIfPawnIsInEnemyBase(oldField)) {
            return newFieldColor == oldFieldColor;
        }
        else {
            return true; // the pawn isn't in enemy's base
        }
    }

    /* The method finds possible ways for the pawn over other pawns,
        there is 6 ways to check:
                       xx
                       xPx
                        xx
        where P - pawn, x - fields where pawn can move to.
        There are two free spaces - top right corner and bottom left corner.
        These two spaces cannot be use, because gameBoard is imagination of real board in different way.
        This is recursive method;
     */
    public void checkPossibleWaysForPawn(BoardCoordinates coordinates) {
        int X = coordinates.getColumn();
        int Y = coordinates.getRow();

        if (gameBoard.get(Y - 1).get(X) != null && gameBoard.get(Y - 1).get(X).isOccupied() &&
                gameBoard.get(Y - 2).get(X) != null && !gameBoard.get(Y - 2).get(X).isOccupied() &&
                !checkIsInJumps(new BoardCoordinates(Y - 2, X))) {
            jumps.add(new BoardCoordinates(Y - 2, X));
            checkPossibleWaysForPawn(new BoardCoordinates(Y - 2, X));
        }

        if (gameBoard.get(Y).get(X + 1) != null && gameBoard.get(Y).get(X + 1).isOccupied() &&
                gameBoard.get(Y).get(X + 2) != null && !gameBoard.get(Y).get(X + 2).isOccupied() &&
                !checkIsInJumps(new BoardCoordinates(Y, X + 2))) {
            jumps.add(new BoardCoordinates(Y, X + 2));
            checkPossibleWaysForPawn(new BoardCoordinates(Y, X + 2));
        }

        if (gameBoard.get(Y + 1).get(X + 1) != null && gameBoard.get(Y + 1).get(X + 1).isOccupied() &&
                gameBoard.get(Y + 2).get(X + 2) != null && !gameBoard.get(Y + 2).get(X + 2).isOccupied() &&
                !checkIsInJumps(new BoardCoordinates(Y + 2, X + 2))) {
            jumps.add(new BoardCoordinates(Y + 2, X + 2));
            checkPossibleWaysForPawn(new BoardCoordinates(Y + 2, X + 2));
        }

        if (gameBoard.get(Y + 1).get(X) != null && gameBoard.get(Y + 1).get(X).isOccupied() &&
                gameBoard.get(Y + 2).get(X) != null && !gameBoard.get(Y + 2).get(X).isOccupied() &&
                !checkIsInJumps(new BoardCoordinates(Y + 2, X))) {
            jumps.add(new BoardCoordinates(Y + 2, X));
            checkPossibleWaysForPawn(new BoardCoordinates(Y + 2, X));
        }

        if (gameBoard.get(Y).get(X - 1) != null && gameBoard.get(Y).get(X - 1).isOccupied() &&
                gameBoard.get(Y).get(X - 2) != null && !gameBoard.get(Y).get(X - 2).isOccupied() &&
                !checkIsInJumps(new BoardCoordinates(Y, X - 2))) {
            jumps.add(new BoardCoordinates(Y, X - 2));
            checkPossibleWaysForPawn(new BoardCoordinates(Y, X - 2));
        }

        if (gameBoard.get(Y - 1).get(X - 1) != null && gameBoard.get(Y - 1).get(X - 1).isOccupied() &&
                gameBoard.get(Y - 2).get(X - 2) != null && !gameBoard.get(Y - 2).get(X - 2).isOccupied() &&
                !checkIsInJumps(new BoardCoordinates(Y - 2, X - 2))) {
            jumps.add(new BoardCoordinates(Y - 2, X - 2));
            checkPossibleWaysForPawn(new BoardCoordinates(Y - 2, X - 2));
        }

    }

    // Checks that jumps array doesn't contain particular coordinates
    private boolean checkIsInJumps(BoardCoordinates bc) {
        for(BoardCoordinates b : jumps) {
            if(b.getRow() == bc.getRow() && b.getColumn() == bc.getColumn()) {
                return true;
            }
        }
        return false;
    }

    // Checks that the move over other pawns is correct
    private boolean checkMultiMoveIsCorrect(BoardCoordinates oldField, BoardCoordinates newField) {
        jumps = new ArrayList<>();
        
        checkPossibleWaysForPawn(oldField.getBoardCoordinates());

        for (BoardCoordinates bc : jumps) {
            if ((newField.getRow() == bc.getRow() && newField.getColumn() == bc.getColumn())) {
                return true;
            } 
        }

        return false;
    }

    /**
     * Looks for the pawns which are located nearby the pawn that is chosen to be the closest to the enemy's base.
     * This ensures that the bot behaviour is more random, and thus less predictable for the player.
     * @param playerPawnsCoordinates
     * @param candidatesToMove
     * @param column
     * @param row
     */
    private ArrayList<BoardCoordinates> findNearbyPawns(ArrayList<BoardCoordinates> playerPawnsCoordinates, ArrayList<BoardCoordinates> candidatesToMove, int column, int row) {
        int radius = 1;

        do {
            for (BoardCoordinates coordinate : playerPawnsCoordinates) {
                if(Math.abs(coordinate.getColumn() - column) == radius && Math.abs(coordinate.getRow() - row) == radius) {
                    candidatesToMove.add(coordinate);
                    System.out.println(coordinate.getRow() + ", " + coordinate.getColumn());
                }
                else if(Math.abs(coordinate.getColumn() - column) == radius && Math.abs(coordinate.getRow() - row) == 0) {
                    candidatesToMove.add(coordinate);
                    System.out.println(coordinate.getRow() + ", " + coordinate.getColumn());
                }
                else if(Math.abs(coordinate.getRow() - row) == radius && Math.abs(coordinate.getColumn() - column) == 0) {
                    candidatesToMove.add(coordinate);
                    System.out.println(coordinate.getRow() + ", " + coordinate.getColumn());
                }
                
                /*if (coordinate.getRow() == (row - radius)) {
                    if(coordinate.getColumn() == column || coordinate.getColumn() == (column - radius) || coordinate.getColumn() == (column + radius)) {
                        candidatesToMove.add(coordinate);
                        continue;
                    }
                if(coordinate.getRow() == (row + radius)) {
                    if(coordinate.getColumn() == column || coordinate.getColumn() == (column - radius) || coordinate.getColumn() == (column + radius)) {
                        candidatesToMove.add(coordinate);
                        continue;
                    }
                if(coordinate.getRow() == row) {
                    if(coordinate.getColumn() == (column - radius) || coordinate.getColumn() == (column + radius)) {
                        candidatesToMove.add(coordinate);
                        continue;
                    }
                }*/
            }
            radius++;

        } while (candidatesToMove.size() < 4 && radius < 20);
        System.out.println("findnearbyByPawns" + candidatesToMove.size());

        return candidatesToMove;
    }

    /**
     * Checks if pawns which are the candidates for our move are not blocked by other pawns, meaning that they would be unable to move.
     * @param candidatesToMove
     */
    private Map<BoardCoordinates, ArrayList<BoardCoordinates>>
        getCandidatesWhichCanMoveInDesiredDirectionOnly(ArrayList<BoardCoordinates> candidatesToMove, Color playerColor)
        throws WrongPawnColorException {

            System.out.println("getCandidatesWhichCanMoveInDesiredDirectionOnly");

        // Bots with these colors will increase their row and/or column on move.
        // Hence, we'll check if the moves where these values change are correct.

        // Note that we assume here that the bot's pawn will move no more than once using the multi-move trick.
        // TODO: We don't want to be too ambitious or something here, and most games don't even allow multi-multi move.

        int single_dxdy[][], multi_dxdy[][];

        //ArrayList<BoardCoordinates> desiredCoorinatesToMoveInMultiMoveOnly = new ArrayList<BoardCoordinates>();
        //ArrayList<BoardCoordinates> desiredCoordinatesToMoveInSingleMoveOnly = new ArrayList<BoardCoordinates>();

        Map<BoardCoordinates, ArrayList<BoardCoordinates>> coordinatesWithPositionsToMoveTo = new HashMap<>();

        if (playerColor == Color.Red || playerColor == Color.Black) {
            single_dxdy = new int[][] {{0, 1}, {1, 0}, {1, 1}, {0, -1}};
            multi_dxdy = new int[][] {{0, 2}, {2, 0}, {2, 2}, {0, -2}};
        }
        else if (playerColor == Color.Yellow || playerColor == Color.Blue) {
            single_dxdy = new int[][] {{0, -1}, {-1, 0}, {-1, -1}, {0, 1}};
            multi_dxdy = new int[][] {{0, -2}, {-2, 0}, {-2, -2}, {0, 2}};
        }
        else if (playerColor == Color.Orange) {
            single_dxdy = new int[][] {{0, 1}, {-1, 0}, {-1, -1}, {1, 1}};
            multi_dxdy = new int[][] {{0, -2}, {-2, 0}, {-2, -2}, {2, 2}};
        }
        else if (playerColor == Color.Green) {
            single_dxdy = new int[][] {{0, -1}, {1, 0}, {1, 1}, {-1, -1}};
            multi_dxdy = new int[][] {{0, -2}, {2, 0}, {2, 2}, {-2, -2}};
        }
        else
            throw new WrongPawnColorException();

//        System.out.println("multi dx array:");
//        for (int [] delta: multi_dxdy) {
//            System.out.println("dx = " + delta[0] + ", dy = " + delta[1]);
//        }
//
//        int i = 0;
//
//        for (BoardCoordinates candidate: candidatesToMove) {
//            System.out.println(i + ") row: " + candidate.getRow() + ", column: " + candidate.getColumn());
//            i++;
//        }


        for (BoardCoordinates candidate: candidatesToMove) {

            ArrayList<BoardCoordinates> currentPawnWaysForMultiMove = new ArrayList<>();
            ArrayList<BoardCoordinates> currentPawnWaysForSingleMove = new ArrayList<>();

            for (int[] delta : single_dxdy) {
                // Let's check if the single move can be performed in a desired direction.
                int dx = delta[0], dy = delta[1];
                BoardCoordinates toCheck = new BoardCoordinates(candidate.getRow() + dx, candidate.getColumn() + dy);

                if (gameBoard.get(toCheck.getRow()).get(toCheck.getColumn()) != null && !gameBoard.get(toCheck.getRow()).get(toCheck.getColumn()).isOccupied()) {
                    // If the field we're checking is not occupied by anything, let's add it to our list.
                    currentPawnWaysForSingleMove.add(toCheck);
                }
            }

            // Add all possible single moves to the hashmap.
            if (currentPawnWaysForSingleMove.size() != 0)
                coordinatesWithPositionsToMoveTo.put(candidate, currentPawnWaysForSingleMove);

            for (int[] delta : multi_dxdy) {
                // Let's check if the multi move can be performed in the direction where it is desired.
                int dx = delta[0], dy = delta[1];
                BoardCoordinates toCheck = new BoardCoordinates(candidate.getRow() + dx, candidate.getColumn() + dy);

                if (checkMultiMoveIsCorrect(candidate, toCheck)) {
                    currentPawnWaysForMultiMove.add(toCheck);
                }
            }

            System.out.println("getCandidatesWhichCanMoveInDesiredDirectionOnly");

            // Add all possible multi moves to the hashmap.
            if (currentPawnWaysForMultiMove.size() != 0)
                coordinatesWithPositionsToMoveTo.put(candidate, currentPawnWaysForMultiMove);
        }
        return coordinatesWithPositionsToMoveTo;
    }

    private ArrayList<BoardCoordinates> getCoordinatesOfPawnsClosestToTheEnemyBaseByColor(Color playerColor) {
        System.out.println("getCoordinatesOfPawnsClosest....");

        ArrayList<BoardCoordinates> playerPawnsCoordinates = getCoordinatesOfAllPawnsOfColor(playerColor),
                candidatesToMove = new ArrayList<>();
        int column = 0, row = 0;

        if (playerColor == Color.Red || playerColor == Color.Black) {
            // Yellow/blue is the enemy we're concerned with, since the bot does not attack others

            // First, find the red/black pawn that's the closest to the yellow pawn

            for (BoardCoordinates coordinate: playerPawnsCoordinates) {
                int coordinateColumn = coordinate.getColumn(),
                        coordinateRow = coordinate.getRow();

                if (coordinateRow > row || coordinateColumn > column) {
                    // It will be random in choosing whether it will be the max from column or max from row,
                    // depending on what order the pawn will be chosen.
                    column = coordinateColumn;
                    row = coordinateRow;
                }
            }

            candidatesToMove.add(new BoardCoordinates(row, column));

            // Find some pawns nearby to the furthest-placed one
            candidatesToMove = findNearbyPawns(playerPawnsCoordinates, candidatesToMove, column, row);

            // Return the pawns found
            return candidatesToMove;
        }
        else if (playerColor == Color.Yellow || playerColor == Color.Blue) {
            // Red/black is the enemy we're concerned with.
            // Start by finding the yellow/blue pawn closest to the red base.

            // Get a first player pawn
            row = playerPawnsCoordinates.get(0).getRow();
            column = playerPawnsCoordinates.get(0).getColumn();

            // If a pawn is found with lesser coordinates, set it as a maximum

            for (BoardCoordinates coordinate: playerPawnsCoordinates) {
                int coordinateColumn = coordinate.getColumn(),
                        coordinateRow = coordinate.getRow();

                if (coordinateRow < row || coordinateColumn < column) {
                    row = coordinateRow;
                    column = coordinateColumn;
                }
            }

            //System.out.println("Max pawn row: " + row + ", column: " + column);
            candidatesToMove.add(new BoardCoordinates(row, column));

            // Find some pawns nearby to the furthest-placed one
            candidatesToMove = findNearbyPawns(playerPawnsCoordinates, candidatesToMove, column, row);

            // Return the pawns found
            return candidatesToMove;
        }
        else if (playerColor == Color.Orange) {
            // Green is the enemy we're concerned with
            // Firstly, let's find the orange pawn that's closest to the enemy's base

            row = playerPawnsCoordinates.get(0).getRow();
            column = playerPawnsCoordinates.get(0).getColumn();

            for (BoardCoordinates coordinate: playerPawnsCoordinates) {
                int coordinateColumn = coordinate.getColumn(),
                        coordinateRow = coordinate.getRow();

                if (coordinateColumn > column) {
                    row = coordinateRow;
                    column = coordinateColumn;
                }
            }

            //System.out.println("Max pawn row: " + row + ", column: " + column);
            candidatesToMove.add(new BoardCoordinates(row, column));

            // Find some pawns nearby to the furthest-placed one
            candidatesToMove = findNearbyPawns(playerPawnsCoordinates, candidatesToMove, column, row);

            // Return the pawns found
            return candidatesToMove;
        }
        else if (playerColor == Color.Green) {
            // Orange is the enemy we're concerned with
            // Firstly, let's find the green pawn that's closest to the enemy's base

            row = playerPawnsCoordinates.get(0).getRow();
            column = playerPawnsCoordinates.get(0).getColumn();

            for (BoardCoordinates coordinate: playerPawnsCoordinates) {
                int coordinateColumn = coordinate.getColumn(),
                        coordinateRow = coordinate.getRow();

                if (coordinateColumn < column) {
                    row = coordinateRow;
                    column = coordinateColumn;
                }
            }

            //System.out.println("Max pawn row: " + row + ", column: " + column);
            candidatesToMove.add(new BoardCoordinates(row, column));

            // Find some pawns nearby to the furthest-placed one
            candidatesToMove = findNearbyPawns(playerPawnsCoordinates, candidatesToMove, column, row);

            System.out.println("getCoordinatesOfPawnsClosest....");
            // Return the pawns found
            return candidatesToMove;
        }
        else {
            System.out.println("getCoordinatesOfPawnsClosest....");
            return null;
        }
    }

    /**
     * Performs the movement of the randomly chosen pawn owned by the player which is handled by the bot.
     * 
     * @param bot player that is a bot
     * @return array of coordinates - (0) old field, (1) new field
     */
    public ArrayList<BoardCoordinates> performBotMove(Player bot) {
        ArrayList<BoardCoordinates> move = new ArrayList<>();

        try {
            // Color of the bot
            Color botColor = bot.getColor();

            // Get the list of pawns we'll be chosing from.
            ArrayList<BoardCoordinates> pawnsToChooseFrom = getCoordinatesOfPawnsClosestToTheEnemyBaseByColor(botColor);
            //System.out.println("Pawns we're chosing from:\n" + pawnsToChooseFrom);

            // Get the hashmap of pawns associated with the best possible directions to move to for a given color.
            Map<BoardCoordinates, ArrayList<BoardCoordinates>> mapOfPawnsToMove = getCandidatesWhichCanMoveInDesiredDirectionOnly(pawnsToChooseFrom, botColor);

            //System.out.println("Current hashmap:");
            //System.out.println(mapOfPawnsToMove);

            // Randomly chose the pawn that will be moved from the generated hashmap.
            Random generator = new Random();
            List<BoardCoordinates> pawns = new ArrayList<BoardCoordinates>(mapOfPawnsToMove.keySet());
            BoardCoordinates pawnChosen = pawns.get(generator.nextInt(pawns.size()));

            move.add(0, pawnChosen);

            System.out.println("Chosen pawn row: " + pawnChosen.getRow() + ", column: " + pawnChosen.getColumn());

            // Get the coordinates to which it's possible to move the chosen pawn.
            ArrayList<BoardCoordinates> possibileCoordinatesToMoveChosenPawn = mapOfPawnsToMove.get(pawnChosen);

            //System.out.println("Coordinates possible to move:\n" + possibileCoordinatesToMoveChosenPawn);

            // Now, yet again randomly choose where shall we move it!
            BoardCoordinates newChosenPawnPosition = possibileCoordinatesToMoveChosenPawn.get(generator.nextInt(possibileCoordinatesToMoveChosenPawn.size()));

            move.add(1, newChosenPawnPosition);

            // Finally(!) move the pawn there.
            //movePawnToNewField(pawnChosen, newChosenPawnPosition); faaaaaaaaaaaaaaaak ile czasu stracilem na to 

            System.out.println("New pawn position row: " + newChosenPawnPosition.getRow() + ", column: " + newChosenPawnPosition.getColumn());

            // Jupijajej maderfaker.

        }
        catch (WrongPawnColorException ex) {
            System.out.printf("Provided color: " + bot.getColor().toString() + "is wrong.");
            return null;
        }

        return move;
    }

    private ArrayList<BoardCoordinates> getCoordinatesOfAllPawnsOfColor(Color playerColor) {

        ArrayList<BoardCoordinates> allPawns = getPawnsCoordinates();
        ArrayList<BoardCoordinates> pawnsOfChosenColor = new ArrayList<>();

        for (BoardCoordinates bc: allPawns) {
            Pawn pawn = gameBoard.get(bc.getRow()).get(bc.getColumn()).getPawn();

            if (pawn.getColor() == playerColor)
                pawnsOfChosenColor.add(bc);
        }

        return pawnsOfChosenColor;
    }

    /**
     * The method returns current coordinates of all pawns.
     *
     * @return array of coordinates of pawns
     */
    @Override
    public ArrayList<BoardCoordinates> getPawnsCoordinates() {
        ArrayList<BoardCoordinates> pawnesCoordinates = new ArrayList<>();
        Field field;

        for (int i = 0; i < 19; i++) {
            for (int j = 0; j < 19; j++) {
                field = gameBoard.get(i).get(j);
                if (field != null && field.isOccupied()) {
                    pawnesCoordinates.add(new BoardCoordinates(i, j));
                }
            }
        }

        return pawnesCoordinates;
    }

    /**
     * The method checks if the player has all pawns in the opposite base.
     *
     * @param player the player whose result we want to check
     * @return true if all pawns of player are in the opposite base
     */
    @Override
    public boolean checkIfThePlayerIsAWinner(Player player) {
        Color colorOfPlayer = player.getColor();
        Field field;
        int baseSize = 10;
        int numberOfPawnsInBase = 0;

        for (int i = 0; i < 19; i++) {
            for (int j = 0; j < 19; j++) {
                field = gameBoard.get(i).get(j);
                if (field != null && field.getColor().equals(getOppositeColor(colorOfPlayer))
                        && field.isOccupied() && field.getPawn().getColor().equals(colorOfPlayer)) {
                    numberOfPawnsInBase++;
                }
            }
        }

        return numberOfPawnsInBase == baseSize;
    }

    // Returns color of opposite base (pawns)
    private Color getOppositeColor(Color col) {
        if(col.equals(Color.Red)) {
            return Color.Yellow;
        } else if(col.equals(Color.Yellow)) {
            return Color.Red;
        } else if(col.equals(Color.Blue)) {
            return Color.Black;
        } else if(col.equals(Color.Black)) {
            return Color.Blue;
        } else if(col.equals(Color.Green)) {
            return Color.Orange;
        } else if(col.equals(Color.Orange)) {
            return Color.Green;
        } else {
            return null;
        }
    }
}