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

public class BackpackTabCompleter implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("bp")) {
            if (args.length == 1) {
                // 第一个参数的补全
                if (sender instanceof Player) {
                    completions.add("open");
                }

                if (sender.isOp()) {
                    completions.add("reload");
                    completions.add("save");
                    completions.add("armor");
                    completions.add("set");
                    completions.add("savegui");
                }
                // 根据已输入的内容过滤
                return filterCompletions(completions, args[0]);
            } else if (args.length == 2 && args[0].equals("set") && sender.isOp()) {
                // set指令的第二个参数补全（物品名）
                completions.addAll(Backpack.getInstance().getStorage().getItemMatchKeys());
                return filterCompletions(completions, args[1]);
            } else if (args.length == 3 && args[0].equals("set") && sender.isOp()) {
                // set指令的第三个参数补全
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