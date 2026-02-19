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

import java.util.HashSet;
import java.util.Set;

public class SkillGUI {

    MessageUtil messageUtil = new MessageUtil();
    FileUtils fileUtils = new FileUtils();

    public ItemStack buildSkillGUIItem(Player player,
                                       Skill skill,
                                       int bpNumber) {
        Set<String> perms = new HashSet<>(PermissionOperation.getPlayerPermissions(player,
                messageUtil.getPermission()));

        if (skill.getPermission() != null && perms.contains(skill.getPermission())) {
            boolean enabled = checkSkillEnabled(player, skill.getId(), bpNumber);
            Material pane = enabled
                    ? Material.LIME_STAINED_GLASS_PANE
                    : Material.WHITE_STAINED_GLASS_PANE;

            return ItemBuilder.buildItem(pane, skill.getName(), skill.getLore()
            );
        }
        return ItemBuilder.buildItem(
                Material.RED_STAINED_GLASS_PANE,
                skill.getName(),
                skill.getLore()
        );
    }

    //打开技能主GUI
    public void openSkillGUI(Player player, int bpNumber) {
        SkillGUIHolder holder =
                new SkillGUIHolder(player.getUniqueId(), bpNumber);

        Inventory gui = Bukkit.createInventory(holder, 9,
                ChatColorUtil.color(messageUtil.getSkillGUITitle()) + bpNumber);

        int slot = 0;

        for (Skill skill : SkillManager.getSkills()) {
            ItemStack item = buildSkillGUIItem(player, skill, bpNumber);
            gui.setItem(slot, item);
            holder.bindSkill(slot, skill.getId());

            slot++;
        }

        player.openInventory(gui);
    }


    private boolean checkSkillEnabled(Player player,
                                      Integer skillId,
                                      Integer bpNumber) {
        return fileUtils.getEnabledSKillID(player, bpNumber).contains(skillId);
    }
}
