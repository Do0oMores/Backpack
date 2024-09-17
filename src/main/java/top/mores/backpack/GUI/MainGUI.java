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
import top.mores.backpack.Utils.ChatColorUtil;
import top.mores.backpack.Utils.FileUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MainGUI {

    FileUtils fileUtils = new FileUtils();
    final String separatorLine = "§7§m§e§m-§e§m-§6§m-§6§m-§8§m---------------------§6§m-§6§m-§e§m-§e§m-§7§m§m";
    final FileConfiguration data = Backpack.getInstance().getDataConfig();

    /**
     * 构建主背包内的物品
     *
     * @param playerName 玩家ID
     * @return 主背包内的物品列表
     */
    public List<ItemStack> MainInventoryItem(String playerName) {
        // 获取背包数量
        int backpackAmount = fileUtils.getBackpackAmount();

        // 如果背包数量为 0，直接返回空列表
        if (backpackAmount == 0) {
            return Collections.emptyList();
        }

        // 创建用于存储背包 ItemStack 的列表
        List<ItemStack> items = new ArrayList<>(backpackAmount);

        // 遍历每个背包
        for (int i = 1; i <= backpackAmount; i++) {
            // 创建一个新的背包物品
            ItemStack itemStack = new ItemStack(Material.CHEST, 1);
            ItemMeta meta = itemStack.getItemMeta();
            assert meta != null;

            // 设置背包的显示名称
            meta.setDisplayName(ChatColor.DARK_AQUA + "背包" + i);

            // 初始化 lore 列表
            List<String> loreList = new ArrayList<>();
            loreList.add(separatorLine);

            // 读取 data.yml 文件中的背包数据
            List<Map<?, ?>> backpackItems = data.getMapList(playerName + ".Backpack" + i + ".items");

            // 如果背包为空，则显示“该背包为空”
            if (backpackItems.isEmpty()) {
                loreList.add(ChatColor.RED + "该背包为空");
            } else {
                // 遍历背包中的物品并解析 display-name
                for (Map<?, ?> itemData : backpackItems) {
                    String displayName = ChatColor.GRAY + "未注册物品";
                    Map<String, Object> metaMap = (Map<String, Object>) itemData.get("meta");

                    if (metaMap != null && metaMap.containsKey("display-name")) {
                        displayName = ChatColorUtil.parseDisplayName((String) metaMap.get("display-name"));
                    }
                    loreList.add(ChatColor.YELLOW + displayName);
                }
            }
            // 设置物品的 lore
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
        player.openInventory(MainInventory);
    }
}