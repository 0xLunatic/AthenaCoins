package lunatic.athenacoins.Listener;

import lunatic.athenacoins.AthenaCoins;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;
import java.util.UUID;

public class PlayerData implements Listener {

    private final AthenaCoins plugin;

    public PlayerData(AthenaCoins plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();

        if (event.getLoginResult().equals(AsyncPlayerPreLoginEvent.Result.ALLOWED)) {
            try {
                plugin.getDatabase().loadData(uuid);
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
    }
}
