package top.mores.backpack;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import top.mores.backpack.Command.BackpackCommand;
import top.mores.backpack.Command.BackpackTabCompleter;
import top.mores.backpack.EventListener.InventoryEventListener;
import top.mores.backpack.EventListener.PlayerEventListener;
import top.mores.backpack.GUI.MainGUI;
import top.mores.backpack.GUI.SkillManager;
import top.mores.backpack.Utils.ConfigOperation.MessageUtil;

import java.io.File;
import java.util.Objects;

public final class Backpack extends JavaPlugin {

    private static Backpack instance;
    private FileConfiguration config;
    private FileConfiguration systemData;
    private File configFile;
    private File dataFile;
    private top.mores.backpack.storage.SQLiteStorage storage;
    private File systemDataFile;

    @Override
    public void onEnable() {
        instance = this;
        initFiles();
        MainGUI mainGUI = new MainGUI();
        this.getServer().getPluginManager().registerEvents(new PlayerEventListener(), this);
        this.getServer().getPluginManager().registerEvents(new InventoryEventListener(mainGUI), this);
        Objects.requireNonNull(getCommand("bp")).setExecutor(new BackpackCommand());
        Objects.requireNonNull(getCommand("bp")).setTabCompleter(new BackpackTabCompleter());
        config = getConfigFile();
        MessageUtil messageUtil = new MessageUtil();
        SkillManager.loadSkills(messageUtil.getSKillGUIItem(), messageUtil.getPermission());
        getLogger().info("Enabled!");
    }

    @Override
    public void onDisable() {
        if (storage != null) {
            storage.close();
        }
        getLogger().info("Disabled!");
    }

    public static Backpack getInstance() {
        return instance;
    }

    public void reloadConfigFile() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public @NotNull FileConfiguration getConfigFile() {
        if (config == null) {
            reloadConfigFile();
        }
        return config;
    }

    public top.mores.backpack.storage.SQLiteStorage getStorage() {
        return storage;
    }


    public void reloadData() {
        if (storage != null) {
            storage.close();
        }
        storage = new top.mores.backpack.storage.SQLiteStorage(getDataFolder());
        storage.initialize(dataFile);
    }

    public void reloadSystemData() {
        systemData = YamlConfiguration.loadConfiguration(systemDataFile);
    }

    public FileConfiguration getSystemDataConfig() {
        if (systemData == null) {
            reloadSystemData();
        }
        return systemData;
    }

    private void initFiles() {
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            boolean isCreateDir = configFile.getParentFile().mkdirs();
            if (!isCreateDir) {
                getLogger().warning("创建config.yml目录失败");
                return;
            }
            saveResource("config.yml", false);
        }
        reloadConfigFile();

        dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                saveResource("data.yml", false);
                getLogger().info("创建data.yml成功");
            } catch (Exception e) {
                getLogger().warning("创建data.yml失败: " + e.getMessage());
            }
        }
        storage = new top.mores.backpack.storage.SQLiteStorage(getDataFolder());
        storage.initialize(dataFile);

        systemDataFile = new File(getDataFolder(), "systemData.yml");
        if (!systemDataFile.exists()) {
            try {
                saveResource("systemData.yml", false);
                getLogger().info("创建systemData.yml成功");
            } catch (Exception e) {
                getLogger().warning("创建systemData失败: " + e.getMessage());
            }
        }
        reloadSystemData();
    }
}