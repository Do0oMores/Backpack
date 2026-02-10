package top.mores.backpack.Permission;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PermissionOperation {

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
            } else {
                player.sendMessage("没读到权限" + perm);
            }
        }
        return owned;
    }
}
