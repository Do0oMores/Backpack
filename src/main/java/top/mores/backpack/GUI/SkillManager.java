package top.mores.backpack.GUI;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import top.mores.backpack.Permission.PermissionOperation;
import top.mores.backpack.Utils.ChatColorUtil;
import top.mores.backpack.Utils.ConfigOperation.FileUtils;
import top.mores.backpack.Utils.ConfigOperation.MessageUtil;

import java.util.*;

public class SkillManager {

    private static final Map<Integer, Skill> skills = new LinkedHashMap<>();
    PermissionOperation permissionOperation = new PermissionOperation();
    FileUtils fileUtils = new FileUtils();
    MessageUtil messageUtil = new MessageUtil();
    private static final String TAG_PREFIX = "tactical_skill_";

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

    public static List<Skill> getPlayerSkills(Player player) {
        Set<String> tags = player.getScoreboardTags();
        List<Skill> list = new ArrayList<>();
        for (Skill skill : skills.values()) {
            if (tags.contains(skill.getTag())) {
                list.add(skill);
            }
        }
        return list;
    }

    public void setSkills(Player player, Integer bpNumber) {
        if (!permissionOperation.getArenaWorldNames().contains(player.getWorld().getName())) {
            return;
        }

        clearSkillTags(player);

        List<Integer> enabled = fileUtils.getEnabledSKillID(player, bpNumber);
        if (enabled == null || enabled.isEmpty()) {
            return;
        }

        ConfigurationSection skillsRoot = messageUtil.getSKillGUIItem();
        if (skillsRoot == null) {
            return;
        }

        Set<String> added = new HashSet<>();

        for (Integer id : enabled) {
            if (id == null) continue;
            String rawTag = skillsRoot.getString(id + ".tag");
            if (rawTag == null || rawTag.isBlank()) continue;

            String tagToAdd = TAG_PREFIX + rawTag.trim();
            if (added.add(tagToAdd)) {
                player.addScoreboardTag(tagToAdd);
            }
        }
    }

    private void clearSkillTags(Player player) {
        for (String tag : new java.util.HashSet<>(player.getScoreboardTags())) {
            if (tag.startsWith(TAG_PREFIX)) {
                player.removeScoreboardTag(tag);
            }
        }
    }
}
