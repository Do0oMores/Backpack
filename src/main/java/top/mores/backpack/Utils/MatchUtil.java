package top.mores.backpack.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.mores.backpack.Backpack;

import java.util.*;
import java.util.stream.Stream;

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
                Backpack.getInstance().getStorage().setItemMatch(itemName, SerializedItem);
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
                        if (Backpack.getInstance().getStorage().hasItemMatch(itemName)) {
                            return ItemStackUtil.getItemStacksFromConfig(
                                    Backpack.getInstance().getStorage().getItemMatch(itemName)
                            );
                        } else {
                            return new ItemStack[0];
                        }
                    })
                    .flatMap(Arrays::stream)
                    .flatMap(item -> {
                        if (item.getAmount() <= 127) {
                            return Stream.of(item);
                        } else {
                            List<ItemStack> splitItems = new ArrayList<>();
                            int remaining = item.getAmount();

                            while (remaining > 0) {
                                ItemStack stack = item.clone();
                                int amount = Math.min(remaining, 127);
                                stack.setAmount(amount);
                                splitItems.add(stack);
                                remaining -= amount;
                            }
                            return splitItems.stream();
                        }
                    })
                    .peek(item -> {
                        // 为发放的物品添加背包标识
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                            if (lore == null) lore = new ArrayList<>();
                            if (!lore.contains(ItemStackUtil.BACKPACK_ITEM_LORE)) {
                                lore.add(ItemStackUtil.BACKPACK_ITEM_LORE);
                                meta.setLore(lore);
                                item.setItemMeta(meta);
                            }
                        }
                    })
                    .toList();

            if (!itemsToReturn.isEmpty()) {
                Bukkit.getScheduler().runTask(Backpack.getInstance(), () -> {
                    Inventory inventory = player.getInventory();
                    itemsToReturn.forEach(item -> {
                        int slot = getFirstEmptyInMainInventory(inventory);
                        if (slot == -1) {
                            player.getWorld().dropItemNaturally(player.getLocation(), item);
                        } else {
                            inventory.setItem(slot, item);
                        }
                    });
                });
            }
        });
    }

    private int getFirstEmptyInMainInventory(Inventory inventory) {
        for (int i = 9; i < 36; i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType() == Material.AIR) {
                return i;
            }
        }
        return -1;
    }
}
