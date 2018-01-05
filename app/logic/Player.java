package logic;

public class Player {

    private final Color color;
    private final String username;
    private final boolean isBot;

    public Player(Color playerColor, String username, boolean isBot) {
        this.color = playerColor;
        this.username = username;
        this.isBot = isBot;
    }

    public Color getColor() {
        return color;
    }

    public String getUsername() {
        return username;
    }

    public boolean checkPlayerIsBot() {
        return isBot;
    }

}