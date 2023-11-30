package lunatic.athenacoins.papi;

import com.google.common.base.Joiner;
import lunatic.athenacoins.Main;
import lunatic.athenacoins.databases.Database;
import lunatic.athenacoins.utils.PlayerAthenaCoins;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.UUID;

public class PlaceholderManager extends PlaceholderExpansion {
    private final Main plugin;
    private final Database database;
    public PlaceholderManager(Main plugin, Database database) {
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
            PlayerAthenaCoins coins = null;
            try {
                coins = database.getAthenaCoinsByUUID(playerUUID);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return String.valueOf(coins);
        }
        return null;
    }
}
