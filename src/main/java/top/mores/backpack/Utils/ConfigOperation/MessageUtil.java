package top.mores.backpack.Utils.ConfigOperation;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import top.mores.backpack.Backpack;

import java.util.List;

public class MessageUtil {

    private FileConfiguration getConfig() {
        return Backpack.getInstance().getConfigFile();
    }

    private FileConfiguration getSystemData() {
        return Backpack.getInstance().getSystemDataConfig();
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

    public String getSkillGUITitle() {
        return getSystemData().getString("技能GUI标题");
    }

    public ConfigurationSection getPermission(){
        return getSystemData().getConfigurationSection("permissions");
    }

    public ConfigurationSection getSKillGUIItem(){
        return getSystemData().getConfigurationSection("技能");
    }
}
