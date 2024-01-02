package lunatic.athenacoins.databases;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lunatic.athenacoins.Main;
import lunatic.athenacoins.utils.PlayerAthenaCoins;

import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;
import java.util.List;

public class Database {
    private final Main plugin;
    private static HikariDataSource dataSource;

    public Database(Main plugin) {
        this.plugin = plugin;

        String database = plugin.getConfig().getString("database.database");
        String host = plugin.getConfig().getString("database.host");
        String port = plugin.getConfig().getString("database.port");

        String username = plugin.getConfig().getString("database.user");
        String password = plugin.getConfig().getString("database.password");

        HikariConfig config = new HikariConfig();
        config.setUsername(username);
        config.setPassword(password);
        config.setConnectionTimeout(10000);
        config.setMaximumPoolSize(20);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?allowPublicKeyRetrieval=true&useSSL=false");
        config.setMaxLifetime(30000);
        dataSource = new HikariDataSource(config);

        try {
            Connection connection = dataSource.getConnection();
            closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void initializeDatabase() throws SQLException {
        try (Connection connection = getConnection();
            Statement statement = connection.createStatement()){

            // Create the table if it does not exist
            String createTableSQL = "CREATE TABLE IF NOT EXISTS coins(player_uuid varchar(36) primary key, player_name varchar(36), athena_coins int)";
            statement.executeUpdate(createTableSQL);
            System.out.println("Table not found, Creating new table.");
        }
    }

    public PlayerAthenaCoins getAthenaCoinsByUUID(UUID uuid) throws SQLException {
        try (Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM coins WHERE player_uuid = ?")) {
            statement.setString(1, String.valueOf(uuid));

            ResultSet results = statement.executeQuery();

            if (results.next()) {
                int athena_coins = results.getInt("athena_coins");
                String playerName = results.getString("player_name");

                PlayerAthenaCoins athenaCoins = new PlayerAthenaCoins(uuid, playerName, athena_coins);

                return athenaCoins;
            }
            PlayerAthenaCoins athenaCoins = new PlayerAthenaCoins(uuid, null, 0);

            return athenaCoins;

        }
    }

    public void createAthenaCoins(PlayerAthenaCoins athenaCoins) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection
                     .prepareStatement("INSERT INTO coins(player_uuid, player_name, athena_coins) VALUES (?, ?, ?)")) {

            statement.setString(1, String.valueOf(athenaCoins.getPlayerUUID()));
            statement.setString(2, athenaCoins.getPlayerName());
            statement.setDouble(3, athenaCoins.getAthenaCoins());

            statement.executeUpdate();
        }
    }

    public void clearAthenaCoins(PlayerAthenaCoins athenaCoins) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection
                     .prepareStatement("DELETE FROM coins WHERE player_uuid = ?")) {

            statement.setString(1, String.valueOf(athenaCoins.getPlayerUUID()));

            statement.executeUpdate();
        }
    }

    public void clearDatabase() throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection
                     .prepareStatement("DROP TABLE coins")) {

            statement.executeUpdate();
        }
    }

    public void updateAthenaCoins(PlayerAthenaCoins athenaCoins) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection
                     .prepareStatement("UPDATE coins SET athena_coins = ? WHERE player_uuid = ?")) {

            statement.setDouble(1, athenaCoins.getAthenaCoins());
            statement.setString(2, String.valueOf(athenaCoins.getPlayerUUID()));

            statement.executeUpdate();
        }
    }

    public void addAthenaCoins(PlayerAthenaCoins athenaCoins, double value) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection
                     .prepareStatement("UPDATE coins SET athena_coins = ? WHERE player_uuid = ?")) {

            statement.setDouble(1, athenaCoins.getAthenaCoins() + value);
            statement.setString(2, String.valueOf(athenaCoins.getPlayerUUID()));

            statement.executeUpdate();
        }
    }

    public List<String> getPlayers() throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection
                     .prepareStatement("SELECT player_name FROM coins LIMIT 50 OFFSET 0")) {

            List<String> players = new ArrayList<>();

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String playerName = resultSet.getString("player_name");
                players.add(playerName);
            }
            return players;
        }
    }

    public void closeConnection(Connection connection) {
        dataSource.evictConnection(connection);
    }

    public void closeDataSource() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public int getActiveConnections() {
        return dataSource.getHikariPoolMXBean().getActiveConnections();
    }

    public int getTotalConnections() {
        return dataSource.getHikariPoolMXBean().getTotalConnections();
    }

    public int getIdleConnections() {
        return dataSource.getHikariPoolMXBean().getIdleConnections();
    }
}