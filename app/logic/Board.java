package logic;

import logic.BoardCoordinates;
import logic.exceptions.*;
import java.util.*;
import java.util.ArrayList;

/**
 * Creates the board for Chinese Checkers game, manages it, checks correctness of movements and performs movements for bots.
 */
public class Board implements BoardInterface {
    private int numberOfPlayers;                        // number of players - with bots
    private ArrayList<ArrayList<Field>> gameBoard;      // array for GameField objects
    private ArrayList<Color> colorsOfPlayersOnBoard;    // colors of pawns
    private ArrayList<BoardCoordinates> jumps;          // array for checkPossibleWaysForPawn and checkMultiMoveIsCorrect methods

    /**
     * The class has only one constructor which creates the gameBoard.
     * The board consists of fields (GameField class) upon which the pawns (Pawn class) are placed.
     * The constructor also assigns the colors to the players in the game
     * 
     * @param numberOfPlayers number of players (including bot players)
     * @throws IllegalArgumentException which is thrown when a number of players is different than 2, 3, 4 or 6
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

    // Creates an array of fields (of GameField type), which form our board.
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

    // Creates new pawns and places them to the base fields
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

    // Assigns colors to the players
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
     * Moves the pawn from its' current position to a new field.
     *
     * @param oldPawnPosition current position of the pawn on the board
     * @param newPawnPosition new position of the pawn on the board
     * @param player          the player who wants to move the chosen pawn
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

    // Moves the pawn from its current position to the new field
    // Can be used only if we're sure of the validity of the movement
    private void movePawnToNewField(BoardCoordinates oldField, BoardCoordinates newField) {
        gameBoard.get(newField.getRow()).get(newField.getColumn())
                .setPawn(gameBoard.get(oldField.getRow()).get(oldField.getColumn()).getPawn());
        gameBoard.get(oldField.getRow()).get(oldField.getColumn()).setPawn(null);
    }

    // Checks if the distance between the fields is equal to one
    // Used to check if the movement will be performed by one field only
    private boolean checkDistanceIsOneField(BoardCoordinates oldField, BoardCoordinates newField) {
        return (Math.abs(oldField.getRow() - newField.getRow()) + Math.abs(oldField.getColumn() - newField.getColumn())) == 1 ||
                ((oldField.getColumn() - newField.getColumn()) == 1 && (oldField.getRow() - newField.getRow()) == 1) ||
                ((oldField.getColumn() - newField.getColumn()) == -1 && (oldField.getRow() - newField.getRow()) == -1);
    }

    // Checks if the pawn is in the enemy's base
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

    // Checks if the movement can be performed when pawn is in the enemy's base
    // It's not possible for the pawn to step out of the enemy's base once it's in it
    private boolean checkIfPawnInEnemyBaseCanMoveToNewField(BoardCoordinates oldField, BoardCoordinates newField) {
        Color oldFieldColor = gameBoard.get(oldField.getRow()).get(oldField.getColumn()).getColor(),
                newFieldColor = gameBoard.get(newField.getRow()).get(newField.getColumn()).getColor();

        if(checkIfPawnIsInEnemyBase(oldField)) {
            return newFieldColor == oldFieldColor;
        }
        else {
            return true; // the pawn is not in the enemy's base
        }
    }

    /**
     * Calculates all of the possibilities for the movement of a pawn with a given coordinates and stores
     * them in jumps ArrayList
     * @param coordinates the coordinates of a pawn
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

    // Checks if the jumps array does not contain the provided coordinate for movement
    private boolean checkIsInJumps(BoardCoordinates bc) {
        for(BoardCoordinates b : jumps) {
            if(b.getRow() == bc.getRow() && b.getColumn() == bc.getColumn()) {
                return true;
            }
        }
        return false;
    }

    // Checks whether the movement over other pawns is correct
    private boolean checkMultiMoveIsCorrect(BoardCoordinates oldField, BoardCoordinates newField) {
        jumps = new ArrayList<>();
        
        checkPossibleWaysForPawn(oldField);

        for (BoardCoordinates bc : jumps) {
            if ((newField.getRow() == bc.getRow() && newField.getColumn() == bc.getColumn())) {
                return true;
            } 
        }

        return false;
    }

    /**
     * Checks if pawns which are the candidates for our move are not blocked by other pawns, meaning that they would be unable to move.
     * 
     * @param candidatesToMove
     */
    private Map<BoardCoordinates, ArrayList<BoardCoordinates>> getCandidatesWhichCanMoveInDesiredDirectionOnly(ArrayList<BoardCoordinates> candidatesToMove, Color playerColor)
        throws WrongPawnColorException {

        // Bots with these colors will increase their row and/or column on move.
        // Hence, we'll check if the moves where these values change are correct.
        // Note that we assume here that the bot's pawn will move no more than once using the multi-move trick.

        int single_dxdy[][], multi_dxdy[][];

        Map<BoardCoordinates, ArrayList<BoardCoordinates>> coordinatesWithPositionsToMoveTo = new HashMap<>();

        if (playerColor == Color.Red) {
            single_dxdy = new int[][] {{0, 1}, {1, 0}, {1, 1}, {0, -1}};
            multi_dxdy = new int[][] {{0, 2}, {2, 0}, {2, 2}, {0, -2}};
        }
        else if (playerColor == Color.Black) {
            single_dxdy = new int[][] {{0, 1}, {1, 0}, {1, 1}, {-1, 0}};
            multi_dxdy = new int[][] {{0, 2}, {2, 0}, {2, 2}, {-2, 0}};
        }
        else if (playerColor == Color.Yellow) {
            single_dxdy = new int[][] {{0, -1}, {-1, 0}, {-1, -1}, {0, 1}};
            multi_dxdy = new int[][] {{0, -2}, {-2, 0}, {-2, -2}, {0, 2}};
        }
        else if (playerColor == Color.Blue) {
            single_dxdy = new int[][] {{0, -1}, {-1, 0}, {-1, -1}, {1, 1}};
            multi_dxdy = new int[][] {{0, -2}, {-2, 0}, {-2, -2}, {2, 2}};
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

            // Add all possible multi moves to the hashmap.
            if (currentPawnWaysForMultiMove.size() != 0)
                coordinatesWithPositionsToMoveTo.put(candidate, currentPawnWaysForMultiMove);
        }
        return coordinatesWithPositionsToMoveTo;
    }

    /**
     * Performs the movement of the randomly chosen pawn owned by the player which is handled by the bot.
     * 
     * @param bot player that is a bot
     * @throws WrongMoveException thrown if move is incorrect
     * @return array of coordinates - (0) old field, (1) new field
     */
    public ArrayList<BoardCoordinates> performBotMove(Player bot) throws WrongMoveException {
        ArrayList<BoardCoordinates> move = new ArrayList<>();

        try {
            Color botColor = bot.getColor();

            // Get the list of pawns we'll be chosing from.
            ArrayList<BoardCoordinates> pawnsToChooseFrom = getCoordinatesOfAllPawnsOfColor(botColor);

            // Get the hashmap of pawns associated with the best possible directions to move to for a given color.
            Map<BoardCoordinates, ArrayList<BoardCoordinates>> mapOfPawnsToMove = getCandidatesWhichCanMoveInDesiredDirectionOnly(pawnsToChooseFrom, botColor);

            // Randomly chose the pawn that will be moved from the generated hashmap.
            Random generator = new Random();
            List<BoardCoordinates> pawns = new ArrayList<BoardCoordinates>(mapOfPawnsToMove.keySet());
            
            if(pawns.size() == 0) {
                throw new WrongMoveException();
            }

            BoardCoordinates pawnChosen = pawns.get(generator.nextInt(pawns.size()));

            move.add(0, pawnChosen);

            // Get the coordinates to which it's possible to move the chosen pawn.
            ArrayList<BoardCoordinates> possibileCoordinatesToMoveChosenPawn = mapOfPawnsToMove.get(pawnChosen);

            // Now, yet again randomly choose where shall we move it!
            BoardCoordinates newChosenPawnPosition = possibileCoordinatesToMoveChosenPawn.get(generator.nextInt(possibileCoordinatesToMoveChosenPawn.size()));

            move.add(1, newChosenPawnPosition);
        }
        catch (WrongPawnColorException ex) {
            System.out.printf("Provided color: " + bot.getColor().toString() + "is wrong.");
            return null;
        }

        return move;
    }

    // Returns the coordinates of every single pawn of the chosen color
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

    // Returns a color of the opposite base (player) for the chosen color
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