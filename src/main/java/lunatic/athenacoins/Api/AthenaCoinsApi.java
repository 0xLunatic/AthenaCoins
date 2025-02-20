package lunatic.athenacoins.Api;

import lunatic.athenacoins.Exceptions.DataNotLoadedException;
import lunatic.athenacoins.Util.PlayerAthenaCoins;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AthenaCoinsApi {

    default CompletableFuture<PlayerAthenaCoins> loadData(UUID uuid, String playerName, boolean ignoredJoin) {
        return this.getAthenaCoinsByUUIDAsync(uuid, playerName);
    }

    CompletableFuture<PlayerAthenaCoins> getAthenaCoinsByUUIDAsync(UUID uuid, String playerName);

    PlayerAthenaCoins getLoadedData(UUID uuid) throws DataNotLoadedException;

    boolean withdraw(UUID uuid, String playerName, double amount, String executor);

    boolean deposit(UUID uuid, String playerName, double amount, String executor);

    boolean set(UUID uuid, String playerName, double amount, String executor);

    int getBalance(UUID uuid, String playerName, boolean wait);

    int getBalance(UUID uuid, String playerName);

    CompletableFuture<Integer> getBalanceFuture(UUID uuid, String playerName);
}
