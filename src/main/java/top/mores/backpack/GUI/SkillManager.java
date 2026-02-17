package top.mores.backpack.GUI;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import top.mores.backpack.Utils.ChatColorUtil;

import java.util.*;

public class SkillManager {

    private static final Map<Integer, Skill> skills = new LinkedHashMap<>();

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
            skill.setTag(sec.getString("tag"));

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

    public static List<Skill> getPlayerSkills(Player player){
        Set<String> tags = player.getScoreboardTags();
        List<Skill> list = new ArrayList<>();
        for(Skill skill : skills.values()){
            if(tags.contains(skill.getTag())){
                list.add(skill);
            }
        }
        return list;
    }
}
