package top.mores.backpack.EventListener;

import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
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
        //判断是否是创建的背包
        if (inventoryView.getTitle().equals("§d背包选择")) {
            if (fileUtils.isInCanEditWorlds(player.getWorld().getName())) {
                //判断点的哪个背包
                int slot = event.getSlot() + 1;
                if(slot<= fileUtils.getBackpackAmount()){
                    if (fileUtils.isInSyncWorlds(player.getWorld().getName())) {
                        singleBackpack.SyncSingleBackpack((Player) player, slot);
                        player.sendMessage("背包" + slot + "已同步");
                    } else {
                        singleBackpack.CreateSingleInventory((Player) player, slot);
                    }
                }else {
                    return;
                }
            } else {
                player.sendMessage("该世界不可编辑背包！");
            }
            //处理完逻辑后取消事件
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerCloseInventory(InventoryCloseEvent event) {
        InventoryView inventoryView = event.getView();
        if (inventoryView.getTitle().contains("§a背包")) {
            HumanEntity player = event.getPlayer();
            String playerName = player.getName();
            if (fileUtils.isInCanEditWorlds(player.getWorld().getName())) {
                Inventory inventory = event.getInventory();
                int MainAmount = singleBackpack.checkItemLoreContains(inventory, "主武器");
                int SecondAmount = singleBackpack.checkItemLoreContains(inventory, "副武器");

                //提取背包编号
                String title = inventoryView.getTitle();
                String backpackNumber = title.substring(title.lastIndexOf("背包") + 2);
                String path = playerName + ".Backpack" + backpackNumber + ".items";
                if (MainAmount == 1 && SecondAmount == 1) {

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
                    for (ItemStack item : inventory.getContents()) {
                        if (item != null) {
                            HashMap<Integer, ItemStack> remainingItems = player.getInventory().addItem(item);
                            for (ItemStack remaining : remainingItems.values()) {
                                player.getWorld().dropItemNaturally(player.getLocation(), remaining);
                            }
                        }
                    }
                    inventory.clear();
                    Backpack.getInstance().getDataConfig().set(path, null);
                    Backpack.getInstance().saveDataFile();
                    player.sendMessage(ChatColor.DARK_RED + "背包保存失败，需要有一把主武器和一把副武器，您的物品已返还");
                }
            } else {
                player.sendMessage("该世界不可编辑背包！");
            }
        }
    }
}
