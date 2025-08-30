package top.mores.backpack.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.mores.backpack.Backpack;
import top.mores.backpack.GUI.MainGUI;
import top.mores.backpack.Utils.MatchUtil;
import top.mores.backpack.Utils.ArmorUtil;

import java.util.List;
import java.util.Map;

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
            } else if (strings.length == 1 && strings[0].equals("armor")) {
                if (commandSender.isOp()) {
                    ArmorUtil.saveArmorSet(sender);
                }
            } else if (strings.length == 4 && strings[0].equals("set")) {
                // set指令实现: /bp set <物品名> amount <数量>
                if (commandSender.isOp() && strings[2].equals("amount")) {
                    try {
                        int amount = Integer.parseInt(strings[3]);
                        setItemAmount(strings[1], amount, commandSender);
                    } catch (NumberFormatException e) {
                        commandSender.sendMessage("数量必须是整数！");
                    }
                }
            }
        } else {
            commandSender.sendMessage("只有玩家在游戏内可以这样做！");
        }
        return true;
    }

    private void setItemAmount(String itemName, int amount, CommandSender sender) {
        String path = "物品匹配." + itemName;
        
        // 检查物品是否存在
        if (Backpack.getInstance().getDataConfig().contains(path)) {
            List<Map<?, ?>> itemData = Backpack.getInstance().getDataConfig().getMapList(path);
            
            if (!itemData.isEmpty()) {
                // 更新第一个物品的数量
                Map<String, Object> itemMap = (Map<String, Object>) itemData.get(0);
                if (amount <= 0) {
                    itemMap.remove("amount"); // 数量为1时移除amount键
                } else {
                    itemMap.put("amount", amount);
                }
                
                // 保存更新后的数据
                Backpack.getInstance().getDataConfig().set(path, itemData);
                Backpack.getInstance().saveDataFile();
                sender.sendMessage("已将物品 " + itemName + " 的数量设置为 " + amount);
            } else {
                sender.sendMessage("物品数据格式错误");
            }
        } else {
            sender.sendMessage("未找到物品 " + itemName);
        }
    }
}