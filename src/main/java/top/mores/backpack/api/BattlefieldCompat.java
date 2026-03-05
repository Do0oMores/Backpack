package top.mores.backpack.api;

import org.bukkit.entity.Player;
import top.mores.backpack.GUI.SingleBackpack;

public class BattlefieldCompat {
    private static final SingleBackpack backpack = new SingleBackpack();

    public static void applyBackpack(Player player,int slot) {
        backpack.SyncSingleBackpack(player,slot);
    }
}
