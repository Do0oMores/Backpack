package top.mores.backpack.GUI;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import top.mores.backpack.Utils.ChatColorUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SkillManager {

    private static final Map<Integer, Skill> skills = new HashMap<>();

    public static void loadSkills(ConfigurationSection section,
                                  ConfigurationSection permSection) {
        skills.clear();
        for (String key : section.getKeys(false)) {

            int id = Integer.parseInt(key);
            ConfigurationSection sec = section.getConfigurationSection(key);

            Skill skill = new Skill();
            skill.setId(id);
            skill.setName(ChatColorUtil.color(sec.getString("name")));
            skill.setLore(ChatColorUtil.color(sec.getStringList("lore")));
            skill.setIcon(Material.matchMaterial(sec.getString("item", "STONE")));

            String perm = permSection.getKeys(false)
                    .stream()
                    .filter(p -> permSection.getInt(p) == id)
                    .findFirst()
                    .orElse(null);

            skill.setPermission(perm);

            skills.put(id, skill);
        }
    }

    public static Skill getSkill(int id) {
        return skills.get(id);
    }

    public static Collection<Skill> getSkills() {
        return skills.values();
    }
}
