package top.mores.backpack.EventListener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
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
            return;
        }
        int slot = event.getSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) {
            event.setCancelled(true);
            return;
        }
        slot +=1;
        // 检查背包编号是否在允许的范围内
        if (slot > fileUtils.getBackpackAmount()) {
            event.setCancelled(true);
            return;
        }

        // 根据玩家所在的世界进行同步或创建
        if (fileUtils.isInSyncWorlds(player.getWorld().getName())) {
            singleBackpack.SyncSingleBackpack((Player) player, slot);
            player.sendMessage(fileUtils.getSyncSuccessTip()
                    .replace("%slot%", String.valueOf(slot)));
            event.setCancelled(true);
            player.closeInventory();
            return;
        }

        // 检查是否在可编辑的世界中
        if (fileUtils.isInCanEditWorlds(player.getWorld().getName())) {
            singleBackpack.CreateSingleInventory((Player) player, slot);
            event.setCancelled(true);
        } else {
            player.sendMessage(fileUtils.getEditBPERROR());
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String worldName = player.getWorld().getName();
        
        // 检查是否在需要清除背包的世界
        if (fileUtils.getEnableClearInv() && fileUtils.getDelPlayerInventoryWorld().contains(worldName)) {
            // 清除玩家背包中的背包物品
            Bukkit.getScheduler().runTaskLater(Backpack.getInstance(), () -> {
                ItemStackUtil.removeBackpackItems(player.getInventory());
            }, 20L); // 延迟1 tick确保死亡事件处理完成
        }
        
        // 检查是否在同步世界中
        if (fileUtils.isInSyncWorlds(worldName)) {
            // 延迟打开背包选择界面
            Bukkit.getScheduler().runTaskLater(Backpack.getInstance(), () -> {
                mainGUI.CreateMainInventory(player);
            }, 20L); // 延迟1秒确保玩家完全死亡并重生
        }
    }

    @EventHandler
    public void onPlayerCloseInventory(InventoryCloseEvent event) {
        InventoryView inventoryView = event.getView();
        String title = inventoryView.getTitle();
        HumanEntity human = event.getPlayer();
        if (!(human instanceof Player player)) return;

        if (fileUtils.isInCanEditWorlds(player.getWorld().getName())) {
            clearTargetLore(player.getInventory());
        }

        player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        if ("§d背包选择".equals(title)) {
            if (checkEmptyInventory(player.getInventory())){
                if (fileUtils.isInSyncWorlds(player.getWorld().getName())) {
                    int firstNonEmptyBackpack = getFirstNonEmptyBackpack(
                            player.getName(), Backpack.getInstance().getDataConfig());
                    if (firstNonEmptyBackpack != -1) {
                        singleBackpack.SyncSingleBackpack(player, firstNonEmptyBackpack);
                        player.sendMessage(fileUtils.getCloseSyncInvTip());
                    } else {
                        String command = fileUtils.getNotAllowedRunCommand();
                        if (command != null && !command.isEmpty()) {
                            Bukkit.dispatchCommand(player, command);
                        }
                    }
                }
            }
        }
        //判断是否是目标背包
        if (!title.contains("§a背包")) {
            return;
        }
        String playerName = player.getName();

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
                player.sendMessage(fileUtils.getSaveSuccessTip()
                        .replace("%number%", backpackNumber));
            } else {
                player.sendMessage(fileUtils.getBPSaveERROR());
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
                        .map(slot -> fileUtils.getNoMatchERROR()
                                .replace("%slot%", String.valueOf(slot + 1))
                                .replace("%lore%", String.join(" / ", loreMap.get(slot))))
                        .collect(Collectors.joining("， "));
                player.sendMessage(fileUtils.getNOMatchItemsERROR() + errorMsg);
                returnInvItems(inventory, player, path);
            }
        }
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        String changeWorldName = event.getFrom().getName();
        Player player = event.getPlayer();
        String NowWorldName = player.getWorld().getName();
        if (fileUtils.getEnableClearInv()) {
            if (fileUtils.getDelPlayerInventoryWorld().contains(changeWorldName)) {
                Bukkit.getScheduler().runTaskLater(Backpack.getInstance(), () -> ItemStackUtil.removeBackpackItems(player.getInventory()), 20L);
            }
        }
        if (fileUtils.isInSyncWorlds(NowWorldName)) {
            int firstNonEmptyBackpack = getFirstNonEmptyBackpack(
                    player.getName(), Backpack.getInstance().getDataConfig());
            if (firstNonEmptyBackpack == -1) {
                String command = fileUtils.getNotAllowedRunCommand();
                if (command != null && !command.isEmpty()) {
                    Bukkit.dispatchCommand(player, command);
                }
                player.sendMessage(fileUtils.getEmptyBPTip());
            } else {
                Bukkit.getScheduler().runTaskLater(Backpack.getInstance(), () -> {
                    mainGUI.CreateMainInventory(player);
                    Bukkit.getScheduler().runTaskLater(Backpack.getInstance(), () -> {
                        if (player.getOpenInventory().getTitle().equals("§d背包选择")) {
                            player.closeInventory();
                        }
                    }, fileUtils.getCloseSyncInvTime() * 20L);
                }, fileUtils.getSyncTime() * 20L);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();
        
        // 检查是否在同步世界中
        if (fileUtils.isInSyncWorlds(worldName)) {
            // 增加延迟打开背包选择界面，确保玩家完全重生
            Bukkit.getScheduler().runTaskLater(Backpack.getInstance(), () -> {
                int firstNonEmptyBackpack = getFirstNonEmptyBackpack(
                        player.getName(), Backpack.getInstance().getDataConfig());
                if (firstNonEmptyBackpack == -1) {
                    String command = fileUtils.getNotAllowedRunCommand();
                    if (command != null && !command.isEmpty()) {
                        Bukkit.dispatchCommand(player, command);
                    }
                    player.sendMessage(fileUtils.getEmptyBPTip());
                } else {
                    mainGUI.CreateMainInventory(player);
                    // 设置自动关闭时间
                    Bukkit.getScheduler().runTaskLater(Backpack.getInstance(), () -> {
                        if (player.getOpenInventory().getTitle().equals("§d背包选择")) {
                            // 自动选择第一个非空背包
                            singleBackpack.SyncSingleBackpack(player, firstNonEmptyBackpack);
                            player.closeInventory();
                            // 发送自动选择提示
                            player.sendMessage(fileUtils.getCloseSyncInvTip());
                        }
                    }, fileUtils.getCloseSyncInvTime() * 20L);
                }
            }, 20L); // 延迟1秒确保玩家完全重生
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

    public int getFirstNonEmptyBackpack(String playerName, FileConfiguration dataConfig) {
        int number = fileUtils.getBackpackAmount();
        for (int i = 1; i <= number; i++) {
            String path = playerName + ".Backpack" + i + ".items";
            Object value = dataConfig.get(path);
    
            if (value == null) {
                continue;
            }
            if (value instanceof String && ((String) value).isEmpty()) {
                continue;
            }
            if (value instanceof List<?> list) {
                if (!list.isEmpty()) {
                    return i;
                }
            }
        }
        return -1;
    }

    private boolean checkEmptyInventory(Inventory inventory) {
        for (int slot = 0; slot < 36; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                return false;
            }
        }
        return true;
    }

    private void clearTargetLore(Inventory inventory) {
        for (int slot = 0; slot < 36; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                ItemMeta meta = item.getItemMeta();
                if (meta!=null&&meta.hasLore()) {
                    List<String> itemLore = meta.getLore();
                    if(itemLore!=null&& itemLore.contains("§b§l背包物品")){
                        itemLore.remove("§b§l背包物品");
                        meta.setLore(itemLore);
                        item.setItemMeta(meta);
                    }
                }
            }
        }
    }
}