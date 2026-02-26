package top.mores.backpack.session;

import org.bukkit.inventory.Inventory;

import java.util.UUID;

public class BackpackSession {
    private final UUID playerId;
    private final int backpackSlot;
    private final Inventory inventory;

    private boolean dirty = false;
    private long lastDirtyMillis = 0L;

    public BackpackSession(UUID playerId, int backpackSlot, Inventory inventory) {
        this.playerId = playerId;
        this.backpackSlot = backpackSlot;
        this.inventory = inventory;
    }

    public UUID getPlayerId() { return playerId; }
    public int getBackpackSlot() { return backpackSlot; }
    public Inventory getInventory() { return inventory; }

    public void markDirty() {
        this.dirty = true;
        this.lastDirtyMillis = System.currentTimeMillis();
    }

    public boolean isDirty() { return dirty; }
    public long getLastDirtyMillis() { return lastDirtyMillis; }

    public void clearDirty() { this.dirty = false; }
}
