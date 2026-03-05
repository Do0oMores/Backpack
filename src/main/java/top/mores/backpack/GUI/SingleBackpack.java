package top.mores.backpack.GUI;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.mores.backpack.Backpack;
import top.mores.backpack.GUI.holder.SingleBPHolder;
import top.mores.backpack.Permission.PermissionOperation;
import top.mores.backpack.Utils.*;
import top.mores.backpack.Utils.ConfigOperation.FileUtils;
import top.mores.backpack.Utils.ConfigOperation.MessageUtil;

import java.util.*;

public class SingleBackpack {
    MatchUtil matchUtil = new MatchUtil();
    MessageUtil messageUtil = new MessageUtil();
    FileUtils fileUtils = new FileUtils();
    PermissionOperation permissionOperation = new PermissionOperation();

    /**
     * 单个背包物品数组
     *
     * @param playerName 玩家ID
     * @param slot       在主背包中表示的背包槽
     * @return 物品组
     */
    public ItemStack[] SingleBackpackItems(UUID playerUuid, int slot) {
        List<Map<String, Object>> itemList = Backpack.getInstance().getStorage().getBackpackItems(playerUuid, slot);
        return ItemStackUtil.getItemStacksFromConfig(itemList);
    }

    /**
     * 创建单个背包
     *
     * @param player 玩家
     * @param slot   物品槽
     */
    public void CreateSingleInventory(Player player, int slot) {
        SingleBPHolder holder = new SingleBPHolder(player.getUniqueId(), slot);
        //背包格式：两行物品栏
        Inventory singleInventory = Bukkit.createInventory(holder, 18,
                ChatColorUtil.color(messageUtil.getOtherGUITitle()) + slot);
        for (ItemStack item : SingleBackpackItems(player.getUniqueId(), slot)) {
            singleInventory.setItem(singleInventory.firstEmpty(), item);
        }
        //第二行GUI
        ItemStack barrier = createGUIItem(Material.STRUCTURE_VOID,
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

        List<ItemStack> skillItems = buildEnabledSkillItems(player, slot);
        if (skillItems.isEmpty()) {
            for (int s : barrierSlots) {
                singleInventory.setItem(s, barrier.clone());
            }
        } else {
            for (int i = 0; i < barrierSlots.length; i++) {
                if (i < skillItems.size()) {
                    singleInventory.setItem(barrierSlots[i], skillItems.get(i));
                } else {
                    singleInventory.setItem(barrierSlots[i],
                            barrier.clone());
                }
            }
        }

        for (int s : whitePaneSlots) {
            singleInventory.setItem(s, whitePane.clone());
        }

        for (int s : redPaneSlots) {
            singleInventory.setItem(s, redPane.clone());
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
            List<ItemStack> items = List.of(SingleBackpackItems(player.getUniqueId(), slot));
            matchUtil.returnItem(items, player);
            permissionOperation.setSkillTags(player, permissionOperation.getPlayerBPTags(player, slot));
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
                                    List<String> lore,
                                    boolean unbreakable) {

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;
        meta.setDisplayName(ChatColorUtil.color(name));

        if (lore != null && !lore.isEmpty()) {
            meta.setLore(
                    lore.stream()
                            .map(ChatColorUtil::color)
                            .toList()
            );
        }
        if (unbreakable) {
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        item.setItemMeta(meta);
        return item;
    }

    private List<ItemStack> buildEnabledSkillItems(Player player, int bpNumber) {
        List<Integer> enabled = fileUtils.getEnabledSKillID(player, bpNumber);
        List<ItemStack> items = new ArrayList<>();
        for (Integer id : enabled) {
            Skill skill = SkillManager.getSkill(id);
            if (skill == null) continue;
            ItemStack item = ItemBuilder.buildItem(
                    skill.getIcon(),
                    skill.getName(),
                    skill.getLore()
            );
            items.add(item);
        }
        return items;
    }
}