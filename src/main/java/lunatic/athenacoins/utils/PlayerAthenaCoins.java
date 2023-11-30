package lunatic.athenacoins.utils;

import java.util.UUID;

public class PlayerAthenaCoins {
    private UUID playerUUID;
    private String playerName;
    private int athenaCoins;

    public PlayerAthenaCoins(UUID playerUUID, String playerName, int athenaCoins) {
        this.playerUUID = playerUUID;
        this.athenaCoins = athenaCoins;
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public int getAthenaCoins() {
        return athenaCoins;
    }

    public void setAthenaCoins(int athenaCoins) {
        this.athenaCoins = athenaCoins;
    }
}
