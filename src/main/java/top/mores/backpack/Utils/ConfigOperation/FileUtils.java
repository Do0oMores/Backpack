package top.mores.backpack.Utils.ConfigOperation;

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

    /**
     * 获取最大背包数量
     *
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
        Backpack.getInstance().getStorage().initializePlayerBackpacks(player.getUniqueId(), getBackpackAmount());
    }

    /**
     * @return 可编辑的世界
     */
    private List<String> getEditWorlds() {
        return getConfig().getStringList("可编辑的世界");
    }

    public boolean isInCanEditWorlds(String WorldName) {
        return getEditWorlds().contains(WorldName);
    }

    /**
     * @return 同步背包的世界
     */
    private List<String> getSyncWorlds() {
        return getConfig().getStringList("同步背包的世界");
    }

    public boolean isInSyncWorlds(String WorldName) {
        return getSyncWorlds().contains(WorldName);
    }

    /**
     * @return 清除背包的世界.世界列表
     */
    public List<String> getDelPlayerInventoryWorld() {
        return getConfig().getStringList("清除背包的世界.世界列表");
    }

    public boolean getEnableBPLoreLock() {
        return getConfig().getBoolean("背包锁定物品.是否启用");
    }

    public List<String> getBPLoreLockItem() {
        return getConfig().getStringList("背包锁定物品.lore键");
    }

    public boolean getEnableBPLock() {
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

    public String getBPSaveERROR() {
        return getConfig().getString("背包锁定物品.保存背包失败提示");
    }

    public String getEditBPERROR() {
        return getConfig().getString("不可编辑提示");
    }

    public String getMaxItemsERROR() {
        return getConfig().getString("背包栏锁定.超出最大物品数提示");
    }

    public String getNOMatchItemsERROR() {
        return getConfig().getString("背包栏锁定.不符合背包配置提示");
    }

    public String getNoMatchERROR() {
        return getConfig().getString("背包栏锁定.不符合具体提示");
    }

    public String getSyncSuccessTip() {
        return getConfig().getString("背包同步成功提示");
    }

    public String getSaveSuccessTip() {
        return getConfig().getString("背包保存成功提示");
    }

    public boolean getEnableClearInv() {
        return getConfig().getBoolean("清除背包的世界.是否启用");
    }

    /**
     * @return 同步背包自动关闭时间
     */
    public Integer getCloseSyncInvTime() {
        return getConfig().getInt("同步背包自动关闭时间");
    }

    public String getCloseSyncInvTip() {
        return getConfig().getString("同步背包自动选择提示");
    }

    public List<String> getNotAllowedRunCommand() {
        return getConfig().getStringList("控制台空背包执行命令");
    }

    public String getEmptyBPRunCommand() {
        return getConfig().getString("关闭空背包执行命令");
    }

    public String getEmptyBPTip() {
        return getConfig().getString("空背包提示");
    }

    public Integer getSyncTime() {
        return getConfig().getInt("延迟同步背包的时间");
    }

    public List<String> getEmptyCommandToPlayer() {
        return getConfig().getStringList("玩家空背包执行命令");
    }

    public String getEmptyCommand() {
        return getConfig().getString("空背包玩家执行命令");
    }

    public Integer getHarmlessTime() {
        return getConfig().getInt("关闭背包后的无敌时间");
    }

    public String getMainGUITitle() {
        return getConfig().getString("主背包GUI标题");
    }

    public List<Integer> getEnabledSKillID(Player player, Integer i) {
        return Backpack.getInstance().getStorage().getEnabledSkills(player.getUniqueId(), i);
    }

    public String getDisabledSkillTip() {
        return getConfig().getString("关闭技能提示");
    }

    public String getEnabledSkillTip() {
        return getConfig().getString("启用技能提示");
    }

    public String getMaxSkillsTip() {
        return getConfig().getString("最大启用技能提示");
    }
}