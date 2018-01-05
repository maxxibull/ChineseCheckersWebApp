package logic;

/**
 * The class includes information about players (and bots).
 */
public class Player {
    private final Color color;
    private final String username;
    private final boolean isBot; // true - bot, false - human 

    /**
     * The main constructor of Player class, sets variables.
     * 
     * @param playerColor color of player
     * @param username name of player
     * @param isBot true - bot, false - human
     */
    public Player(Color playerColor, String username, boolean isBot) {
        this.color = playerColor;
        this.username = username;
        this.isBot = isBot;
    }

    /**
     * Returns color of the player
     * 
     * @return color of the player
     */
    public Color getColor() {
        return color;
    }

    /**
     * Returns name of the player
     * 
     * @return name of the player
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns info whether player is human or bot
     * 
     * @return true - bot, false - human
     */
    public boolean checkPlayerIsBot() {
        return isBot;
    }

}