package top.mores.backpack.GUI;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import top.mores.backpack.GUI.holder.SkillGUIHolder;
import top.mores.backpack.Permission.PermissionOperation;
import top.mores.backpack.Utils.ChatColorUtil;
import top.mores.backpack.Utils.ConfigOperation.FileUtils;
import top.mores.backpack.Utils.ConfigOperation.MessageUtil;
import top.mores.backpack.Utils.ItemBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SkillGUI {

    MessageUtil messageUtil = new MessageUtil();
    FileUtils fileUtils = new FileUtils();

    /**
     * 构建分背包内的技能GUI物品
     *
     * @param player    玩家
     * @param inventory 分背包
     * @return 技能GUI物品
     */
    public List<ItemStack> SkillGUIItem(Player player, Inventory inventory) {
        Set<String> perms =
                new HashSet<>(PermissionOperation.getPlayerPermissions(player, messageUtil.getPermission()));
        List<ItemStack> items = new ArrayList<>();
        for (Skill skill : SkillManager.getSkills()) {
            ItemStack item;

            if (skill.getPermission() != null &&
                    perms.contains(skill.getPermission())) {
                boolean enabled = checkSkillEnabled(player, skill.getId(), getBPNumber(inventory));
                Material pane = enabled
                        ? Material.LIME_STAINED_GLASS_PANE
                        : Material.WHITE_STAINED_GLASS_PANE;

                item = ItemBuilder.buildItem(
                        pane,
                        skill.getName(),
                        skill.getLore()
                );

            } else {
                item = ItemBuilder.buildItem(
                        Material.RED_STAINED_GLASS_PANE,
                        skill.getName(),
                        skill.getLore()
                );
            }

            items.add(item);
        }

        return items;
    }

    //打开技能主GUI
    public void openSkillGUI(Player player, Inventory inventory) {
        SkillGUIHolder holder = new SkillGUIHolder(player.getUniqueId(), getBPNumber(inventory));
        Inventory gui = Bukkit.createInventory(holder, 9,
                ChatColorUtil.color(messageUtil.getSkillGUITitle()));
        for (ItemStack item : SkillGUIItem(player, inventory)) {
            gui.setItem(gui.firstEmpty(), item);
        }
        player.openInventory(gui);
    }

    private boolean checkSkillEnabled(Player player, Integer skillId, Integer bpNumber) {
        return fileUtils.getEnabledSKillID(player, bpNumber).contains(skillId);
    }

    /**
     * 获取背包编号
     *
     * @param inventory 背包
     * @return 编号，默认会返回1
     */
    private Integer getBPNumber(Inventory inventory) {
        InventoryHolder holder = inventory.getHolder();
        if (holder instanceof SkillGUIHolder) {
            return ((SkillGUIHolder) holder).getSlot();
        }
        return 1;
    }
}
