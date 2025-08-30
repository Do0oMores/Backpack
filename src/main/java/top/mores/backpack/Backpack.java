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

    public static Backpack instance;
    public FileConfiguration config;
    public FileConfiguration data;
    private File configFile;
    private File dataFile;

    @Override
    public void onEnable() {
        instance = this;
        initFiles();// 添加这行来自动更新配置文件

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
                dataFile.createNewFile();
            } catch (IOException e) {
                getLogger().warning("创建data.yml失败: " + e.getMessage());
            }
        }
        reloadData();

        File dataFolder = new File(getDataFolder(), "data");
        if (!dataFolder.exists()) {
            boolean isCreateDir = dataFolder.getParentFile().mkdirs();
            if (!isCreateDir) {
                getLogger().warning("创建data文件夹失败");
            }
        }
    }

    // 添加配置文件自动更新方法
//    private void updateConfig() {
//        try {
//            // 读取插件jar中的默认配置文件
//            InputStream defaultConfigStream = getResource("config.yml");
//            if (defaultConfigStream != null) {
//                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream));
//
//                // 检查并更新配置项
//                boolean updated = false;
//
//                // 检查每个默认配置项是否存在
//                for (String key : defaultConfig.getKeys(true)) {
//                    if (!config.contains(key)) {
//                        config.set(key, defaultConfig.get(key));
//                        updated = true;
//                    }
//                }
//
//                // 如果有更新，保存配置文件
//                if (updated) {
//                    config.save(configFile);
//                    getLogger().info("配置文件已自动更新");
//                }
//            }
//        } catch (Exception e) {
//            getLogger().warning("自动更新配置文件时出错: " + e.getMessage());
//        }
//    }
}