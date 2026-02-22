package top.mores.backpack.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mores.backpack.Backpack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BackpackTabCompleter implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("bp")) {
            if (args.length == 1) {
                if (sender instanceof Player) {
                    completions.add("open");
                }
                if (sender.isOp()) {
                    completions.add("reload");
                    completions.add("save");
                    completions.add("armor");
                    completions.add("set");
                }
                return filterCompletions(completions, args[0]);
            } else if (args.length == 2 && args[0].equals("set") && sender.isOp()) {
                if (Backpack.getInstance().getDataConfig().contains("物品匹配")) {
                    completions.addAll(Objects.requireNonNull(Backpack.getInstance().getDataConfig().getConfigurationSection("物品匹配")).getKeys(false));
                }
                return filterCompletions(completions, args[1]);
            } else if (args.length == 3 && args[0].equals("set") && sender.isOp()) {
                completions.add("amount");
                return filterCompletions(completions, args[2]);
            }
        }
        return completions;
    }

    private List<String> filterCompletions(List<String> completions, String arg) {
        List<String> result = new ArrayList<>();
        for (String completion : completions) {
            if (completion.toLowerCase().startsWith(arg.toLowerCase())) {
                result.add(completion);
            }
        }
        return result;
    }
}