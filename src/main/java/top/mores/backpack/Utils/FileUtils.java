package top.mores.backpack.Utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import top.mores.backpack.Backpack;

public class FileUtils {
    FileConfiguration config = Backpack.getInstance().getConfig();
    FileConfiguration data = Backpack.getInstance().getDataConfig();

    //获取config.yml 背包数量
    public int getBackpackAmount() {
        return config.getInt("背包数量");
    }

    /**
     * 初始化玩家主背包数据
     *
     * @param player 需要初始化的玩家
     */
    public void initPLayerMainInventoryData(Player player) {
        String playerName = player.getName();
        if (!data.contains(playerName)) {
            int index = getBackpackAmount();
            for (int i = 1; i <= index; i++) {
                data.set(playerName + ".Backpack" + i + ".items", "");
            }
        }
        Backpack.getInstance().saveDataFile();
    }
}
