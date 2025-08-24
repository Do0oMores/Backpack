package top.mores.backpack.Utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import top.mores.backpack.Backpack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileUtils {
    private FileConfiguration getConfig() {
        return Backpack.getInstance().getConfigFile();
    }

    private FileConfiguration getData() {
        return Backpack.getInstance().getDataConfig();
    }

    /**
     * 获取最大背包数量
     * @return 最大背包数量
     */
    public int getBackpackAmount() {
        return getConfig().getInt("背包数量");
    }

    /**
     * 初始化玩家主背包数据
     *
     * @param player 需要初始化的玩家
     */
    public void initPLayerMainInventoryData(Player player) {
        String playerName = player.getName();
        if (!getData().contains(playerName)) {
            int index = getBackpackAmount();
            for (int i = 1; i <= index; i++) {
                getData().set(playerName + ".Backpack" + i + ".items", "");
            }
        }
        Backpack.getInstance().saveDataFile();
    }

    private List<String> getEditWorlds() {
        return getConfig().getStringList("可编辑的世界");
    }

    public boolean isInCanEditWorlds(String WorldName) {
        return getEditWorlds().contains(WorldName);
    }

    private List<String> getSyncWorlds() {
        return getConfig().getStringList("同步背包的世界");
    }

    public boolean isInSyncWorlds(String WorldName) {
        return getSyncWorlds().contains(WorldName);
    }

    public List<String> getDelPlayerInventoryWorld(){
        return getConfig().getStringList("清除背包的世界.世界列表");
    }

    public boolean getEnableBPLoreLock(){
        return getConfig().getBoolean("背包锁定物品.是否启用");
    }

    public List<String> getBPLoreLockItem(){
        return getConfig().getStringList("背包锁定物品.lore键");
    }

    public boolean getEnableBPLock(){
        return getConfig().getBoolean("背包栏锁定.是否启用");
    }

    public Map<Integer, List<String>> getBPLockItem() {
        Map<Integer, List<String>> loreKeys = new HashMap<>();
        ConfigurationSection section = getConfig().getConfigurationSection("背包栏锁定.lore键");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                int slot = Integer.parseInt(key);
                loreKeys.put(slot, section.getStringList(key));
            }
        }
        return loreKeys;
    }

    public String getBPSaveERROR(){
        return getConfig().getString("背包锁定物品.保存背包失败提示");
    }

    public String getEditBPERROR(){
        return getConfig().getString("不可编辑提示");
    }

    public String getMaxItemsERROR(){
        return getConfig().getString("背包栏锁定.超出最大物品数提示");
    }

    public String getNOMatchItemsERROR(){
        return getConfig().getString("背包栏锁定.不符合背包配置提示");
    }

    public String getNoMatchERROR(){
        return getConfig().getString("背包栏锁定.不符合具体提示");
    }

    public String getSyncSuccessTip(){
        return getConfig().getString("背包同步成功提示");
    }

    public String getSaveSuccessTip(){
        return getConfig().getString("背包保存成功提示");
    }

    public boolean getEnableClearInv(){
        return getConfig().getBoolean("清除背包的世界.是否启用");
    }

    public Integer getCloseSyncInvTime(){
        return getConfig().getInt("同步背包自动关闭时间");
    }

    public String getCloseSyncInvTip(){
        return getConfig().getString("同步背包自动选择提示");
    }

    public String getNotAllowedRunCommand(){
        return getConfig().getString("空背包执行命令");
    }

    public String getEmptyBPTip(){
        return getConfig().getString("空背包提示");
    }
}