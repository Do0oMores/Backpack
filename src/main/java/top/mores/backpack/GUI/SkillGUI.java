package top.mores.backpack.GUI;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
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

    public List<ItemStack> SkillGUIItem(Player player, Integer bpNumber) {

        Set<String> perms =
                new HashSet<>(PermissionOperation.getPlayerPermissions(player, messageUtil.getPermission()));

        List<ItemStack> items = new ArrayList<>();

        for (Skill skill : SkillManager.getSkills()) {

            ItemStack item;

            if (skill.getPermission() != null &&
                    perms.contains(skill.getPermission())) {

                boolean enabled = checkSkillEnabled(player, skill.getId(), bpNumber);

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

    public void openSkillGUI(Player player, Integer bpNumber) {
        SkillGUIHolder holder = new SkillGUIHolder(player.getUniqueId());
        Inventory gui = Bukkit.createInventory(holder, 9,
                ChatColorUtil.color(messageUtil.getSkillGUITitle()));
        for (ItemStack item : SkillGUIItem(player, bpNumber)) {
            gui.setItem(gui.firstEmpty(), item);
        }
        player.openInventory(gui);
    }

    private boolean checkSkillEnabled(Player player, Integer skillId, Integer bpNumber) {
        return fileUtils.getEnabledSKillID(player, bpNumber).contains(skillId);
    }
}
