package top.mores.backpack.GUI;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.mores.backpack.Backpack;
import top.mores.backpack.Utils.ItemStackUtil;

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
    public ItemStack[] SingleBackpackItems(String playerName, int slot) {
        String path = playerName + ".Backpack" + slot + ".items";
        FileConfiguration dataConfig = Backpack.getInstance().getDataConfig();

        List<Map<String, Object>> itemList = dataConfig.contains(path) ?
                (List<Map<String, Object>>) dataConfig.getList(path) : null;
        return (itemList != null) ? ItemStackUtil.getItemStacksFromConfig(itemList) : new ItemStack[0];
    }

    /**
     * 创建单个背包
     *
     * @param player 玩家
     * @param slot   物品槽
     */
    public void CreateSingleInventory(Player player, int slot) {
        Inventory singleInventory = Bukkit.createInventory(player, 9, "§a背包" + slot);
        for (ItemStack item : SingleBackpackItems(player.getName(), slot)) {
            singleInventory.setItem(singleInventory.firstEmpty(), item);
        }
        player.openInventory(singleInventory);
    }

    /**
     * 检查容器内有多少物品的lore包含字符串
     *
     * @param inventory 容器
     * @param charValue 字符串
     * @return 数量
     */
    public int checkItemLoreContains(Inventory inventory, String charValue) {
        int amount = 0;
        // 获取物品列表
        ItemStack[] itemList = inventory.getContents();
        // 遍历物品
        for (ItemStack item : itemList) {
            if (item == null) continue; // 跳过空物品

            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta == null || !itemMeta.hasLore()) continue;

            List<String> loreList = itemMeta.getLore();
            if (loreList == null) continue; // 跳过没有lore的物品

            if (loreList.stream().anyMatch(lore -> lore.contains(charValue))) {
                amount++;
            }
        }
        return amount;
    }

    /**
     * 直接将背包内的物品同步到玩家物品栏
     *
     * @param player 玩家
     * @param slot   背包槽
     */
    public void SyncSingleBackpack(Player player, int slot) {
        //先清空玩家背包再进行处理
        player.getInventory().clear();
        Inventory inventory = player.getInventory();
        for (ItemStack item : SingleBackpackItems(player.getName(), slot)) {
            inventory.setItem(inventory.firstEmpty(), item);
        }
    }
}
