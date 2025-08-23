package top.mores.backpack.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.mores.backpack.Backpack;
import top.mores.backpack.GUI.MainGUI;
import top.mores.backpack.Utils.MatchUtil;

public class BackpackCommand implements CommandExecutor {
    MainGUI mainGUI = new MainGUI();
    MatchUtil matchUtil = new MatchUtil();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player sender) {
            //打开背包
            if (strings.length == 1 && strings[0].equals("open")) {
                mainGUI.CreateMainInventory(sender);
            } else if (strings.length == 1 && strings[0].equals("reload")) {
                if (commandSender.isOp()) {
                    Backpack.getInstance().reloadConfigFile();
                    Backpack.getInstance().reloadData();
                    commandSender.sendMessage("已重载配置文件");
                }
            } else if (strings.length == 1 && strings[0].equals("save")) {
                if (commandSender.isOp()) {
                    matchUtil.saveItemFromHand(sender);
                }
            }
        } else {
            commandSender.sendMessage("只有玩家在游戏内可以这样做！");
        }
        return true;
    }
}