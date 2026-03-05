package top.mores.backpack.storage;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import top.mores.backpack.Backpack;

import java.io.File;
import java.sql.*;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

public class SQLiteStorage {
    private final File databaseFile;
    private Connection connection;
    private final JSONParser parser = new JSONParser();

    public SQLiteStorage(File dataFolder) {
        this.databaseFile = new File(dataFolder, "backpack.db");
    }

    public void initialize(File legacyDataFile) {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            createTables();
            migrateFromYamlIfNeeded(legacyDataFile);
        } catch (SQLException e) {
            Backpack.getInstance().getLogger().severe("初始化SQLite失败: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS player_backpacks (" +
                    "player_uuid TEXT NOT NULL," +
                    "backpack_slot INTEGER NOT NULL," +
                    "items_json TEXT," +
                    "enabled_skills_json TEXT," +
                    "PRIMARY KEY(player_uuid, backpack_slot))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS item_matches (" +
                    "item_name TEXT PRIMARY KEY," +
                    "items_json TEXT NOT NULL)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS armor_sets (" +
                    "set_name TEXT PRIMARY KEY," +
                    "items_json TEXT NOT NULL)");
        }
    }

    private void migrateFromYamlIfNeeded(File legacyDataFile) {
        if (!legacyDataFile.exists() || hasPlayerBackpackData()) {
            return;
        }
        FileConfiguration legacy = YamlConfiguration.loadConfiguration(legacyDataFile);
        migrateGlobalEntries(legacy);
        migratePlayerEntries(legacy);
    }

    private boolean hasPlayerBackpackData() {
        String sql = "SELECT COUNT(1) FROM player_backpacks";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private void migrateGlobalEntries(FileConfiguration legacy) {
        ConfigurationSection itemMatch = legacy.getConfigurationSection("物品匹配");
        if (itemMatch != null) {
            for (String key : itemMatch.getKeys(false)) {
                setItemMatch(key, convertToStringObjectMapList(itemMatch.getMapList(key)));
            }
        }

        ConfigurationSection armorSets = legacy.getConfigurationSection("盔甲套装");
        if (armorSets != null) {
            for (String key : armorSets.getKeys(false)) {
                setArmorSet(key, convertToStringObjectMapList(armorSets.getMapList(key)));
            }
        }
    }

    private void migratePlayerEntries(FileConfiguration legacy) {
        Set<String> ignoredKeys = Set.of("物品匹配", "盔甲套装");
        for (String topLevelKey : legacy.getKeys(false)) {
            if (ignoredKeys.contains(topLevelKey)) {
                continue;
            }
            UUID uuid = resolvePlayerUuid(topLevelKey);
            if (uuid == null) {
                continue;
            }
            ConfigurationSection section = legacy.getConfigurationSection(topLevelKey);
            if (section == null) {
                continue;
            }
            for (String backpackKey : section.getKeys(false)) {
                if (!backpackKey.startsWith("Backpack")) {
                    continue;
                }
                int slot;
                try {
                    slot = Integer.parseInt(backpackKey.replace("Backpack", ""));
                } catch (NumberFormatException e) {
                    continue;
                }
                String root = topLevelKey + "." + backpackKey;
                List<Map<String, Object>> items = convertToStringObjectMapList(legacy.getMapList(root + ".items"));
                List<Integer> skills = legacy.getIntegerList(root + ".EnabledSkill");
                upsertBackpack(uuid, slot, items, skills);
            }
        }
    }

    private UUID resolvePlayerUuid(String nameOrUuid) {
        try {
            return UUID.fromString(nameOrUuid);
        } catch (IllegalArgumentException ignored) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(nameOrUuid);
            return offlinePlayer.getUniqueId();
        }
    }

    public synchronized void initializePlayerBackpacks(UUID playerUuid, int backpackAmount) {
        for (int i = 1; i <= backpackAmount; i++) {
            String sql = "INSERT OR IGNORE INTO player_backpacks(player_uuid, backpack_slot, items_json, enabled_skills_json) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, playerUuid.toString());
                ps.setInt(2, i);
                ps.setString(3, "[]");
                ps.setString(4, "[]");
                ps.executeUpdate();
            } catch (SQLException e) {
                Backpack.getInstance().getLogger().warning("初始化玩家背包失败: " + e.getMessage());
            }
        }
    }

    public synchronized List<Map<String, Object>> getBackpackItems(UUID playerUuid, int backpackSlot) {
        String sql = "SELECT items_json FROM player_backpacks WHERE player_uuid=? AND backpack_slot=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.setInt(2, backpackSlot);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return parseItemList(rs.getString("items_json"));
                }
            }
        } catch (SQLException e) {
            Backpack.getInstance().getLogger().warning("读取背包失败: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public synchronized void setBackpackItems(UUID playerUuid, int backpackSlot, List<Map<String, Object>> items) {
        String sql = "INSERT INTO player_backpacks(player_uuid, backpack_slot, items_json, enabled_skills_json) VALUES (?, ?, ?, ?) " +
                "ON CONFLICT(player_uuid, backpack_slot) DO UPDATE SET items_json=excluded.items_json";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.setInt(2, backpackSlot);
            ps.setString(3, toJson(items));
            ps.setString(4, "[]");
            ps.executeUpdate();
        } catch (SQLException e) {
            Backpack.getInstance().getLogger().warning("保存背包失败: " + e.getMessage());
        }
    }

    public synchronized void clearBackpackItems(UUID playerUuid, int backpackSlot) {
        setBackpackItems(playerUuid, backpackSlot, new ArrayList<>());
    }

    public synchronized List<Integer> getEnabledSkills(UUID playerUuid, int backpackSlot) {
        String sql = "SELECT enabled_skills_json FROM player_backpacks WHERE player_uuid=? AND backpack_slot=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.setInt(2, backpackSlot);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return parseIntegerList(rs.getString("enabled_skills_json"));
                }
            }
        } catch (SQLException e) {
            Backpack.getInstance().getLogger().warning("读取技能失败: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public synchronized void setEnabledSkills(UUID playerUuid, int backpackSlot, List<Integer> skills) {
        String sql = "INSERT INTO player_backpacks(player_uuid, backpack_slot, items_json, enabled_skills_json) VALUES (?, ?, ?, ?) " +
                "ON CONFLICT(player_uuid, backpack_slot) DO UPDATE SET enabled_skills_json=excluded.enabled_skills_json";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, playerUuid.toString());
            ps.setInt(2, backpackSlot);
            ps.setString(3, "[]");
            ps.setString(4, toJson(skills));
            ps.executeUpdate();
        } catch (SQLException e) {
            Backpack.getInstance().getLogger().warning("保存技能失败: " + e.getMessage());
        }
    }

    public synchronized int findFirstNonEmptyBackpack(UUID playerUuid, int backpackAmount) {
        for (int i = 1; i <= backpackAmount; i++) {
            if (!getBackpackItems(playerUuid, i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    public synchronized void setItemMatch(String itemName, List<Map<String, Object>> itemData) {
        String sql = "INSERT INTO item_matches(item_name, items_json) VALUES (?, ?) ON CONFLICT(item_name) DO UPDATE SET items_json=excluded.items_json";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, itemName);
            ps.setString(2, toJson(itemData));
            ps.executeUpdate();
        } catch (SQLException e) {
            Backpack.getInstance().getLogger().warning("保存物品匹配失败: " + e.getMessage());
        }
    }

    public synchronized boolean hasItemMatch(String itemName) {
        String sql = "SELECT 1 FROM item_matches WHERE item_name=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, itemName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public synchronized List<Map<String, Object>> getItemMatch(String itemName) {
        String sql = "SELECT items_json FROM item_matches WHERE item_name=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, itemName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return parseItemList(rs.getString("items_json"));
                }
            }
        } catch (SQLException e) {
            Backpack.getInstance().getLogger().warning("读取物品匹配失败: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public synchronized List<String> getItemMatchKeys() {
        List<String> keys = new ArrayList<>();
        String sql = "SELECT item_name FROM item_matches";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                keys.add(rs.getString("item_name"));
            }
        } catch (SQLException e) {
            Backpack.getInstance().getLogger().warning("读取物品匹配键失败: " + e.getMessage());
        }
        return keys;
    }

    public synchronized void setArmorSet(String setName, List<Map<String, Object>> items) {
        String sql = "INSERT INTO armor_sets(set_name, items_json) VALUES (?, ?) ON CONFLICT(set_name) DO UPDATE SET items_json=excluded.items_json";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, setName);
            ps.setString(2, toJson(items));
            ps.executeUpdate();
        } catch (SQLException e) {
            Backpack.getInstance().getLogger().warning("保存盔甲套装失败: " + e.getMessage());
        }
    }

    public synchronized List<Map<String, Object>> getArmorSet(String setName) {
        String sql = "SELECT items_json FROM armor_sets WHERE set_name=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, setName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return parseItemList(rs.getString("items_json"));
                }
            }
        } catch (SQLException e) {
            Backpack.getInstance().getLogger().warning("读取盔甲套装失败: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }

    private void upsertBackpack(UUID uuid, int slot, List<Map<String, Object>> items, List<Integer> skills) {
        String sql = "INSERT INTO player_backpacks(player_uuid, backpack_slot, items_json, enabled_skills_json) VALUES (?, ?, ?, ?) " +
                "ON CONFLICT(player_uuid, backpack_slot) DO UPDATE SET items_json=excluded.items_json, enabled_skills_json=excluded.enabled_skills_json";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, slot);
            ps.setString(3, toJson(items));
            ps.setString(4, toJson(skills));
            ps.executeUpdate();
        } catch (SQLException e) {
            Backpack.getInstance().getLogger().warning("迁移背包数据失败: " + e.getMessage());
        }
    }

    private List<Map<String, Object>> parseItemList(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            Object value = parser.parse(json);
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
        } catch (Exception ignored) {
        }
        return new ArrayList<>();
    }

    private List<Integer> parseIntegerList(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            Object value = parser.parse(json);
            if (value instanceof JSONArray array) {
                List<Integer> result = new ArrayList<>();
                for (Object o : array) {
                    if (o instanceof Number number) {
                        result.add(number.intValue());
                    }
                }
                return result;
            }
        } catch (Exception ignored) {
        }
        return new ArrayList<>();
    }

    private String toJson(Object value) {
        return org.json.simple.JSONValue.toJSONString(value);
    }

    private List<Map<String, Object>> convertToStringObjectMapList(List<Map<?, ?>> source) {
        List<Map<String, Object>> converted = new ArrayList<>();
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
