package top.mores.backpack.GUI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import top.mores.backpack.Backpack;
import top.mores.backpack.Utils.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SingleBackpack {

    /**
     * 单个背包物品数组
     *
     * @param playerName 玩家ID
     * @param slot       在主背包中表示的背包槽
     * @return 物品组
     */
    public ItemStack[] SingleBackpack(String playerName, int slot) {
        List<ItemStack> items = new ArrayList<>();
        String path = playerName + ".Backpack" + slot + ".items";
        if (Backpack.getInstance().getDataConfig().contains(path)) {
            List<Map<String, Object>> itemList = (List<Map<String, Object>>) Backpack.getInstance().getDataConfig().getList(path);
            if (itemList != null) {
                return ItemStackUtil.getItemStacksFromConfig(itemList);
            }
        }
        return new ItemStack[0];
    }

    /**
     * 创建单个背包
     *
     * @param player 玩家
     * @param slot   物品槽
     */
    public void CreateSingleInventory(Player player, int slot) {
        Inventory singleInventory = Bukkit.createInventory(player, 9, ChatColor.DARK_PURPLE + "背包" + slot);
        for (ItemStack item : SingleBackpack(player.getName(), slot)) {
            singleInventory.setItem(singleInventory.firstEmpty(), item);
        }
        player.openInventory(singleInventory);
    }
}
