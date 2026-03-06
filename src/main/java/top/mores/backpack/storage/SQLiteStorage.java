package top.mores.backpack.storage;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import top.mores.backpack.Backpack;

import java.io.File;
import java.sql.*;
import java.util.*;

public class SQLiteStorage {
    private static final String EMPTY_JSON_ARRAY = "[]";
    private final File databaseFile;
    private Connection connection;

    public SQLiteStorage(File dataFolder) {
        this.databaseFile = new File(dataFolder, "backpack.db");
    }

    public synchronized void initialize() {
        try {
            openConnectionIfNeeded();
            configureConnection();
            createTables();
        } catch (SQLException e) {
            Backpack.getInstance().getLogger().severe("初始化SQLite失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public synchronized void close() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                Backpack.getInstance().getLogger().warning("关闭SQLite连接失败: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }

    /* =========================
       玩家背包
       ========================= */

    public synchronized void initializePlayerBackpacks(UUID playerUuid, int backpackAmount) {
        if (playerUuid == null || backpackAmount <= 0) {
            return;
        }

        final String sql = "INSERT OR IGNORE INTO player_backpacks(player_uuid, backpack_slot, items_json, enabled_skills_json) VALUES (?, ?, ?, ?)";

        try {
            ensureConnection();

            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (int i = 1; i <= backpackAmount; i++) {
                    ps.setString(1, playerUuid.toString());
                    ps.setInt(2, i);
                    ps.setString(3, EMPTY_JSON_ARRAY);
                    ps.setString(4, EMPTY_JSON_ARRAY);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            connection.commit();
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            rollbackQuietly();
            Backpack.getInstance().getLogger().warning("初始化玩家背包失败: " + playerUuid + ", " + e.getMessage());
        }
    }

    public synchronized List<Map<String, Object>> getBackpackItems(UUID playerUuid, int backpackSlot) {
        final String sql = "SELECT items_json FROM player_backpacks WHERE player_uuid=? AND backpack_slot=?";

        try {
            ensureConnection();

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, playerUuid.toString());
                ps.setInt(2, backpackSlot);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return parseItemList(rs.getString("items_json"));
                    }
                }
            }
        } catch (SQLException e) {
            Backpack.getInstance().getLogger().warning(playerUuid + " 读取背包失败: " + e.getMessage());
        }

        return new ArrayList<>();
    }

    public synchronized void setBackpackItems(UUID playerUuid, int backpackSlot, List<Map<String, Object>> items) {
        if (playerUuid == null || backpackSlot <= 0) {
            return;
        }

        try {
            ensureConnection();
            ensureBackpackRow(playerUuid, backpackSlot);

            final String sql = "UPDATE player_backpacks SET items_json=? WHERE player_uuid=? AND backpack_slot=?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, toJson(items == null ? Collections.emptyList() : items));
                ps.setString(2, playerUuid.toString());
                ps.setInt(3, backpackSlot);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            Backpack.getInstance().getLogger().warning(playerUuid + " 保存背包失败: " + e.getMessage());
        }
    }

    public synchronized void clearBackpackItems(UUID playerUuid, int backpackSlot) {
        setBackpackItems(playerUuid, backpackSlot, Collections.emptyList());
    }

    public synchronized List<Integer> getEnabledSkills(UUID playerUuid, int backpackSlot) {
        final String sql = "SELECT enabled_skills_json FROM player_backpacks WHERE player_uuid=? AND backpack_slot=?";

        try {
            ensureConnection();

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, playerUuid.toString());
                ps.setInt(2, backpackSlot);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return parseIntegerList(rs.getString("enabled_skills_json"));
                    }
                }
            }
        } catch (SQLException e) {
            Backpack.getInstance().getLogger().warning(playerUuid + " 读取技能失败: " + e.getMessage());
        }

        return new ArrayList<>();
    }

    public synchronized void setEnabledSkills(UUID playerUuid, int backpackSlot, List<Integer> skills) {
        if (playerUuid == null || backpackSlot <= 0) {
            return;
        }

        try {
            ensureConnection();
            ensureBackpackRow(playerUuid, backpackSlot);

            final String sql = "UPDATE player_backpacks SET enabled_skills_json=? WHERE player_uuid=? AND backpack_slot=?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, toJson(skills == null ? Collections.emptyList() : skills));
                ps.setString(2, playerUuid.toString());
                ps.setInt(3, backpackSlot);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            Backpack.getInstance().getLogger().warning(playerUuid + " 保存技能失败: " + e.getMessage());
        }
    }

    public synchronized int findFirstNonEmptyBackpack(UUID playerUuid, int backpackAmount) {
        if (playerUuid == null || backpackAmount <= 0) {
            return -1;
        }

        final String sql = "SELECT backpack_slot, items_json FROM player_backpacks " +
                "WHERE player_uuid=? AND backpack_slot BETWEEN 1 AND ? ORDER BY backpack_slot ASC";

        try {
            ensureConnection();

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, playerUuid.toString());
                ps.setInt(2, backpackAmount);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int slot = rs.getInt("backpack_slot");
                        String itemsJson = rs.getString("items_json");
                        if (itemsJson != null && !itemsJson.isBlank() && !EMPTY_JSON_ARRAY.equals(itemsJson.trim())) {
                            List<Map<String, Object>> items = parseItemList(itemsJson);
                            if (!items.isEmpty()) {
                                return slot;
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            Backpack.getInstance().getLogger().warning(playerUuid + " 查询首个非空背包失败: " + e.getMessage());
        }

        return -1;
    }

    /* =========================
       物品匹配
       ========================= */

    public synchronized void setItemMatch(String itemName, List<Map<String, Object>> itemData) {
        if (itemName == null || itemName.isBlank()) {
            return;
        }

        final String sql = "INSERT INTO item_matches(item_name, items_json) VALUES (?, ?) " +
                "ON CONFLICT(item_name) DO UPDATE SET items_json=excluded.items_json";

        try {
            ensureConnection();

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, itemName);
                ps.setString(2, toJson(itemData == null ? Collections.emptyList() : itemData));
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            Backpack.getInstance().getLogger().warning("保存物品匹配失败: " + e.getMessage());
        }
    }

    public synchronized boolean hasItemMatch(String itemName) {
        if (itemName == null || itemName.isBlank()) {
            return false;
        }

        final String sql = "SELECT 1 FROM item_matches WHERE item_name=? LIMIT 1";

        try {
            ensureConnection();

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, itemName);

                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            Backpack.getInstance().getLogger().warning("检查物品匹配失败: " + e.getMessage());
            return false;
        }
    }

    public synchronized List<Map<String, Object>> getItemMatch(String itemName) {
        if (itemName == null || itemName.isBlank()) {
            return new ArrayList<>();
        }

        final String sql = "SELECT items_json FROM item_matches WHERE item_name=?";

        try {
            ensureConnection();

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, itemName);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return parseItemList(rs.getString("items_json"));
                    }
                }
            }
        } catch (SQLException e) {
            Backpack.getInstance().getLogger().warning("读取物品匹配失败: " + e.getMessage());
        }

        return new ArrayList<>();
    }

    public synchronized List<String> getItemMatchKeys() {
        final List<String> keys = new ArrayList<>();
        final String sql = "SELECT item_name FROM item_matches ORDER BY item_name ASC";

        try {
            ensureConnection();

            try (PreparedStatement ps = connection.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    keys.add(rs.getString("item_name"));
                }
            }
        } catch (SQLException e) {
            Backpack.getInstance().getLogger().warning("读取物品匹配键失败: " + e.getMessage());
        }

        return keys;
    }

    /* =========================
       盔甲套装
       ========================= */

    public synchronized void setArmorSet(String setName, List<Map<String, Object>> items) {
        if (setName == null || setName.isBlank()) {
            return;
        }

        final String sql = "INSERT INTO armor_sets(set_name, items_json) VALUES (?, ?) " +
                "ON CONFLICT(set_name) DO UPDATE SET items_json=excluded.items_json";

        try {
            ensureConnection();

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, setName);
                ps.setString(2, toJson(items == null ? Collections.emptyList() : items));
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            Backpack.getInstance().getLogger().warning("保存盔甲套装失败: " + e.getMessage());
        }
    }

    public synchronized List<Map<String, Object>> getArmorSet(String setName) {
        if (setName == null || setName.isBlank()) {
            return new ArrayList<>();
        }

        final String sql = "SELECT items_json FROM armor_sets WHERE set_name=?";

        try {
            ensureConnection();

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, setName);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return parseItemList(rs.getString("items_json"));
                    }
                }
            }
        } catch (SQLException e) {
            Backpack.getInstance().getLogger().warning("读取盔甲套装失败: " + e.getMessage());
        }

        return new ArrayList<>();
    }

    /* =========================
       初始化
       ========================= */

    private void createTables() throws SQLException {
        ensureConnection();

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS player_backpacks (" +
                    "player_uuid TEXT NOT NULL," +
                    "backpack_slot INTEGER NOT NULL," +
                    "items_json TEXT NOT NULL DEFAULT '[]'," +
                    "enabled_skills_json TEXT NOT NULL DEFAULT '[]'," +
                    "PRIMARY KEY(player_uuid, backpack_slot)" +
                    ")");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS item_matches (" +
                    "item_name TEXT PRIMARY KEY," +
                    "items_json TEXT NOT NULL" +
                    ")");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS armor_sets (" +
                    "set_name TEXT PRIMARY KEY," +
                    "items_json TEXT NOT NULL" +
                    ")");

        }
    }

    private void configureConnection() throws SQLException {
        ensureConnection();

        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA journal_mode=WAL;");
            st.execute("PRAGMA synchronous=NORMAL;");
            st.execute("PRAGMA busy_timeout=5000;");
            st.execute("PRAGMA foreign_keys=ON;");
        }
    }

    /* =========================
       内部工具
       ========================= */

    private void openConnectionIfNeeded() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        }
    }

    private void ensureConnection() throws SQLException {
        openConnectionIfNeeded();
    }

    private void ensureBackpackRow(UUID playerUuid, int backpackSlot) throws SQLException {
        final String sql = "INSERT OR IGNORE INTO player_backpacks(player_uuid, backpack_slot, items_json, enabled_skills_json) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.setInt(2, backpackSlot);
            ps.setString(3, EMPTY_JSON_ARRAY);
            ps.setString(4, EMPTY_JSON_ARRAY);
            ps.executeUpdate();
        }
    }

    private void rollbackQuietly() {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {
            }
        }
    }

    private List<Map<String, Object>> parseItemList(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }

        try {
            Object value = new JSONParser().parse(json);
            if (value instanceof List<?> list) {
                List<Map<String, Object>> converted = new ArrayList<>();

                for (Object o : list) {
                    if (o instanceof Map<?, ?> map) {
                        Map<String, Object> target = new HashMap<>();
                        for (Map.Entry<?, ?> entry : map.entrySet()) {
                            if (entry.getKey() != null) {
                                target.put(String.valueOf(entry.getKey()), entry.getValue());
                            }
                        }
                        converted.add(target);
                    }
                }

                return converted;
            }
        } catch (Exception e) {
            Backpack.getInstance().getLogger().warning("解析物品JSON失败: " + e.getMessage());
        }

        return new ArrayList<>();
    }

    private List<Integer> parseIntegerList(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }

        try {
            Object value = new JSONParser().parse(json);
            if (value instanceof JSONArray array) {
                List<Integer> result = new ArrayList<>();
                for (Object o : array) {
                    if (o instanceof Number number) {
                        result.add(number.intValue());
                    }
                }
                return result;
            }
        } catch (Exception e) {
            Backpack.getInstance().getLogger().warning("解析技能JSON失败: " + e.getMessage());
        }

        return new ArrayList<>();
    }

    private String toJson(Object value) {
        return JSONValue.toJSONString(value);
    }

    private List<Map<String, Object>> convertToStringObjectMapList(List<Map<?, ?>> source) {
        List<Map<String, Object>> converted = new ArrayList<>();
        if (source == null) {
            return converted;
        }

        for (Map<?, ?> map : source) {
            Map<String, Object> target = new HashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() != null) {
                    target.put(String.valueOf(entry.getKey()), entry.getValue());
                }
            }
            converted.add(target);
        }

        return converted;
    }
}
