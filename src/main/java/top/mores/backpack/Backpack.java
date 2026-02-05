package top.mores.backpack;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import top.mores.backpack.Command.BackpackCommand;
import top.mores.backpack.Command.BackpackTabCompleter;
import top.mores.backpack.EventListener.InventoryEventListener;
import top.mores.backpack.GUI.MainGUI;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public final class Backpack extends JavaPlugin {

    private static Backpack instance;
    private FileConfiguration config;
    private FileConfiguration data;
    private FileConfiguration systemData;
    private File configFile;
    private File dataFile;
    private File systemDataFile;

    @Override
    public void onEnable() {
        instance = this;
        initFiles();

        MainGUI mainGUI = new MainGUI();
        this.getServer().getPluginManager().registerEvents(new InventoryEventListener(mainGUI), this);
        Objects.requireNonNull(getCommand("bp")).setExecutor(new BackpackCommand());
        Objects.requireNonNull(getCommand("bp")).setTabCompleter(new BackpackTabCompleter());

        config = getConfigFile();
        getLogger().info("Enabled!");
    }

    @Override
    public void onDisable() {
        saveDataFile();
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

    // 保存 data.yml
    public void saveDataFile() {
        try {
            data.save(dataFile);
        } catch (IOException e) {
            getLogger().severe("保存数据文件出错！" + e.getMessage());
        }
    }

    // 重载 data.yml
    public void reloadData() {
        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    // 获取 data.yml
    public FileConfiguration getDataConfig() {
        if (data == null) {
            reloadData();
        }
        return data;
    }

    private void reloadSystemData() {
        systemData = YamlConfiguration.loadConfiguration(systemDataFile);
    }

    private FileConfiguration getSystemDataConfig() {
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
                if (dataFile.createNewFile()) {
                    getLogger().info("创建data.yml成功");
                }
            } catch (IOException e) {
                getLogger().warning("创建data.yml失败: " + e.getMessage());
            }
        }
        reloadData();

        systemDataFile = new File(getDataFolder(), "systemData.yml");
        if (!systemDataFile.exists()) {
            try {
                if (systemDataFile.createNewFile()) {
                    getLogger().info("创建systemData.yml成功");
                }
            } catch (IOException e) {
                getLogger().warning("创建systemData失败: " + e.getMessage());
            }
        }
        reloadSystemData();
    }
}