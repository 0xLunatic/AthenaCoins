package lunatic.athenacoins;

import lunatic.athenacoins.commands.AthenaCommands;
import lunatic.athenacoins.databases.Database;
import lunatic.athenacoins.papi.PlaceholderManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;

public final class Main extends JavaPlugin {
    private Database database;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        // Plugin startup logic
        // Initialize the database
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

        // Check if PlaceholderAPI is present and register placeholder if true
//        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
//            new PlaceholderManager(this, database).register();
//        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    // Method to save the default config.yml if it doesn't exist
    public void saveDefaultConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveResource("config.yml", false);
            getLogger().info("Config.yml not found, creating default config.yml");
        }
    }
}