package top.mores.backpack.Utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.file.FileConfiguration;
import top.mores.backpack.Backpack;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class ArmorUtil {

    /**
     * 保存管理员的盔甲到data.yml（固定名称"default"）
     *
     * @param player 管理员玩家
     */
    public static void saveArmorSet(Player player) {
        ItemStack[] armorContents = player.getInventory().getArmorContents();
        List<Map<String, Object>> serializedArmor = new ArrayList<>();

        for (ItemStack item : armorContents) {
            if (item != null && item.getType().isItem()) {
                serializedArmor.add(ItemStackUtil.getItemStackMap(item));
            }
        }

        FileConfiguration dataConfig = Backpack.getInstance().getDataConfig();
        dataConfig.set("盔甲套装.default", serializedArmor);
        Backpack.getInstance().saveDataFile();

        player.sendMessage("默认盔甲套装已保存！");
    }

    /**
     * 为玩家装备默认盔甲套装
     *
     * @param player 玩家
     */
    public static void equipDefaultArmor(Player player) {
        FileConfiguration dataConfig = Backpack.getInstance().getDataConfig();
        String path = "盔甲套装.default";

        if (dataConfig.contains(path)) {
            List<Map<?, ?>> rawArmorData = dataConfig.getMapList(path);
            List<Map<String, Object>> armorData = new ArrayList<>();
            for (Map<?, ?> map : rawArmorData) {
                Map<String, Object> newMap = new java.util.HashMap<>();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (entry.getKey() instanceof String) {
                        newMap.put((String) entry.getKey(), entry.getValue());
                    }
                }
                armorData.add(newMap);
            }
            ItemStack[] armorItems = ItemStackUtil.getItemStacksFromConfig(armorData);

            ItemStack[] armorContents = new ItemStack[4];
            for (int i = 0; i < Math.min(4, armorItems.length); i++) {
                armorContents[i] = armorItems[i];
            }

            player.getInventory().setArmorContents(armorContents);
        }
    }
}