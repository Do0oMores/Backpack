package top.mores.backpack.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemBuilder {

    private ItemBuilder() {
    }

    public static ItemStack fromYaml(ConfigurationSection section) {
        if (section == null) {
            throw new IllegalArgumentException("ConfigSection is null");
        }

        String materialStr = section.getString("material");
        Material material = Material.matchMaterial(materialStr);

        if (material == null) {
            material = Material.STONE;
            Bukkit.getLogger().warning("Invalid material:" + materialStr);
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        if (section.contains("name")) {
            meta.setDisplayName(ChatColorUtil.color(section.getString("name")));
        }
        if (section.contains("lore")) {
            meta.setLore(ChatColorUtil.color(section.getStringList("lore")));
        }
        item.setItemMeta(meta);

        return item;
    }
}
