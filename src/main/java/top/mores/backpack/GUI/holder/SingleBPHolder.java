package top.mores.backpack.GUI.holder;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SingleBPHolder implements InventoryHolder {

    private final UUID playerUUID;
    private final int backpackSlot;

    private final Map<Integer, Integer> skillSlotMap = new HashMap<>();

    public SingleBPHolder(UUID playerUUID, int backpackSlot) {
        this.playerUUID = playerUUID;
        this.backpackSlot = backpackSlot;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public int getBackpackSlot() {
        return backpackSlot;
    }

    public void bindSkill(int guiSlot, int skillId) {
        skillSlotMap.put(guiSlot, skillId);
    }

    public Integer getSkill(int guiSlot) {
        return skillSlotMap.get(guiSlot);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }
}
