package top.mores.backpack;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import top.mores.backpack.Command.BackpackCommand;
import top.mores.backpack.EventListener.PlayerEventListener;
import top.mores.backpack.GUI.MainGUI;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public final class Backpack extends JavaPlugin {

    public static Backpack instance;
    public static FileConfiguration config;
    public static FileConfiguration data;
    private File configFile;
    private File dataFile;

    @Override
    public void onEnable() {
        instance = this;
        MainGUI mainGUI = new MainGUI();
        this.getServer().getPluginManager().registerEvents(new PlayerEventListener(mainGUI), this);
        Objects.requireNonNull(getCommand("bp")).setExecutor(new BackpackCommand());

        //加载config.yml
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            boolean isCreateDir = configFile.getParentFile().mkdir();
            if (!isCreateDir) {
                getLogger().warning("创建config.yml失败");
                return;
            }
            saveResource("config.yml", false);
        }
        reloadConfig();

        dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                getLogger().warning("创建data.yml失败");
            }
        }
        reloadData();
        config = getConfig();
        getLogger().info("Enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled!");
    }

    public static Backpack getInstance() {
        return instance;
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    @Override
    public FileConfiguration getConfig() {
        return config;
    }

    // 保存 data.yml
    public void saveDataFile() {
        try {
            data.save(dataFile);
        } catch (IOException e) {
            getLogger().severe("保存数据文件出错！");
        }
    }

    // 重载 data.yml
    public void reloadData() {
        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    // 获取 data.yml
    public FileConfiguration getDataConfig() {
        return data;
    }
}
