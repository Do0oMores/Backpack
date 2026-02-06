package top.mores.backpack.EventListener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;

public class PlayerEventListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (ProtectionManager.isProtected(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerClickSkillGUI(InventoryClickEvent event){

    }
}
