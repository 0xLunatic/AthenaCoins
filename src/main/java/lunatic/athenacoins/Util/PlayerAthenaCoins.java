package lunatic.athenacoins.Util;

import lunatic.athenacoins.AthenaCoins;

import java.util.UUID;

public class PlayerAthenaCoins {
    private UUID uuid;
    private String playerName;
    private int balance = 0;
    private final AthenaCoins plugin;

    public PlayerAthenaCoins(AthenaCoins plugin, UUID uuid) {
        this.plugin = plugin;
        this.uuid = uuid;
    }

    public void destroy() {
        this.plugin.getDatabase().invalidateCache(uuid);
    }

    public void withdraw(double amount) {
        this.balance -= amount;
    }

    public void deposit(double amount) {
        this.balance += amount;
    }

    public void setBalance(int amount) {
        this.balance = amount;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public int getBalance() {
        return this.balance;
    }
}
