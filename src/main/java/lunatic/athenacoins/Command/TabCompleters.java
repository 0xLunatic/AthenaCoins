package lunatic.athenacoins.Command;

import lunatic.athenacoins.AthenaCoins;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TabCompleters implements TabCompleter {
    private final AthenaCoins plugin;
    public TabCompleters(AthenaCoins plugin) {
        this.plugin = plugin;
    }
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("add");
            completions.add("remove");
            completions.add("check");
            completions.add("clear");
        } else if (args.length == 2) {
            try {
                String[] players = plugin.getDatabase().getPlayers().toArray(new String[0]);
                Collections.addAll(completions, players);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return completions;
    }
}
