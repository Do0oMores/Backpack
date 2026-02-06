package top.mores.backpack.GUI;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import top.mores.backpack.GUI.holder.SkillGUIHolder;
import top.mores.backpack.Utils.ChatColorUtil;
import top.mores.backpack.Utils.ConfigOperation.MessageUtil;
import top.mores.backpack.Utils.ItemBuilder;

import java.util.List;

public class SkillGUI {

    MessageUtil messageUtil = new MessageUtil();

//    public List<ItemStack> SkillGUIItem(){
//
//    }

    public void openSkillGUI(Player player) {
        SkillGUIHolder holder = new SkillGUIHolder(player.getUniqueId());
        Inventory gui = Bukkit.createInventory(holder, 9,
                ChatColorUtil.color(messageUtil.getSkillGUITitle()));

        player.openInventory(gui);
    }
}
