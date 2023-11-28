package lunatic.athenatoken;

import lunatic.athenatoken.commands.AthenaCommands;
import lunatic.athenatoken.databases.Database;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class Main extends JavaPlugin {
    private Database database;
    @Override
    public void onEnable() {
        // Plugin startup logic
        try {
            this.database = new Database(this);
            database.initializeDatabase();
        } catch (SQLException ex) {
            System.out.println("Failed to connect to the Database and create Tables!");
            ex.printStackTrace();
        }

        getCommand("athenacoins").setExecutor(new AthenaCommands(this, new Database(this)));


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
