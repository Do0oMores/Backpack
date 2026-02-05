package top.mores.backpack.Utils;

import org.bukkit.configuration.file.FileConfiguration;
import top.mores.backpack.Backpack;

import java.util.List;

public class MessageUtil {

    private FileConfiguration getConfig() {
        return Backpack.getInstance().getConfigFile();
    }

    public String getOneItemName() {
        return getConfig().getString("背包GUI.前三个.物品名");
    }

    public List<String> getOneItemLore() {
        return getConfig().getStringList("背包GUI.前三个.lore");
    }

    public String getTwoItemName() {
        return getConfig().getString("背包GUI.中三个.物品名");
    }

    public List<String> getTwoItemLore() {
        return getConfig().getStringList("背包GUI.中三个.lore");
    }

    public String getThreeItemName() {
        return getConfig().getString("背包GUI.后三个.物品名");
    }

    public List<String> getThreeItemLore() {
        return getConfig().getStringList("背包GUI.后三个.lore");
    }

    public String getOtherGUITitle() {
        return getConfig().getString("分背包GUI标题");
    }
}
