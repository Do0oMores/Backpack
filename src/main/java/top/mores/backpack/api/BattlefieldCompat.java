package top.mores.backpack.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.mores.backpack.GUI.SingleBackpack;

import java.util.UUID;

public class BattlefieldCompat {
    private static final SingleBackpack backpack = new SingleBackpack();

    public static void applyBackpack(Player player, int slot) {
        backpack.SyncSingleBackpack(player, slot);
    }

    public static ItemStack[] getBackpackItems(UUID playerUuid, int slot) {
        return backpack.SingleBackpackItems(playerUuid, slot);
    }

    public static ItemStack[] getBackpackItems(Player player, int slot) {
        return backpack.SingleBackpackItems(player.getUniqueId(), slot);
    }
}