package lunatic.athenatoken.databases;

import lunatic.athenatoken.Main;
import lunatic.athenatoken.utils.PlayerAthenaCoins;

import java.sql.*;
import java.util.UUID;

public class Database {
    private final Main plugin;

    private Connection connection;

    public Database(Main plugin) {
        this.plugin = plugin;
    }

    public Connection getConnection() throws SQLException {

        if (connection != null) {
            return connection;
        }
        String database = plugin.getConfig().getString("database.database");
        String host = plugin.getConfig().getString("database.host");
        String port = plugin.getConfig().getString("database.port");

        String username = plugin.getConfig().getString("database.user");
        String password = plugin.getConfig().getString("database.password");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database;

        this.connection = DriverManager.getConnection(url, username, password);

        return this.connection;
    }

    public void initializeDatabase() throws SQLException {
        Connection connection = getConnection();
        Statement statement = connection.createStatement();

        // Create the table if it does not exist
        String createTableSQL = "CREATE TABLE IF NOT EXISTS coins(player_uuid varchar(36) primary key, player_name varchar(36), athena_coins int)";
        statement.executeUpdate(createTableSQL);
        System.out.println("Table not found, Creating new table.");

        statement.close();
    }
    public PlayerAthenaCoins getAthenaCoinsByUUID(UUID uuid) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM coins WHERE player_uuid = ?");
        statement.setString(1, String.valueOf(uuid));

        ResultSet results = statement.executeQuery();

        if (results.next()) {
            int athena_coins = results.getInt("athena_coins");
            String playerName = results.getString("player_name");

            PlayerAthenaCoins athenaCoins = new PlayerAthenaCoins(uuid, playerName, athena_coins);

            statement.close();
            return athenaCoins;
        }
        statement.close();
        return null;
    }
    public void createAthenaCoins(PlayerAthenaCoins athenaCoins) throws SQLException {
        PreparedStatement statement = getConnection()
                .prepareStatement("INSERT INTO coins(player_uuid, player_name, athena_coins) VALUES (?, ?, ?)");

        statement.setString(1, String.valueOf(athenaCoins.getPlayerUUID()));
        statement.setString(2, athenaCoins.getPlayerName());
        statement.setDouble(3, athenaCoins.getAthenaCoins());

        statement.executeUpdate();
        statement.close();
    }
    public void clearAthenaCoins(PlayerAthenaCoins athenaCoins) throws SQLException {
        PreparedStatement statement = getConnection()
                .prepareStatement("DELETE FROM coins WHERE player_uuid = ?");

        statement.setString(1, String.valueOf(athenaCoins.getPlayerUUID()));

        statement.executeUpdate();
        statement.close();
    }
    public void clearDatabase() throws SQLException {
        PreparedStatement statement = getConnection()
                .prepareStatement("DROP TABLE coins");
        statement.executeUpdate();
        statement.close();
    }
    public void updatAthenaCoins(PlayerAthenaCoins athenaCoins) throws SQLException {
        PreparedStatement statement = getConnection()
                .prepareStatement("UPDATE coins SET athena_coins = ? WHERE player_uuid = ?");

        statement.setDouble(1, athenaCoins.getAthenaCoins());
        statement.setString(2, String.valueOf(athenaCoins.getPlayerUUID()));

        statement.executeUpdate();
        statement.close();
    }
    public void addAthenaCoins(PlayerAthenaCoins athenaCoins, double value) throws SQLException{
        PreparedStatement statement = getConnection()
                .prepareStatement("UPDATE coins SET athena_coins = ? WHERE player_uuid = ?");

        statement.setDouble(1, athenaCoins.getAthenaCoins() + value);
        statement.setString(2, String.valueOf(athenaCoins.getPlayerUUID()));

        statement.executeUpdate();
        statement.close();
    }
}