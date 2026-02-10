package top.mores.backpack.Permission;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import top.mores.backpack.GUI.SkillManager;
import top.mores.backpack.Utils.ConfigOperation.FileUtils;

import java.util.*;

public class PermissionOperation {
    FileUtils fileUtils = new FileUtils();

    /**
     * 玩家现有匹配的权限节点列表
     *
     * @param player  玩家
     * @param section 配置文件的权限
     * @return 匹配配置文件权限的节点列表
     */
    public static List<String> getPlayerPermissions(Player player,
                                                    ConfigurationSection section) {
        List<String> owned = new ArrayList<>();
        if (section == null) return owned;
        for (String perm : section.getKeys(false)) {
            if (player.hasPermission(perm)) {
                owned.add(perm);
            }
        }
        return owned;
    }

    /**
     * 设置技能
     * @param player 玩家
     * @param skills 技能
     */
    public void setSkillTags(Player player, List<String> skills){
        Set<String> tags = player.getScoreboardTags();
        for(String tag : Perms.ALL){
            if(tags.contains(tag)){
                player.removeScoreboardTag(tag);
            }
        }

        for(String skill : skills){
            player.addScoreboardTag(skill);
        }
    }

    /**
     * 根据背包获取已装备的技能
     * @param player 玩家
     * @param bpNumber 背包
     * @return tags
     */
    public List<String> getPlayerBPTags(Player player, Integer bpNumber){
        List<String> tags=new ArrayList<>();
        for (Integer id:fileUtils.getEnabledSKillID(player, bpNumber)){
            String perm= SkillManager.getSkill(id).getTag();
            if (perm!=null){
                tags.add(perm);
            }
        }
        return tags;
    }
}
