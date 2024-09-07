package top.mores.backpack.EventListener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
            player.sendMessage("该世界不可编辑背包！");
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

        // 判断是否是目标背包
        if (!title.contains("§a背包")) {
            return;
        }

        HumanEntity player = event.getPlayer();
        String playerName = player.getName();
        String worldName = player.getWorld().getName();

        // 检查是否在可编辑的世界中
        if (fileUtils.isInCanEditWorlds(worldName)) {
            player.sendMessage("该世界不可编辑背包！");
            return;
        }

        Inventory inventory = event.getInventory();
        int mainAmount = singleBackpack.checkItemLoreContains(inventory, "主武器");
        int secondAmount = singleBackpack.checkItemLoreContains(inventory, "副武器");

        // 提取背包编号
        String backpackNumber = title.substring(title.lastIndexOf("背包") + 2);
        String path = playerName + ".Backpack" + backpackNumber + ".items";

        // 确保有一把主武器和一把副武器
        if (mainAmount == 1 && secondAmount == 1) {
            // 序列化物品
            List<Map<String, Object>> serializedItems = Arrays.stream(inventory.getContents())
                    .filter(Objects::nonNull) // 过滤掉空的物品栏位
                    .map(ItemStackUtil::getItemStackMap)
                    .collect(Collectors.toList());

            // 保存到 data.yml
            Backpack.getInstance().getDataConfig().set(path, serializedItems);
            Backpack.getInstance().saveDataFile();

            player.sendMessage(ChatColor.GREEN + "背包 " + backpackNumber + " 已保存！");
        } else {
            // 将物品返还给玩家并清空背包
            Map<Integer, ItemStack> remainingItems = player.getInventory().addItem(inventory.getContents());
            remainingItems.values().forEach(item ->
                    player.getWorld().dropItemNaturally(player.getLocation(), item)
            );
            inventory.clear();
            Backpack.getInstance().getDataConfig().set(path, null);
            Backpack.getInstance().saveDataFile();

            player.sendMessage(ChatColor.DARK_RED + "背包保存失败，需要有一把主武器和一把副武器，您的物品已返还");
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
}
