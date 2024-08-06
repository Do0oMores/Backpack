package top.mores.backpack;

import org.bukkit.plugin.java.JavaPlugin;
import top.mores.backpack.EventListener.PlayerEventListener;

public final class Backpack extends JavaPlugin {

    public static Backpack instance;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(new PlayerEventListener(),this);
        getLogger().info("Enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled!");
    }

    public static Backpack getInstance(){
        return instance;
    }
}
