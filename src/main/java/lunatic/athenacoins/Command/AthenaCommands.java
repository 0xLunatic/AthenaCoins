package lunatic.athenacoins.Command;

import lunatic.athenacoins.AthenaCoins;
import lunatic.athenacoins.Database.Database;
import me.kenvera.chronocord.ChronoCord;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;


public class AthenaCommands implements CommandExecutor, Listener {
    private final AthenaCoins plugin;
    private final Database database;
    private final String[] whitelist = {"Kenvera", "iKagu", "Evynx", "Keiins", "Mornov"};

    public AthenaCommands(AthenaCoins plugin, Database database) {
        this.plugin = plugin;
        this.database = database;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /athenacoins <add|remove|check|clear> <player> <value>");
            return true;
        }

        if (sender instanceof Player) {
            if (!Arrays.asList(whitelist).contains(sender.getName())) {
                return true;
            }
        }

        String subcommand = args[0].toLowerCase();
        String playerName;
        Player player;
        UUID uuid;
        int previousToken;
        int currentToken;
        Long channel = 1192494512736575579L;

        switch (subcommand) {
            case "check":
                if (args.length < 2) {
                    sender.sendMessage("§7Usage: /athenacoins check <player>");
                    break;
                }

                try {
                    playerName = args[1];
                    uuid = database.getPlayerUUID(playerName);
                    if (uuid != null) {
                        double amount = plugin.getDatabase().getBalance(uuid, playerName, true);
                        sender.sendMessage("§b" + playerName + "§a has §6" + amount + " §aAthena Coins.");
                    } else {
                        sender.sendMessage("§cPlayer data cannot be found: §b" + playerName);
                    }
                } catch (SQLException e) {
                    e.printStackTrace(System.out);
                    sender.sendMessage("§cAn error occurred while checking Athena Coins.");
                }



//                playerName = args[1];
//                player = plugin.getServer().getPlayer(playerName);
//
//                if (player != null) {
//                    uuid = player.getUniqueId();
//                    try {
//                        PlayerAthenaCoins athenaCoins = database.getAthenaCoinsByUUID(uuid);
//
//                        if (athenaCoins == null) {
//                            sender.sendMessage("§b" + player.getName() + "§a has §60 §aAthena Coins.");
//                        } else {
//                            sender.sendMessage("§b" + player.getName() + "§a has §6" + athenaCoins.getAthenaCoins() + " §aAthena Coins.");
//                        }
//                    } catch (SQLException e) {
//                        e.printStackTrace(System.out);
//                        sender.sendMessage("§cAn error occurred while getting player's coin!");
//                    }
//
//                } else {
//                    try {
//                        uuid = database.getPlayerUUID(playerName);
//                        if (uuid != null ) {
//                            playerName = database.getPlayer(uuid.toString());
//                            PlayerAthenaCoins athenaCoins = database.getAthenaCoinsByUUID(uuid);
//
//                            if (athenaCoins.getPlayerName() == null) {
//                                sender.sendMessage("§b" + playerName + "§a has §60 §aAthena Coins.");
//                            } else {
//                                sender.sendMessage("§b" + playerName + "§a has §6" + athenaCoins.getAthenaCoins() + " §aAthena Coins.");
//                            }
//                        } else {
//                            sender.sendMessage("§cPlayer data cannot be found: §b" + playerName);
//                        }
//                    } catch (SQLException e) {
//                        e.printStackTrace(System.out);
//                        sender.sendMessage("§cAn error occurred while checking Athena Coins.");
//                    }
//                }
                break;

//            case "clear":
//                if (args.length < 2) {
//                    sender.sendMessage("§cUsage: /athenacoins clear <player>");
//                    break;
//                }
//
//                playerName = args[1];
//                player = plugin.getServer().getPlayer(playerName);
//
//                if (player != null) {
//                    uuid = player.getUniqueId();
//                    try {
//                        PlayerAthenaCoins athenaCoins = database.getAthenaCoinsByUUID(uuid);
//
//                        if (athenaCoins == null) {
//                            sender.sendMessage("§b" + player.getName() + "§a has §60 §aAthena Coins.");
//                        } else {
//                            previousToken = athenaCoins.getAthenaCoins();
//                            database.clearAthenaCoins(athenaCoins);
//                            sender.sendMessage("§aSuccesfully purged coins of §b" + player.getName() + "§a!");
//                            ChronoCord.getInstance().getChronoLogger().logToken(playerName, uuid.toString(), String.valueOf(previousToken), "0", "Token Clear", "-", sender.getName(), channel);
//                        }
//                    } catch (SQLException e) {
//                        e.printStackTrace(System.out);
//                        sender.sendMessage("§cAn error occurred while getting player's coin!");
//                    }
//
//                } else {
//                    try {
//                        uuid = database.getPlayerUUID(playerName);
//                        playerName = database.getPlayer(uuid.toString());
//                        if (uuid != null) {
//                            PlayerAthenaCoins athenaCoins = database.getAthenaCoinsByUUID(uuid);
//
//                            if (athenaCoins == null) {
//                                sender.sendMessage("§b" + playerName + "§a has §60 §aAthena Coins.");
//                            } else {
//                                database.clearAthenaCoins(athenaCoins);
//                                sender.sendMessage("§aSuccesfully purged coins of §b" + playerName + "§a!");
//                            }
//                        } else {
//                            sender.sendMessage("§cPlayer data cannot be found: " + playerName);
//                        }
//                    } catch (SQLException e) {
//                        e.printStackTrace(System.out);
//                        sender.sendMessage("§cAn error occurred while checking Athena Coins.");
//                    }
//                }
//                break;
//
            case "add":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /athenacoins add <player> <value>");
                    break;
                }

                playerName = args[1];
                player = Bukkit.getPlayer(playerName);

                if (player != null) {
                    int value = Integer.parseInt(args[2]);
                    uuid = player.getUniqueId();
                    previousToken = plugin.getDatabase().getBalance(uuid, playerName, true);
                    currentToken = previousToken + value;
                    plugin.getDatabase().deposit(uuid, playerName, value, sender.getName());

                    sender.sendMessage("§aSuccesfully added §6" + value + " §acoins to " + playerName + "§a!");
                    ChronoCord.getInstance().getChronoLogger().logToken(playerName, uuid.toString(), "0", String.valueOf(currentToken), "Token Add", String.valueOf(value), sender.getName(), channel);
                } else {
                    try {
                        if (plugin.getDatabase().isExist(playerName)) {
                            int value = Integer.parseInt(args[2]);
                            uuid = plugin.getDatabase().getPlayerUUID(playerName);
                            previousToken = plugin.getDatabase().getBalance(uuid, playerName, true);
                            currentToken = previousToken + value;
                            plugin.getDatabase().deposit(uuid, playerName, value, sender.getName());

                            sender.sendMessage("§aSuccesfully added §6" + value + " §acoins to " + playerName + "§a!");
                            ChronoCord.getInstance().getChronoLogger().logToken(playerName, uuid.toString(), "0", String.valueOf(currentToken), "Token Add", String.valueOf(value), sender.getName(), channel);
                        } else {
                            sender.sendMessage("§cPlayer §b" + playerName + " §cisn't available within database!");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace(System.out);
                    }
                }

//                    try {
//                        int value = Integer.parseInt(args[2]);
//                        uuid = player.getUniqueId();
//                        playerName = player.getName();
//                        PlayerAthenaCoins athenaCoins = database.getAthenaCoinsByUUID(uuid);
//
//                        if (athenaCoins.getPlayerName() == null) {
//                            previousToken = athenaCoins.getAthenaCoins();
//                            athenaCoins = new PlayerAthenaCoins(uuid, playerName, value);
//                            database.createAthenaCoins(athenaCoins);
//                            sender.sendMessage("§aSuccesfully added §6" + value + " §acoins to " + playerName + "§a!");
//                            currentToken = previousToken + value;
//                            ChronoCord.getInstance().getChronoLogger().logToken(playerName, uuid.toString(), "0", String.valueOf(currentToken), "Token Add", String.valueOf(value), sender.getName(), channel);
//                        } else {
//                            previousToken = athenaCoins.getAthenaCoins();
//                            database.addAthenaCoins(athenaCoins, value);
//                            sender.sendMessage("§aSuccesfully added §6" + value + " §acoins to " + playerName + "§a!");
//                            currentToken = previousToken + value;
//                            ChronoCord.getInstance().getChronoLogger().logToken(playerName, uuid.toString(), String.valueOf(previousToken), String.valueOf(currentToken), "Token Add", String.valueOf(value), sender.getName(), channel);
//                        }
//                    } catch (NumberFormatException e) {
//                        e.printStackTrace(System.out);
//                        sender.sendMessage("§cInvalid value of coin, Please provide a valid number.");
//                    } catch (SQLException e) {
//                        e.printStackTrace(System.out);
//                        sender.sendMessage("§cAn error occurred while updating Athena Coins.");
//                    }


//                } else {
//                    try {
////                        String uuid =
//                        int value = Integer.parseInt(args[2]);
//                        playerName = player.getName();
//                        PlayerAthenaCoins athenaCoins = database.getAthenaCoinsByUUID(uuid);
//
//                        if (athenaCoins.getPlayerName() == null) {
//                            previousToken = athenaCoins.getAthenaCoins();
//                            athenaCoins = new PlayerAthenaCoins(uuid, playerName, value);
//                            database.createAthenaCoins(athenaCoins);
//                            sender.sendMessage("§aSuccesfully added §6" + value + " §acoins to " + playerName + "§a!");
//                            currentToken = previousToken + value;
//                            ChronoCord.getInstance().getChronoLogger().logToken(playerName, uuid.toString(), "0", String.valueOf(currentToken), "Token Add", String.valueOf(value), sender.getName(), channel);
//                        } else {
//                            previousToken = athenaCoins.getAthenaCoins();
//                            database.addAthenaCoins(athenaCoins, value);
//                            sender.sendMessage("§aSuccesfully added §6" + value + " §acoins to " + playerName + "§a!");
//                            currentToken = previousToken + value;
//                            ChronoCord.getInstance().getChronoLogger().logToken(playerName, uuid.toString(), String.valueOf(previousToken), String.valueOf(currentToken), "Token Add", String.valueOf(value), sender.getName(), channel);
//                        }
//                    } catch (NumberFormatException e) {
//                        e.printStackTrace(System.out);
//                        sender.sendMessage("§cInvalid value of coin, Please provide a valid number.");
//                    } catch (SQLException e) {
//                        e.printStackTrace(System.out);
//                        sender.sendMessage("§cAn error occurred while updating Athena Coins.");
//                    }
//
//
//                }
                break;

//            case "remove":
//                if (args.length < 2) {
//                    sender.sendMessage("§cUsage: /athenacoins remove <player> <value>");
//                    break;
//                }
//
//                playerName = args[1];
//                player = Bukkit.getPlayer(playerName);
//
//                if (player != null) {
//                    try {
//                        int value = Integer.parseInt(args[2]);
//                        uuid = player.getUniqueId();
//                        playerName = player.getName();
//                        PlayerAthenaCoins athenaCoins = database.getAthenaCoinsByUUID(uuid);
//
//                        if (athenaCoins.getPlayerName() == null || athenaCoins.getAthenaCoins() <= value) {
//                            sender.sendMessage("§b" + playerName +  " §cdoesn't has enough Coins to be removed!");
//                        } else {
//                            previousToken = athenaCoins.getAthenaCoins();
//                            database.addAthenaCoins(athenaCoins, -value);
//                            currentToken = previousToken - value;
//                            sender.sendMessage("§aSuccesfully removed §6" + value + " §acoins from " + playerName + "§a!");
//                            ChronoCord.getInstance().getChronoLogger().logToken(playerName, uuid.toString(), String.valueOf(previousToken), String.valueOf(currentToken), "Token Remove", String.valueOf(value), sender.getName(), channel);
//                        }
//                    } catch (NumberFormatException e) {
//                        e.printStackTrace(System.out);
//                        sender.sendMessage("§cInvalid value of coin, Please provide a valid number.");
//                    } catch (SQLException e) {
//                        e.printStackTrace(System.out);
//                        sender.sendMessage("§cAn error occurred while updating Athena Coins.");
//                    }
//                } else {
//                    try {
//                        int value = Integer.parseInt(args[2]);
//                        uuid = database.getPlayerUUID(playerName);
//                        playerName = database.getPlayer(uuid.toString());
//                        PlayerAthenaCoins athenaCoins = database.getAthenaCoinsByUUID(uuid);
//
//                        if (athenaCoins.getPlayerName() == null || athenaCoins.getAthenaCoins() <= value) {
//                            sender.sendMessage("§b" + playerName +  " §cdoesn't has enough Coins to be removed!");
//                        } else {
//                            previousToken = athenaCoins.getAthenaCoins();
//                            database.addAthenaCoins(athenaCoins, -value);
//                            currentToken = previousToken - value;
//                            sender.sendMessage("§aSuccesfully removed §6" + value + " §acoins from " + playerName + "§a!");
//                            ChronoCord.getInstance().getChronoLogger().logToken(playerName, uuid.toString(), String.valueOf(previousToken), String.valueOf(currentToken), "Token Add", String.valueOf(value), sender.getName(), channel);
//                        }
//                    } catch (NumberFormatException e) {
//                        e.printStackTrace(System.out);
//                        sender.sendMessage("§cInvalid value of coin, Please provide a valid number.");
//                    } catch (SQLException e) {
//                        e.printStackTrace(System.out);
//                        sender.sendMessage("§cAn error occurred while updating Athena Coins.");
//                    }
//                }
//                break;

            default:
                sender.sendMessage("§cInvalid subcommand");
        }
        return true;
    }
}
