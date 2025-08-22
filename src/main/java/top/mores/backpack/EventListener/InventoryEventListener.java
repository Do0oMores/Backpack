package top.mores.backpack.EventListener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.mores.backpack.Backpack;
import top.mores.backpack.GUI.MainGUI;
import top.mores.backpack.GUI.SingleBackpack;
import top.mores.backpack.Utils.FileUtils;
import top.mores.backpack.Utils.ItemStackUtil;

import java.util.*;
import java.util.stream.Collectors;

public class InventoryEventListener implements Listener {

    FileUtils fileUtils = new FileUtils();
    MainGUI mainGUI;
    SingleBackpack singleBackpack = new SingleBackpack();

    public InventoryEventListener(MainGUI mainGUI) {
        this.mainGUI = mainGUI;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        //初始化玩家主背包数据
        fileUtils.initPLayerMainInventoryData(player);
    }

    @EventHandler
    public void onPlayerClickInventory(InventoryClickEvent event) {
        InventoryView inventoryView = event.getView();
        HumanEntity player = event.getWhoClicked();
        String inventoryTitle = inventoryView.getTitle();

        // 判断是否是创建的背包
        if (!"§d背包选择".equals(inventoryTitle)) {
            return; // 不是目标背包，直接返回
        }

        // 检查是否在可编辑的世界中
        if (fileUtils.isInCanEditWorlds(player.getWorld().getName())) {
            player.sendMessage(fileUtils.getEditBPERROR());
            event.setCancelled(true);
            return;
        }

        // 获取点击的背包编号
        int slot = event.getSlot() + 1;

        // 检查背包编号是否在允许的范围内
        if (slot > fileUtils.getBackpackAmount()) {
            event.setCancelled(true);
            return;
        }

        // 根据玩家所在的世界进行同步或创建
        if (fileUtils.isInSyncWorlds(player.getWorld().getName())) {
            singleBackpack.SyncSingleBackpack((Player) player, slot);
            player.sendMessage("背包" + slot + "已同步");
        } else {
            singleBackpack.CreateSingleInventory((Player) player, slot);
        }

        // 处理完逻辑后取消事件
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerCloseInventory(InventoryCloseEvent event) {
        InventoryView inventoryView = event.getView();
        String title = inventoryView.getTitle();

        //判断是否是目标背包
        if (!title.contains("§a背包")) {
            return;
        }

        HumanEntity player = event.getPlayer();
        String playerName = player.getName();
        String worldName = player.getWorld().getName();

        //检查是否在可编辑的世界中
        if (fileUtils.isInCanEditWorlds(worldName)) {
            player.sendMessage(fileUtils.getEditBPERROR());
            return;
        }

        Inventory inventory = event.getInventory();
        int mainAmount = singleBackpack.checkItemLoreContains(inventory, fileUtils.getBPLoreLockItem().get(0));
        int secondAmount = singleBackpack.checkItemLoreContains(inventory, fileUtils.getBPLoreLockItem().get(1));
        String backpackNumber = title.substring(title.lastIndexOf("背包") + 2);
        String path = playerName + ".Backpack" + backpackNumber + ".items";

        if (fileUtils.getEnableBPLoreLock()) {
            if (mainAmount == 1 && secondAmount == 1) {
                List<Map<String, Object>> serializedItems = getInvItems(inventory);
                Backpack.getInstance().getDataConfig().set(path, serializedItems);
                Backpack.getInstance().saveDataFile();
                player.sendMessage(ChatColor.GREEN + "背包 " + backpackNumber + " 已保存！");
            } else {
                returnInvItems(inventory, player, path);
            }
        }
        if (fileUtils.getEnableBPLock()) {
            Map<Integer, List<String>> loreMap = fileUtils.getBPLockItem();
            List<Map<String, Object>> invItems = getInvItems(inventory);

            if (invItems.size() > loreMap.size()) {
                player.sendMessage(fileUtils.getMaxItemsERROR() + loreMap.size());
                returnInvItems(inventory, player, path);
                return;
            }
            List<Integer> invalidSlots = getInvalidSlots(inventory, loreMap);
            if (invalidSlots.isEmpty()) {
                Backpack.getInstance().getDataConfig().set(path, invItems);
                Backpack.getInstance().saveDataFile();
                player.sendMessage(ChatColor.GREEN + "背包 " + backpackNumber + " 已保存！");
            } else {
                String errorMsg = invalidSlots.stream()
                        .map(slot -> "第" + (slot + 1) + "格需要: " + String.join(" / ", loreMap.get(slot)))
                        .collect(Collectors.joining("， "));
                player.sendMessage(fileUtils.getNOMatchItemsERROR() + " → " + errorMsg);
                returnInvItems(inventory, player, path);
            }
        }
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        String changeWorldName = event.getFrom().getName();
        if (fileUtils.getDelPlayerInventoryWorld().contains(changeWorldName)) {
            Player player = event.getPlayer();
            Bukkit.getScheduler().runTaskLater(Backpack.getInstance(), () -> player.getInventory().clear(), 20L);
        }
    }

    public List<Integer> getInvalidSlots(Inventory inventory, Map<Integer, List<String>> loreMap) {
        List<Integer> invalidSlots = new ArrayList<>();
        for (Map.Entry<Integer, List<String>> entry : loreMap.entrySet()) {
            int slot = entry.getKey();
            List<String> requiredLore = entry.getValue();
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType() == Material.AIR) {
                invalidSlots.add(slot);
                continue;
            }
            ItemMeta meta = item.getItemMeta();
            if (meta == null || !meta.hasLore()) {
                invalidSlots.add(slot);
                continue;
            }
            List<String> itemLore = meta.getLore();
            if (itemLore == null) {
                invalidSlots.add(slot);
                continue;
            }
            boolean match = requiredLore.stream()
                    .anyMatch(req -> itemLore.stream().anyMatch(l -> l.contains(req)));
            if (!match) {
                invalidSlots.add(slot);
            }
        }
        return invalidSlots;
    }

    public void returnInvItems(Inventory inventory, HumanEntity player, String path) {
        ItemStack[] contents = Arrays.stream(inventory.getContents())
                .filter(item -> item != null && item.getType() != Material.AIR)
                .toArray(ItemStack[]::new);
        Map<Integer, ItemStack> remainingItems = player.getInventory().addItem(contents);
        remainingItems.values().forEach(item ->
                player.getWorld().dropItemNaturally(player.getLocation(), item)
        );
        inventory.clear();
        Backpack.getInstance().getDataConfig().set(path, null);
        Backpack.getInstance().saveDataFile();
    }

    public List<Map<String, Object>> getInvItems(Inventory inventory) {
        return Arrays.stream(inventory.getContents())
                .filter(Objects::nonNull)
                .map(ItemStackUtil::getItemStackMap)
                .toList();
    }
}