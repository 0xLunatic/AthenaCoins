package lunatic.athenacoins.commands;

import lunatic.athenacoins.Main;
import lunatic.athenacoins.databases.Database;
import lunatic.athenacoins.utils.PlayerAthenaCoins;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.sql.SQLException;
import java.util.UUID;

public class AthenaCommands implements CommandExecutor, Listener {
    private final Main plugin;
    private final Database database;

    public AthenaCommands(Main plugin, Database database) {
        this.plugin = plugin;
        this.database = database;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length <= 1 || (args[0].equalsIgnoreCase("add") && args.length < 3) ||
                (args[0].equalsIgnoreCase("remove") && args.length < 3)) {
            sender.sendMessage("§cUsage: /athenacoins <add|remove|check|clear> [player] [value]");
            return true;
        }

        if (args[0].equalsIgnoreCase("check")) {
            if (args.length < 2) {
                sender.sendMessage("§cUsage: /athenacoins check <player>");
                return true;
            }

            String playerName = args[1];
            Player targetPlayer = plugin.getServer().getPlayer(playerName);

            if (targetPlayer == null) {
                sender.sendMessage("§cPlayer not found: " + playerName);
                return true;
            }

            UUID playerUUID = targetPlayer.getUniqueId();

            try {
                PlayerAthenaCoins athenaCoins = database.getAthenaCoinsByUUID(playerUUID);

                if (athenaCoins == null) {
                    sender.sendMessage(targetPlayer.getName() + " has 0 Athena Coins.");
                } else {
                    sender.sendMessage(targetPlayer.getName() + " has " + athenaCoins.getAthenaCoins() + " Athena Coins.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                sender.sendMessage("§cAn error occurred while checking Athena Coins.");
            }

            return true;
        } else if (args[0].equalsIgnoreCase("clear")) {
            if (args.length < 2) {
                sender.sendMessage("§cUsage: /athenacoins clear <player>");
                return true;
            }

            String playerName = args[1];
            Player targetPlayer = plugin.getServer().getPlayer(playerName);

            if (targetPlayer == null) {
                sender.sendMessage("§cPlayer not found: " + playerName);
                return true;
            }

            UUID playerUUID = targetPlayer.getUniqueId();

            try {
                PlayerAthenaCoins athenaCoins = database.getAthenaCoinsByUUID(playerUUID);

                if (athenaCoins != null) {
                    database.clearAthenaCoins(athenaCoins);
                    sender.sendMessage("§aCleared Athena Coins for " + targetPlayer.getName() + ".");
                } else {
                    sender.sendMessage("§a" +targetPlayer.getName() + " has 0 Athena Coins.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                sender.sendMessage("§cAn error occurred while clearing Athena Coins.");
            }

            return true;
        } else {
            String playerName = args[1];
            Player targetPlayer = plugin.getServer().getPlayer(playerName);

            if (targetPlayer == null) {
                sender.sendMessage("§cPlayer not found: " + playerName);
                return true;
            }

            int value;

            try {
                value = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid value. Please provide a valid number.");
                return true;
            }

            UUID playerUUID = targetPlayer.getUniqueId();

            try {
                PlayerAthenaCoins athenaCoins = database.getAthenaCoinsByUUID(playerUUID);

                if (athenaCoins == null) {
                    // Player not in the database, create a new entry
                    athenaCoins = new PlayerAthenaCoins(playerUUID, playerName, 0);
                    database.createAthenaCoins(athenaCoins);
                }

                // Check if it's a remove command
                if (args[0].equalsIgnoreCase("remove")) {
                    // Check if there are enough coins to remove
                    if (athenaCoins.getAthenaCoins() < value) {
                        sender.sendMessage("§cNot enough Athena Coins to remove.");
                        return true;
                    }

                    // Remove coins from the player
                    database.addAthenaCoins(athenaCoins, -value);

                    sender.sendMessage("§aRemoved " + value + " Athena Coins from " + targetPlayer.getName() + ".");
                } else {
                    // Add coins to the player
                    database.addAthenaCoins(athenaCoins, value);

                    sender.sendMessage("§aAdded " + value + " Athena Coins to " + targetPlayer.getName() + ".");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                sender.sendMessage("§cAn error occurred while updating Athena Coins.");
            }

            return true;
        }
    }
}

