package top.mores.backpack.Permission;

import java.util.List;

public class Perms {

    public static final String QUICK_HEAL = "tactical_skill.quick_heal";
    public static final String ALERT = "tactical_skill.alert";
    public static final String LIGHT_GEAR = "tactical_skill.light_gear";
    public static final String TACTICAL_MASK = "tactical_skill.tactical_mask";
    public static final String TEAMWORK = "tactical_skill.teamwork";
    public static final String HIGH_ALERT = "tactical_skill.high_alert";

    public static final List<String> ALL = List.of(
            QUICK_HEAL, ALERT, LIGHT_GEAR,
            TACTICAL_MASK, TEAMWORK, HIGH_ALERT
    );
}
