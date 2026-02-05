package top.mores.backpack.EventListener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import top.mores.backpack.Backpack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ProtectionManager {
    private static final Set<UUID> protectPlayers = new HashSet<>();

    public static void protect(Player player, int seconds) {
        protectPlayers.add(player.getUniqueId());

        Bukkit.getScheduler().runTaskLater(Backpack.getInstance(),
                () -> protectPlayers.remove(player.getUniqueId()), seconds * 20L);
    }

    public static boolean isProtected(Player player) {
        return protectPlayers.contains(player.getUniqueId());
    }
}
