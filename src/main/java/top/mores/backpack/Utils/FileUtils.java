package top.mores.backpack.Utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import top.mores.backpack.Backpack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileUtils {
    FileConfiguration config = Backpack.getInstance().getConfig();
    FileConfiguration data = Backpack.getInstance().getDataConfig();

    /**
     * 获取最大背包数量
     * @return 最大背包数量
     */
    public int getBackpackAmount() {
        return config.getInt("背包数量");
    }

    /**
     * 初始化玩家主背包数据
     *
     * @param player 需要初始化的玩家
     */
    public void initPLayerMainInventoryData(Player player) {
        String playerName = player.getName();
        if (!data.contains(playerName)) {
            int index = getBackpackAmount();
            for (int i = 1; i <= index; i++) {
                data.set(playerName + ".Backpack" + i + ".items", "");
            }
        }
        Backpack.getInstance().saveDataFile();
    }

    private List<String> getEditWorlds() {
        return config.getStringList("可编辑的世界");
    }

    public boolean isInCanEditWorlds(String WorldName) {
        return !getEditWorlds().contains(WorldName);
    }

    private List<String> getSyncWorlds() {
        return config.getStringList("同步背包的世界");
    }

    public boolean isInSyncWorlds(String WorldName) {
        return getSyncWorlds().contains(WorldName);
    }

    public List<String> getDelPlayerInventoryWorld(){
        return config.getStringList("清除背包的世界");
    }

    public boolean getEnableBPLoreLock(){
        return config.getBoolean("背包锁定物品.是否启用");
    }

    public List<String> getBPLoreLockItem(){
        return config.getStringList("背包锁定物品.lore键");
    }

    public boolean getEnableBPLock(){
        return config.getBoolean("背包栏锁定.是否启用");
    }

    public Map<Integer, List<String>> getBPLockItem() {
        Map<Integer, List<String>> loreKeys = new HashMap<>();
        ConfigurationSection section = config.getConfigurationSection("背包栏锁定.lore键");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                int slot = Integer.parseInt(key);
                loreKeys.put(slot, section.getStringList(key));
            }
        }
        return loreKeys;
    }

    public String getBPSaveERROR(){
        return config.getString("背包锁定物品.保存背包失败提示");
    }

    public String getEditBPERROR(){
        return config.getString("不可编辑提示");
    }

    public String getMaxItemsERROR(){
        return config.getString("背包栏锁定.超出最大物品数提示");
    }

    public String getNOMatchItemsERROR(){
        return config.getString("背包栏锁定.不符合背包配置提示");
    }

    public String getNoMatchERROR(){
        return config.getString("背包栏锁定.不符合具体提示");
    }
}