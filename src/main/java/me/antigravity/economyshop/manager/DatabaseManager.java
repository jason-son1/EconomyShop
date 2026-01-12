package me.antigravity.economyshop.manager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.antigravity.economyshop.EconomyShop;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 플러그인의 데이터베이스 연결(HikariCP)을 관리하는 클래스입니다.
 * SQLite 및 MySQL을 지원합니다.
 */
public class DatabaseManager {

    private final EconomyShop plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(EconomyShop plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        String type = plugin.getConfigManager().getMainConfig().getString("start-settings.storage-type", "YAML");

        // YAML 모드인 경우 DB를 사용하지 않거나, SQLite를 보조로 사용할 수 있음.
        // 여기서는 Plan에 따라 SQLite/MySQL 구조를 잡음.

        setupDataSource(type);
        createTables();
    }

    private void setupDataSource(String type) {
        HikariConfig config = new HikariConfig();

        if (type.equalsIgnoreCase("MYSQL")) {
            String host = "localhost"; // config에서 로드 예정
            String port = "3306";
            String database = "economyshop";
            String username = "root";
            String password = "";

            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
            config.setUsername(username);
            config.setPassword(password);
        } else {
            // 기본값: SQLite
            File dbFile = new File(plugin.getDataFolder(), "database.db");
            config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            config.setDriverClassName("org.sqlite.JDBC");
        }

        config.setMaximumPoolSize(10);
        this.dataSource = new HikariDataSource(config);
    }

    private void createTables() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // 동적 가격 정보 테이블
            stmt.execute("CREATE TABLE IF NOT EXISTS shop_dynamic_prices (" +
                    "item_id VARCHAR(64) PRIMARY KEY, " +
                    "current_stock BIGINT, " +
                    "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // 플레이어 제한 기록 테이블
            stmt.execute("CREATE TABLE IF NOT EXISTS player_limits (" +
                    "uuid VARCHAR(36), " +
                    "item_id VARCHAR(64), " +
                    "purchase_count INT, " +
                    "reset_date DATE, " +
                    "PRIMARY KEY (uuid, item_id, reset_date))");

        } catch (SQLException e) {
            plugin.getLogger().severe("테이블 생성 중 오류 발생: " + e.getMessage());
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    // --- 동적 가격 관련 ---

    public void saveDynamicPrice(String itemId, long currentStock) {
        String sql = "REPLACE INTO shop_dynamic_prices (item_id, current_stock, last_updated) VALUES (?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, itemId);
            pstmt.setLong(2, currentStock);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public long loadDynamicStock(String itemId, long defaultStock) {
        String sql = "SELECT current_stock FROM shop_dynamic_prices WHERE item_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, itemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("current_stock");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return defaultStock;
    }

    // --- 플레이어 제한 관련 ---

    public void savePlayerLimit(String uuid, String itemId, int count) {
        String sql = "REPLACE INTO player_limits (uuid, item_id, purchase_count, reset_date) VALUES (?, ?, ?, CURRENT_DATE)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.setString(2, itemId);
            pstmt.setInt(3, count);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int loadPlayerLimit(String uuid, String itemId) {
        String sql = "SELECT purchase_count FROM player_limits WHERE uuid = ? AND item_id = ? AND reset_date = CURRENT_DATE";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.setString(2, itemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("purchase_count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
