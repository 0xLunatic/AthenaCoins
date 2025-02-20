package lunatic.athenacoins;

import lunatic.athenacoins.Command.AthenaCommands;
import lunatic.athenacoins.Command.TabCompleters;
import lunatic.athenacoins.Database.Database;
import lunatic.athenacoins.Hook.PlaceholderManager;
import lunatic.athenacoins.Listener.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;

public final class AthenaCoins extends JavaPlugin {
    public static AthenaCoins instance;
    private Database database;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        instance = this;
        try {
            this.database = new Database(this);
            database.initializeDatabase();
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Failed to connect to the Database and create Tables!");
            e.printStackTrace(System.out);
        }

        saveDefaultConfig();
        this.config = getConfig();
        Bukkit.getPluginManager().registerEvents(new PlayerData(this), this);

        getCommand("athenacoins").setExecutor(new AthenaCommands(this, database));
        getCommand("athenacoins").setTabCompleter(new TabCompleters(this));
        getCommand("athenacoins").setAliases(config.getStringList("command-alias"));

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null &&
                Bukkit.getPluginManager().getPlugin("ChronoCord") != null) {
            new PlaceholderManager(this, database).register();
        }
    }

    @Override
    public void onDisable() {
        database.closeDataSource();
    }

    public void saveDefaultConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveResource("config.yml", false);
            getLogger().info("config.yml not found, creating default config.yml");
        }
    }

    public Database getDatabase() {
        return database;
    }

    public void doAsync(final Runnable runnable) {
        getServer().getScheduler().runTaskAsynchronously(this, runnable);
    }

    public void doAsyncLater(final Runnable runnable, final long delay) {
        getServer().getScheduler().runTaskLaterAsynchronously(this, runnable, delay);
    }

    public static AthenaCoins getInstance() {
        return instance;
    }
}