package top.mores.backpack.EventListener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import top.mores.backpack.Backpack;
import top.mores.backpack.GUI.SkillGUI;
import top.mores.backpack.GUI.SkillManager;
import top.mores.backpack.GUI.holder.SkillGUIHolder;
import top.mores.backpack.Utils.ChatColorUtil;
import top.mores.backpack.Utils.ConfigOperation.FileUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PlayerEventListener implements Listener {
    FileUtils fileUtils = new FileUtils();
    SkillGUI skillGUI = new SkillGUI();

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (ProtectionManager.isProtected(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerClickSkillGUI(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof SkillGUIHolder holder)) return;
        if (event.getClickedInventory() != top) return;
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (slot >= top.getSize()) return;
        Integer skillID = holder.getSkill(slot);
        if (skillID == null) return;

        Set<Integer> enabled = new HashSet<>(fileUtils.getEnabledSKillID(player, holder.getBackpackSlot()));

        if (enabled.contains(skillID)) {
            enabled.remove(skillID);
            player.sendMessage(ChatColorUtil.color(fileUtils.getDisabledSkillTip()));
        } else {
            if (enabled.size() >= 3) {
                player.sendMessage(ChatColorUtil.color(fileUtils.getMaxSkillsTip()));
                return;
            }
            enabled.add(skillID);
            player.sendMessage(ChatColorUtil.color(fileUtils.getEnabledSkillTip()));
        }
        Backpack.getInstance().getStorage().setEnabledSkills(
                player.getUniqueId(),
                holder.getBackpackSlot(),
                new ArrayList<>(enabled)
        );
        top.setItem(slot, skillGUI.buildSkillGUIItem(
                player,
                SkillManager.getSkill(skillID),
                holder.getBackpackSlot()));
    }
}
