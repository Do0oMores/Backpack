package top.mores.backpack.GUI.holder;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SingleBPHolder implements InventoryHolder {
    private final UUID playerUUID;

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }

    public SingleBPHolder(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }
}
