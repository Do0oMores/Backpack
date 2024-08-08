package top.mores.backpack.EventListener;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.InventoryView;
import top.mores.backpack.GUI.MainGUI;
import top.mores.backpack.GUI.SingleBackpack;
import top.mores.backpack.Utils.FileUtils;

public class PlayerEventListener implements Listener {
    FileUtils fileUtils = new FileUtils();
    MainGUI mainGUI;
    SingleBackpack singleBackpack=new SingleBackpack();

    public PlayerEventListener(MainGUI mainGUI) {
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
            //判断点的哪个背包
            int slot = event.getSlot();
            if (slot >= 0 && slot <= fileUtils.getBackpackAmount()) {
                singleBackpack.CreateSingleInventory((Player) player, slot + 1);
            } else
                return;
            //处理完逻辑后取消事件
            event.setCancelled(true);
        }
    }
}
