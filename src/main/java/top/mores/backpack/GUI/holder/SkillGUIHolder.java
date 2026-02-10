package top.mores.backpack.GUI.holder;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SkillGUIHolder implements InventoryHolder {
    private final UUID playerUUID;
    private final int slot;

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }

    public SkillGUIHolder(UUID playerUUID,int slot) {
        this.playerUUID = playerUUID;
        this.slot=slot;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public int getSlot(){
        return slot;
    }
}
