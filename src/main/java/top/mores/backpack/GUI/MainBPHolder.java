package top.mores.backpack.GUI;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MainBPHolder implements InventoryHolder {
    private final UUID playerUUID;

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }

    public MainBPHolder(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }
}
