package top.mores.backpack.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.mores.backpack.GUI.MainGUI;

public class BackpackCommand implements CommandExecutor {
    MainGUI mainGUI = new MainGUI();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender,@NotNull Command command,@NotNull String s,@NotNull String[] strings) {
        if (commandSender instanceof Player) {
            //打开背包
            if (strings.length == 1 && strings[0].equals("open")) {
                Player sender = (Player) commandSender;
                mainGUI.CreateMainInventory(sender);
            }
        } else {
            commandSender.sendMessage("只有玩家在游戏内可以这样做！");
        }
        return true;
    }
}
