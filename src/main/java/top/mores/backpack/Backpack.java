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
    File configFile;
    File dataFile;

    @Override
    public void onEnable() {
        instance = this;
        MainGUI mainGUI=new MainGUI();
        this.getServer().getPluginManager().registerEvents(new PlayerEventListener(mainGUI), this);
        Objects.requireNonNull(getCommand("bp")).setExecutor(new BackpackCommand());

        //加载config.yml
        loadFile("config.yml");
        reloadConfig();
        //加载data.yml
        loadFile("data.yml");
        reloadData();

        getLogger().info("Enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled!");
    }

    public static Backpack getInstance() {
        return instance;
    }

    private void loadFile(String fileName) {
        File file = new File(getDataFolder(), fileName);
        if (!file.exists()) {
            boolean isCreateDir = file.getParentFile().mkdirs();
            //添加一个文件夹创建判断
            if (!isCreateDir) {
                getLogger().warning("创建" + fileName + "失败");
                return;
            }
            saveResource(fileName, false);
        }
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
