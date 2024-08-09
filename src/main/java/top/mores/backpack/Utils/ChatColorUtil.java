package top.mores.backpack.Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ChatColorUtil {

    public static String parseDisplayName(String jsonString) {
        JSONParser parser = new JSONParser();
        try {
            // 解析 JSON 字符串
            JSONObject jsonObject = (JSONObject) parser.parse(jsonString);

            // 解析颜色
            StringBuilder displayName = new StringBuilder();
            if (jsonObject.containsKey("color")) {
                String color = (String) jsonObject.get("color");
                displayName.append(parseColor(color));
            }

            // 解析斜体
            if (jsonObject.containsKey("italic") && (Boolean) jsonObject.get("italic")) {
                displayName.append(ChatColor.ITALIC);
            }

            //解析加粗
            if (jsonObject.containsKey("bold") && (Boolean) jsonObject.get("bold")) {
                displayName.append(ChatColor.BOLD);
            }

            //解析下划线
            if (jsonObject.containsKey("underlined") && (Boolean) jsonObject.get("underlined")) {
                displayName.append(ChatColor.UNDERLINE);
            }

            // 解析文本
            String text = (String) jsonObject.get("text");
            displayName.append(text);

            return displayName.toString();

        } catch (ParseException e) {
            Bukkit.getLogger().warning("未知的文本属性！");
            return ChatColor.RED + "Invalid display-name format";
        }
    }

    private static ChatColor parseColor(String color) {
        return switch (color) {
            case "dark_red" -> ChatColor.DARK_RED;
            case "red" -> ChatColor.RED;
            case "gold" -> ChatColor.GOLD;
            case "yellow" -> ChatColor.YELLOW;
            case "dark_green" -> ChatColor.DARK_GREEN;
            case "green" -> ChatColor.GREEN;
            case "aqua" -> ChatColor.AQUA;
            case "dark_aqua" -> ChatColor.DARK_AQUA;
            case "dark_blue" -> ChatColor.DARK_BLUE;
            case "blue" -> ChatColor.BLUE;
            case "light_purple" -> ChatColor.LIGHT_PURPLE;
            case "dark_purple" -> ChatColor.DARK_PURPLE;
            case "white" -> ChatColor.WHITE;
            case "gray" -> ChatColor.GRAY;
            case "dark_gray" -> ChatColor.DARK_GRAY;
            case "black" -> ChatColor.BLACK;
            default -> ChatColor.RESET;
        };
    }
}
