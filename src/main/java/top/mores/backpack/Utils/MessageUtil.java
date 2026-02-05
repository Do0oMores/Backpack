package top.mores.backpack.Utils;

import org.bukkit.configuration.file.FileConfiguration;
import top.mores.backpack.Backpack;

public class MessageUtil {

    private FileConfiguration getConfig() {
        return Backpack.getInstance().getConfigFile();
    }

    public String getOneItemName() {
        return getConfig().getString("背包GUI.前三个.物品名");
    }

    public String getOneItemLore() {
        return getConfig().getString("背包GUI.前三个.lore");
    }

    public String getTwoItemName() {
        return getConfig().getString("背包GUI.中三个.物品名");
    }

    public String getTwoItemLore() {
        return getConfig().getString("背包GUI.中三个.lore");
    }

    public String getThreeItemName() {
        return getConfig().getString("背包GUI.后三个.物品名");
    }

    public String getThreeItemLore() {
        return getConfig().getString("背包GUI.后三个.lore");
    }
}
