package top.mores.backpack.Utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemBuilder {

    private ItemBuilder() {
    }

    public static ItemStack buildItem(Material material,
                                      String name,
                                      List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(ChatColorUtil.color(name));
        meta.setLore(ChatColorUtil.color(lore));
        item.setItemMeta(meta);
        return item;
    }
}
