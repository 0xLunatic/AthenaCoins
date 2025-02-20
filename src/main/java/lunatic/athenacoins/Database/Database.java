package lunatic.athenacoins.Database;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lunatic.athenacoins.Api.AthenaCoinsApi;
import lunatic.athenacoins.AthenaCoins;
import lunatic.athenacoins.Exceptions.DataNotLoadedException;
import lunatic.athenacoins.Util.PlayerAthenaCoins;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;
import java.util.List;
import java.util.concurrent.*;

public class Database implements AthenaCoinsApi {
    private final AthenaCoins plugin;
    private static HikariDataSource dataSource;
    private final Cache<UUID, PlayerAthenaCoins> playerDataCache = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build();
    private final ExecutorService executorService;
    private final List<UUID> loading = new CopyOnWriteArrayList<>();

    public Database(AthenaCoins plugin) {
        this.plugin = plugin;
        this.executorService = Executors.newWorkStealingPool();

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

    public void loadData(UUID uuid) {
        if (loading.contains(uuid)) {
            return;
        }

        loading.add(uuid);
        try {
            this.getAthenaCoinsByUUIDAsync(uuid, Bukkit.getOfflinePlayer(uuid).getName())
                    .thenAccept((data) -> {
                        this.playerDataCache.put(uuid, data);
                        loading.remove(uuid);
                    })
                    .exceptionally((e) -> {
                        e.printStackTrace();

                        loading.remove(uuid);
                        return null;
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<PlayerAthenaCoins> getAthenaCoinsByUUIDAsync(UUID uuid, String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            PlayerAthenaCoins data = new PlayerAthenaCoins(plugin, uuid);
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement("SELECT * FROM coins WHERE player_uuid = ?")) {

                statement.setString(1, String.valueOf(uuid));
                ResultSet results = statement.executeQuery();

                if (results.next()) {
                    data.setBalance(results.getInt("athena_coins"));
                }

            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to fetch AthenaCoins asynchronously.", e);
            }
            return data;
        }, executorService);
    }

    public PlayerAthenaCoins getLoadedData(UUID uuid) throws DataNotLoadedException {
        PlayerAthenaCoins data = this.playerDataCache.getIfPresent(uuid);
        if (data == null) {
            loadData(uuid);
            throw new DataNotLoadedException("Data for " + uuid + " is not loaded yet!");
        }

        return data;
    }

    @Override
    public boolean withdraw(UUID uuid, String playerName, double amount, String executor) {
        invalidateCache(uuid);
        if (Bukkit.getPlayer(uuid) != null) {
            Player player = Bukkit.getPlayer(uuid);
        }
        String SQL = "UPDATE coins SET `athena_coins` = `athena_coins` - ? WHERE `player_uuid` = ?";
        return executeCoinQuery(uuid, amount, SQL);
    }

    @Override
    public boolean deposit(UUID uuid, String playerName, double amount, String executor) {
        invalidateCache(uuid);
        if (Bukkit.getPlayer(uuid) != null) {
            Player player = Bukkit.getPlayer(uuid);
        }
        if (isExist(uuid.toString(), playerName)) {
            String SQL = "UPDATE coins SET `athena_coins` = `athena_coins` + ? WHERE `player_uuid` = ?";
            return executeCoinQuery(uuid, amount, SQL);
        } else {
            String SQL = "INSERT INTO coins (player_uuid, player_name, athena_coins) VALUES (?, ?, ?)";
            return executeCoinQueryInsert(uuid, playerName, amount, SQL);
        }
    }

    @Override
    public boolean set(UUID uuid, String playerName, double amount, String executor) {
        invalidateCache(uuid);
        if (Bukkit.getPlayer(uuid) != null) {
            Player player = Bukkit.getPlayer(uuid);
        }
        String SQL = "UPDATE coins SET athena_coins = ? WHERE `player_uuid` = ?";
        return executeCoinQuery(uuid, amount, SQL);
    }

    @Override
    public int getBalance(UUID uuid, String playerName) {
        return getBalance(uuid, playerName, true);
    }

    @Override
    public int getBalance(UUID uuid, String playerName, boolean wait) {
        if (wait) {
            try {
                PlayerAthenaCoins data = this.getAthenaCoinsByUUIDAsync(uuid, playerName).get(5, TimeUnit.SECONDS);
                return data.getBalance();
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace(System.out);
                return -1;
            }
        } else {
            try {
                PlayerAthenaCoins data = this.getLoadedData(uuid);
                return data.getBalance();
            } catch (DataNotLoadedException e) {
                loadData(uuid);
                return 0;
            }
        }
    }

    @Override
    public CompletableFuture<Integer> getBalanceFuture(UUID uuid, String playerName) {
        try {
            PlayerAthenaCoins data = this.getLoadedData(uuid);
            return CompletableFuture.completedFuture(data.getBalance());
        } catch (DataNotLoadedException e) {
            return this
                    .getAthenaCoinsByUUIDAsync(uuid, playerName)
                    .thenApply(PlayerAthenaCoins::getBalance)
                    .exceptionally((ex) -> {
                        ex.printStackTrace(System.out);
                        return -1;
                    });
        }
    }

//    public void createAthenaCoins(PlayerAthenaCoins athenaCoins) throws SQLException {
//        try (Connection connection = getConnection();
//             PreparedStatement statement = connection
//                     .prepareStatement("INSERT INTO coins(player_uuid, player_name, athena_coins) VALUES (?, ?, ?)")) {
//
//            statement.setString(1, String.valueOf(athenaCoins.getPlayerUUID()));
//            statement.setString(2, athenaCoins.getPlayerName());
//            statement.setDouble(3, athenaCoins.getAthenaCoins());
//
//            statement.executeUpdate();
//        }
//    }

//    public void clearAthenaCoins(PlayerAthenaCoins athenaCoins) throws SQLException {
//        try (Connection connection = getConnection();
//             PreparedStatement statement = connection
//                     .prepareStatement("DELETE FROM coins WHERE player_uuid = ?")) {
//
//            statement.setString(1, String.valueOf(athenaCoins.getPlayerUUID()));
//
//            statement.executeUpdate();
//        }
//    }

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

    public String getPlayer(String uuid) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection
                     .prepareStatement("SELECT player_name FROM coins WHERE player_uuid = ?")) {

            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("player_name");
            } else {
                return null;
            }
        }
    }

    public boolean isExist(String playerName) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection
                     .prepareStatement("SELECT 1 FROM coins WHERE player_name = ? LIMIT 1")) {

            statement.setString(1, playerName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return true;
            } else {
                return false;
            }
        }
    }

    public UUID getPlayerUUID(String username) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection
                     .prepareStatement("SELECT player_uuid FROM coins WHERE player_name = ?")) {

            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String uuid = resultSet.getString("player_uuid");
                return UUID.fromString(uuid);
            } else {
                return null;
            }
        }
    }

//    public void recordPlayerData(String uuid, String username) throws SQLException {
//        try (Connection connection = getConnection();
//             PreparedStatement statement = connection.prepareStatement("INSERT INTO coins (player_uuid, player_name, athena_coins) VALUES (?, ?, ?)")) {
//
//            statement.setString(1, uuid);
//            statement.setString(2, username);
//            statement.setString(3, "0");
//
//            statement.executeUpdate();
//
//            Bukkit.getLogger().info("Generated " + username + " player data succesfully");
//        } catch (SQLException e) {
//            e.printStackTrace(System.out);
//        }
//    }

    public boolean isExist(String uuid, String username) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM coins WHERE player_uuid = ? AND player_name = ?")) {

            statement.setString(1, uuid);
            statement.setString(2, username);

            ResultSet result = statement.executeQuery();
            if (result.next()) {
                int count = result.getInt(1);
                return count > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace(System.out);
        }
        return false;
    }

    public void invalidateCache(UUID uuid) {
        playerDataCache.invalidate(uuid);
    }

    private boolean executeCoinQuery(UUID uuid, double amount, String SQL) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL)) {

            statement.setDouble(1, amount);
            statement.setString(2, uuid.toString());
            statement.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace(System.out);
            return false;
        }
    }

    private boolean executeCoinQueryInsert(UUID uuid, String playerName, double amount, String SQL) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL)) {

            statement.setString(1, uuid.toString());
            statement.setString(2, playerName);
            statement.setDouble(3, amount);
            statement.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace(System.out);
            return false;
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