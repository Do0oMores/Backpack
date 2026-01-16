package top.mores.backpack.GUI;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.mores.backpack.Backpack;
import top.mores.backpack.Utils.ItemStackUtil;
import top.mores.backpack.Utils.MatchUtil;
import top.mores.backpack.Utils.ArmorUtil;
import top.mores.backpack.Utils.MessageUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SingleBackpack {
    MatchUtil matchUtil = new MatchUtil();
    MessageUtil messageUtil=new MessageUtil();

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
                (List<Map<String, Object>>) dataConfig.getList(path, List.<Map<String, Object>>of()) : null;
        return (itemList != null) ? ItemStackUtil.getItemStacksFromConfig(itemList) : new ItemStack[0];
    }

    /**
     * 创建单个背包
     *
     * @param player 玩家
     * @param slot   物品槽
     */
    public void CreateSingleInventory(Player player, int slot) {
        //背包格式：两行物品栏
        Inventory singleInventory = Bukkit.createInventory(player, 18, "§a背包" + slot);
        for (ItemStack item : SingleBackpackItems(player.getName(), slot)) {
            singleInventory.setItem(singleInventory.firstEmpty(), item);
        }
        //第二行GUI
        ItemStack barrier = createGUIItem(Material.BARRIER,
                messageUtil.getOneItemName(),
                messageUtil.getOneItemLore(), true);
        ItemStack whitePane = createGUIItem(Material.WHITE_STAINED_GLASS_PANE,
                messageUtil.getTwoItemName(),
                messageUtil.getTwoItemLore(), false);
        ItemStack redPane = createGUIItem(Material.RED_STAINED_GLASS_PANE,
                messageUtil.getThreeItemName(),
                messageUtil.getThreeItemLore(), true);

        int[] barrierSlots = {9, 10, 11};
        int[] whitePaneSlots = {12, 13, 14};
        int[] redPaneSlots = {15, 16, 17};

        for(int s:barrierSlots){
            singleInventory.setItem(s,barrier.clone());
        }

        for(int s:whitePaneSlots){
            singleInventory.setItem(s,whitePane.clone());
        }

        for (int s:redPaneSlots){
            singleInventory.setItem(s,redPane.clone());
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
        Bukkit.getScheduler().runTaskAsynchronously(Backpack.getInstance(), () -> {
            List<ItemStack> items = List.of(SingleBackpackItems(player.getName(), slot));
            matchUtil.returnItem(items, player);

            Bukkit.getScheduler().runTask(Backpack.getInstance(), () -> {
                player.getInventory().clear();
                Inventory inventory = player.getInventory();
                for (ItemStack item : items) {
                    inventory.setItem(inventory.firstEmpty(), item);
                }
                // 发放默认盔甲套装
                ArmorUtil.equipDefaultArmor(player);
            });
        });
    }

    //创建背包物品
    private ItemStack createGUIItem(Material material,
                                    String name,
                                    String lore,
                                    boolean unbreakable) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(Arrays.asList(lore.split("\n")));
            }

            if (unbreakable) {
                meta.setUnbreakable(true);
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            }

            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }
}