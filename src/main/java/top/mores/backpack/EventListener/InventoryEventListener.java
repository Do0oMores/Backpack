package top.mores.backpack.EventListener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.mores.backpack.Backpack;
import top.mores.backpack.GUI.SkillGUI;
import top.mores.backpack.GUI.SkillManager;
import top.mores.backpack.GUI.holder.MainBPHolder;
import top.mores.backpack.GUI.holder.SingleBPHolder;
import top.mores.backpack.GUI.MainGUI;
import top.mores.backpack.GUI.SingleBackpack;
import top.mores.backpack.GUI.holder.SkillGUIHolder;
import top.mores.backpack.Utils.ConfigOperation.FileUtils;
import top.mores.backpack.Utils.ItemStackUtil;
import top.mores.backpack.session.BackpackSession;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InventoryEventListener implements Listener {

    FileUtils fileUtils = new FileUtils();
    MainGUI mainGUI;
    SingleBackpack singleBackpack = new SingleBackpack();
    SkillGUI skillGUI = new SkillGUI();
    SkillManager skillManager = new SkillManager();

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
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory inventory = event.getView().getTopInventory();
        if (inventory.getHolder() instanceof SkillGUIHolder) return;
        Inventory clicked = event.getClickedInventory();
        InventoryHolder holder = inventory.getHolder();
        if (clicked == null) return;
        if (event.getClickedInventory() != inventory) return;

        // 判断是否是创建的背包
        if (!(holder instanceof SingleBPHolder) &&
                !clicked.equals(inventory) &&
                !(holder instanceof MainBPHolder)) {
            return;
        }

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getView().getTopInventory().getSize()) {
            return;
        }
        slot += 1;

        // 检查背包编号是否在允许的范围内
        if (holder instanceof MainBPHolder) {
            if (slot > fileUtils.getBackpackAmount()) {
                return;
            }
        }

        // 根据玩家所在的世界进行同步或创建
        if (fileUtils.isInSyncWorlds(player.getWorld().getName()) &&
                holder instanceof MainBPHolder) {
            singleBackpack.SyncSingleBackpack(player, slot);
            skillManager.setSkills(player, slot);
            player.sendMessage(fileUtils.getSyncSuccessTip()
                    .replace("%slot%", String.valueOf(slot)));
            event.setCancelled(true);
            Bukkit.getScheduler().runTaskLater(Backpack.getInstance(), player::closeInventory, 10L);
            return;
        }

        // 检查是否在可编辑的世界中
        if (fileUtils.isInCanEditWorlds(player.getWorld().getName()) &&
                holder instanceof MainBPHolder) {
            singleBackpack.CreateSingleInventory(player, slot);
            event.setCancelled(true);
        }

        if (holder instanceof SingleBPHolder singleBPHolder) {
            if (slot >= 9 && slot <= 18) {
                ItemStack clickItem = event.getCurrentItem();

                if (clickItem != null &&
                        clickItem.getType() == Material.WHITE_STAINED_GLASS_PANE) {
                    skillGUI.openSkillGUI(player, singleBPHolder.getBackpackSlot());
                    event.setCancelled(true);
                }

                if (hasLockLore(clickItem)) {
                    event.setCancelled(true);
                    return;
                }

                ItemStack cursorItem = event.getCursor();
                if (hasLockLore(cursorItem)) {
                    event.setCancelled(true);
                    return;
                }

                // 移动到其他背包的操作
                if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    ItemStack movedItem = event.getCurrentItem();
                    if (hasLockLore(movedItem)) {
                        event.setCancelled(true);
                        return;
                    }
                }

                // 快捷栏交换的操作
                if (event.getAction() == InventoryAction.SWAP_WITH_CURSOR) {
                    ItemStack hotbarItem = null;
                    if (event.getHotbarButton() >= 0) {
                        hotbarItem = player.getInventory().getItem(event.getHotbarButton());
                    }
                    if ((hasLockLore(clickItem)) || (hasLockLore(hotbarItem))) {
                        event.setCancelled(true);
                        return;
                    }
                }

                // 快捷栏移动操作
                if (event.getAction() == InventoryAction.HOTBAR_SWAP ||
                        event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD) {
                    int hotbarSlot = event.getHotbarButton();
                    if (hotbarSlot >= 0) {
                        ItemStack hotbarItem = player.getInventory().getItem(hotbarSlot);
                        if (hasLockLore(hotbarItem)) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String worldName = player.getWorld().getName();

        // 检查是否在需要清除背包的世界
        if (fileUtils.getEnableClearInv() && fileUtils.getDelPlayerInventoryWorld().contains(worldName)) {
            // 清除玩家背包中的背包物品
            Bukkit.getScheduler().runTaskLater(Backpack.getInstance(), () ->
                    ItemStackUtil.removeBackpackItems(player.getInventory()), 20L); // 延迟1 秒确保死亡事件处理完成
        }

        // 检查是否在同步世界中
        if (fileUtils.isInSyncWorlds(worldName)) {
            // 延迟打开背包选择界面
            Bukkit.getScheduler().runTaskLater(Backpack.getInstance(),
                    () -> mainGUI.CreateMainInventory(player), 2L); // 延迟1tick
        }
    }

    @EventHandler
    public void onPlayerCloseInventory(InventoryCloseEvent event) {
        InventoryView inventoryView = event.getView();
        String title = inventoryView.getTitle();
        HumanEntity human = event.getPlayer();
        Inventory topInventory = event.getView().getTopInventory();

        if (!(human instanceof Player player)) return;

        if (fileUtils.isInCanEditWorlds(player.getWorld().getName())) {
            clearTargetLore(player.getInventory());
        }

        ProtectionManager.protect(player, fileUtils.getHarmlessTime());
        if (topInventory.getHolder() instanceof MainBPHolder) {
            if (checkEmptyInventory(player.getInventory())) {
                if (fileUtils.isInSyncWorlds(player.getWorld().getName())) {
                    int firstNonEmptyBackpack = getFirstNonEmptyBackpack(
                            player.getName(), Backpack.getInstance().getDataConfig());
                    if (firstNonEmptyBackpack != -1) {
                        singleBackpack.SyncSingleBackpack(player, firstNonEmptyBackpack);
                        player.sendMessage(fileUtils.getCloseSyncInvTip());
                    } else {
                        //关闭空背包执行命令
                        String command = fileUtils.getEmptyBPRunCommand()
                                .replace("%player%", player.getName());
                        if (!command.isEmpty()) {
                            Bukkit.dispatchCommand(player, command);
                        }
                    }
                }
            }
        }
        //判断是否是目标背包
        if (!(topInventory.getHolder() instanceof SingleBPHolder)) {
            return;
        }
        String playerName = player.getName();

        int mainAmount = singleBackpack.checkItemLoreContains(topInventory, fileUtils.getBPLoreLockItem().get(0));
        int secondAmount = singleBackpack.checkItemLoreContains(topInventory, fileUtils.getBPLoreLockItem().get(1));
        String backpackNumber = title.substring(title.lastIndexOf("背包") + 2);
        String path = playerName + ".Backpack" + backpackNumber + ".items";

        if (fileUtils.getEnableBPLoreLock()) {
            if (mainAmount == 1 && secondAmount == 1) {
                List<Map<String, Object>> serializedItems = getInvItems(topInventory);
                Backpack.getInstance().getDataConfig().set(path, serializedItems);
                Backpack.getInstance().saveDataFile();
                player.sendMessage(fileUtils.getSaveSuccessTip()
                        .replace("%number%", backpackNumber));
            } else {
                player.sendMessage(fileUtils.getBPSaveERROR());
                returnInvItems(topInventory, player, path);
            }
        }
        if (fileUtils.getEnableBPLock()) {
            Map<Integer, List<String>> loreMap = fileUtils.getBPLockItem();
            List<Map<String, Object>> invItems = getInvItems(topInventory);

            if (invItems.size() > loreMap.size()) {
                player.sendMessage(fileUtils.getMaxItemsERROR() + loreMap.size());
                returnInvItems(topInventory, player, path);
                return;
            }
            List<Integer> invalidSlots = getInvalidSlots(topInventory, loreMap);
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
                returnInvItems(topInventory, player, path);
            }
        }
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String NowWorldName = player.getWorld().getName();
        if (fileUtils.getEnableClearInv()) {
            if (fileUtils.getDelPlayerInventoryWorld().contains(NowWorldName)) {
                Bukkit.getScheduler().runTaskLater(Backpack.getInstance(), () ->
                        ItemStackUtil.removeBackpackItems(player.getInventory()), 20L);
            }
        }
        if (fileUtils.isInSyncWorlds(NowWorldName)) {
            World nowWorld = Bukkit.getWorld(NowWorldName);
            //解析世界中所有玩家
            List<Player> onWorldPlayers = null;
            if (nowWorld != null) {
                onWorldPlayers = nowWorld.getPlayers();
            }
            int firstNonEmptyBackpack = getFirstNonEmptyBackpack(
                    player.getName(), Backpack.getInstance().getDataConfig());
            if (firstNonEmptyBackpack == -1) {
                List<String> commandsConsole = fileUtils.getNotAllowedRunCommand();
                List<String> commandsPlayer = fileUtils.getEmptyCommandToPlayer();
                if (commandsConsole == null || commandsConsole.isEmpty() || onWorldPlayers == null) {
                    return;
                }
                String emptyPlayerName = player.getName();
                String selfCommand = fileUtils.getEmptyCommand()
                        .replace("%EmptyPlayer%", emptyPlayerName);
                for (Player target : onWorldPlayers) {
                    String targetName = target.getName();
                    // 控制台命令
                    for (String cmd : commandsConsole) {
                        Bukkit.dispatchCommand(
                                Bukkit.getConsoleSender(),
                                cmd.replace("%player%", targetName)
                                        .replace("%EmptyPlayer%", emptyPlayerName)
                        );
                    }
                    // 执行玩家命令
                    if (target.getUniqueId().equals(player.getUniqueId())) {
                        Bukkit.dispatchCommand(player, selfCommand);
                        continue;
                    }
                    if (commandsPlayer == null || commandsPlayer.isEmpty()) {
                        continue;
                    }
                    for (String cmd : commandsPlayer) {
                        Bukkit.dispatchCommand(
                                target,
                                cmd.replace("%player%", targetName)
                                        .replace("%EmptyPlayer%", emptyPlayerName)
                        );
                    }
                }
                return;
            }
            Bukkit.getScheduler().runTaskLater(Backpack.getInstance(), () -> {
                mainGUI.CreateMainInventory(player);
                Bukkit.getScheduler().runTaskLater(
                        Backpack.getInstance(),
                        () -> {
                            if (player.getOpenInventory().getTopInventory() instanceof MainBPHolder) {
                                player.closeInventory();
                            }
                        },
                        fileUtils.getCloseSyncInvTime() * 20L
                );
            }, fileUtils.getSyncTime() * 20L);
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
                    String command = fileUtils.getEmptyBPRunCommand()
                            .replace("%player%", player.getName());
                    if (!command.isEmpty()) {
                        Bukkit.dispatchCommand(player, command);
                    }
                    player.sendMessage(fileUtils.getEmptyBPTip());
                } else {
                    mainGUI.CreateMainInventory(player);
                    // 设置自动关闭时间
                    Bukkit.getScheduler().runTaskLater(Backpack.getInstance(), () -> {
                        if (player.getOpenInventory().getTopInventory() instanceof MainBPHolder) {
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

    public void returnInvItems(Inventory inventory, Player player, String path) {
        for (int i = 0; i < 9; i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType() == Material.AIR)
                continue;
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);

            leftover.values().forEach(drop -> player.getWorld().dropItemNaturally(
                    player.getLocation(), drop));
            inventory.setItem(i, null);
        }
        Backpack.getInstance().getDataConfig().set(path, null);
        Backpack.getInstance().saveDataFile();
    }

    //只取第一行物品
    public List<Map<String, Object>> getInvItems(Inventory inventory) {
        return IntStream.range(0, Math.min(9, inventory.getSize()))
                .mapToObj(inventory::getItem)
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
                if (meta != null && meta.hasLore()) {
                    List<String> itemLore = meta.getLore();
                    if (itemLore != null && itemLore.contains("§b§l背包物品")) {
                        itemLore.remove("§b§l背包物品");
                        meta.setLore(itemLore);
                        item.setItemMeta(meta);
                    }
                }
            }
        }
    }

    // 检查物品lore是否包含lock
    private boolean hasLockLore(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        if (item.getType().equals(Material.AIR)) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) {
            return false;
        }

        List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty()) {
            return false;
        }

        for (String line : lore) {
            if (ChatColor.stripColor(line).toLowerCase().contains("lock")) {
                return true;
            }
        }
        return false;
    }

    public void flushSession(BackpackSession session) {
        if (session == null) return;
        UUID uuid = session.getPlayerId();
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        String name = player.getName();
        int slot = session.getBackpackSlot();
        Inventory inventory = session.getInventory();
        Backpack.getInstance().getDataConfig().set(name + ".Backpack" + slot + ".Items",
                ItemStackUtil.serializeItemStacks(inventory.getContents()));
        Backpack.getInstance().saveDataFile();
        session.clearDirty();
    }
}