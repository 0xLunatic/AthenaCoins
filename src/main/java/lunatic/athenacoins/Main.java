package lunatic.athenacoins;

import lunatic.athenacoins.commands.AthenaCommands;
import lunatic.athenacoins.commands.TabCompleters;
import lunatic.athenacoins.databases.Database;
import lunatic.athenacoins.papi.PlaceholderManager;
import me.kenvera.chronocore.ChronoCore;
import me.kenvera.chronocore.hooks.ChronoLogger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;

public final class Main extends JavaPlugin {
    public static Main instance;
    private ChronoCore chronoCore;
    private ChronoLogger chronoLogger;
    private Database database;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        try {
            this.database = new Database(this);
            database.initializeDatabase();
        } catch (SQLException ex) {
            System.out.println("Failed to connect to the Database and create Tables!");
            ex.printStackTrace();
        }

        // Check and create config.yml if it doesn't exist
        saveDefaultConfig();
        this.config = getConfig();

        // Register commands
        getCommand("athenacoins").setExecutor(new AthenaCommands(this, database));
        getCommand("athenacoins").setTabCompleter(new TabCompleters(this));

        // Check if PlaceholderAPI is present and register placeholder if true
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && Bukkit.getPluginManager().getPlugin("ChronoCore") != null) {
            new PlaceholderManager(this, database).register();
            chronoCore = (ChronoCore) Bukkit.getPluginManager().getPlugin("ChronoCore");
            chronoLogger = chronoCore.getChronoLogger();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        database.closeDataSource();
    }

    // Method to save the default config.yml if it doesn't exist
    public void saveDefaultConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveResource("config.yml", false);
            getLogger().info("Config.yml not found, creating default config.yml");
        }
    }

    public Database getDatabase() {
        return database;
    }

    public Main getInstance() {
        return instance;
    }

    public ChronoLogger getChronoLogger() {
        return chronoLogger;
    }

}