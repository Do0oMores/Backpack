package top.mores.backpack.EventListener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import top.mores.backpack.Utils.FileUtils;

public class PlayerEventListener implements Listener {
    FileUtils fileUtils=new FileUtils();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player= event.getPlayer();
        //初始化玩家主背包数据
        fileUtils.initPLayerMainInventoryData(player);
    }
}
