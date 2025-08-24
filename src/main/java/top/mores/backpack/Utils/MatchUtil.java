package top.mores.backpack.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.mores.backpack.Backpack;

import java.util.*;

public class MatchUtil {

    public void saveItemFromHand(Player player) {
        ItemStack itemStack1 = player.getInventory().getItemInMainHand();
        ItemStack itemStack2 = player.getInventory().getItemInOffHand();
        if (itemStack1.getType() == Material.AIR || itemStack2.getType() == Material.AIR) {
            player.sendMessage("绑定的物品为空！");
        } else {
            if (Objects.requireNonNull(itemStack1.getItemMeta()).hasDisplayName()) {
                List<Map<String, Object>> SerializedItem = Collections.singletonList(ItemStackUtil.getItemStackMap(itemStack2));
                String itemName = itemStack1.getItemMeta().getDisplayName();
                Backpack.getInstance().getDataConfig()
                        .set("物品匹配." + itemName, SerializedItem);
                Backpack.getInstance().saveDataFile();
                player.sendMessage("物品" + itemName + "数据已写入成功！");
            } else {
                player.sendMessage("非指定物品！");
            }
        }
    }

    public void returnItem(List<ItemStack> itemStacks, Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(Backpack.getInstance(), () -> {
            List<ItemStack> itemsToReturn = itemStacks.stream()
                    .filter(Objects::nonNull)
                    .limit(2)
                    .map(ItemStack::getItemMeta)
                    .filter(Objects::nonNull)
                    .filter(ItemMeta::hasDisplayName)
                    .map(meta -> {
                        String itemName = meta.getDisplayName();
                        String path = "物品匹配." + itemName;
                        if (Backpack.getInstance().getDataConfig().contains(path)) {
                            return ItemStackUtil.getItemStacksFromConfig(
                                    Backpack.getInstance().getDataConfig().getMapList(path)
                            );
                        } else {
//                            Bukkit.getScheduler().runTask(Backpack.getInstance(), () ->
//                                    player.sendMessage("物品匹配数据未写入！"
//                                            + Backpack.getInstance().getDataConfig().getMapList(path))
//                            );
                            return new ItemStack[0];
                        }
                    })
                    .flatMap(Arrays::stream)
                    .toList();
            if (!itemsToReturn.isEmpty()) {
                Bukkit.getScheduler().runTask(Backpack.getInstance(), () -> {
                    Inventory inventory = player.getInventory();
                    itemsToReturn.forEach(item -> {
                        if (inventory.firstEmpty() == -1) {
                            player.getWorld().dropItemNaturally(player.getLocation(), item);
                        } else {
                            inventory.setItem(inventory.firstEmpty(), item);
                        }
                    });
                });
            }
        });
    }
}
