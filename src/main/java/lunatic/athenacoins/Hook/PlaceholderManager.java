package lunatic.athenacoins.Hook;

import com.google.common.base.Joiner;
import lunatic.athenacoins.AthenaCoins;
import lunatic.athenacoins.Database.Database;
import lunatic.athenacoins.Util.PlayerAthenaCoins;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.UUID;

public class PlaceholderManager extends PlaceholderExpansion {
    private final AthenaCoins plugin;
    private final Database database;
    public PlaceholderManager(AthenaCoins plugin, Database database) {
        this.plugin = plugin;
        this.database = database;
    }

    @Override
    public @NotNull String getIdentifier() {
        return this.plugin.getDescription().getName();
    }

    @Override
    public @NotNull String getAuthor() {
        return Joiner.on(", ").join(this.plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {

        if (params.equalsIgnoreCase("coins")) {
            UUID playerUUID = player.getUniqueId();
            int coins = database.getBalance(playerUUID, player.getName(), false);
            return String.valueOf(coins);
        }
        return null;
    }
}
