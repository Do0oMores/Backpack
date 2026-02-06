package top.mores.backpack.Permission;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PermissionOperation {

    public static List<String> getPlayerPermissions(Player player, ConfigurationSection section) {
        List<String> owned = new ArrayList<>();
        if (section == null) return owned;
        for (String perm : section.getKeys(false)) {
            if (player.hasPermission(perm)) {
                owned.add(perm);
            }
        }
        return owned;
    }
}
