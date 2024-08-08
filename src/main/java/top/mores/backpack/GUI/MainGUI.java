package top.mores.backpack.GUI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.mores.backpack.Backpack;
import top.mores.backpack.Utils.FileUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainGUI {
    FileUtils fileUtils = new FileUtils();
    public Map<String, Inventory> inventoryMap = new HashMap<>();

    public Map<String, Inventory> getInventoryMap() {
        return inventoryMap;
    }

    /**
     * 构建主背包内的物品
     *
     * @param playerName 玩家ID
     * @return 主背包内的物品列表
     */
    public List<ItemStack> MainInventoryItem(String playerName) {
        // 获取背包数量
        int backpackAmount = fileUtils.getBackpackAmount();
        // 创建用于存储背包ItemStack的列表
        List<ItemStack> items = new ArrayList<>();
        // 遍历每个背包
        for (int i = 1; i <= backpackAmount; i++) {
            // 创建一个新的背包物品
            ItemStack itemStack = new ItemStack(Material.CHEST, 1);
            ItemMeta meta = itemStack.getItemMeta();
            assert meta != null;
            // 设置背包的显示名称
            meta.setDisplayName(ChatColor.DARK_AQUA + "背包" + i);
            List<String> loreList = new ArrayList<>();
            loreList.add("§7§m§e§m-§e§m-§6§m-§6§m-§8§m---------------------§6§m-§6§m-§e§m-§e§m-§7§m§m");
            // 读取data.yml文件中的背包数据
            FileConfiguration data = Backpack.getInstance().getDataConfig();
            List<Map<?, ?>> backpackItems = data.getMapList(playerName + ".Backpack" + i + ".items");
            if (backpackItems.isEmpty()) {
                // 如果背包为空，则显示“该背包为空”
                loreList.add(ChatColor.RED + "该背包为空");
            } else {
                // 遍历背包中的物品并解析display-name
                for (Map<?, ?> item : backpackItems) {
                    if (item.containsKey("meta")) {
                        Map<?, ?> metaData = (Map<?, ?>) item.get("meta");
                        if (metaData.containsKey("display-name")) {
                            String displayName = (String) metaData.get("display-name");
                            // 将display-name添加到lore
                            loreList.add(ChatColor.translateAlternateColorCodes('&', displayName));
                        }
                    }
                }
            }
            // 设置物品的lore
            meta.setLore(loreList);
            itemStack.setItemMeta(meta);
            // 将物品添加到背包列表中
            items.add(itemStack);
        }
        // 返回背包列表
        return items;
    }

    /**
     * 创建主背包
     *
     * @param player 玩家ID
     */
    public void CreateMainInventory(Player player) {
        Inventory MainInventory = Bukkit.createInventory(player, 9, "§d背包选择");
        int index = MainInventory.firstEmpty();
        if (index != -1) {
            for (ItemStack item : MainInventoryItem(player.getName())) {
                MainInventory.setItem(MainInventory.firstEmpty(), item);
            }
        }
        inventoryMap.put(player.getName(), MainInventory);
        player.openInventory(MainInventory);
    }
}
