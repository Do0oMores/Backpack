package top.mores.backpack.Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.List;

public class ChatColorUtil {

    public static String parseDisplayName(String jsonString) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
            return parseTextObject(jsonObject);
        } catch (ParseException e) {
            Bukkit.getLogger().warning("未知的文本属性！");
            return ChatColor.RED + "Invalid display-name format";
        }
    }

    private static String parseTextObject(JSONObject jsonObject) {
        StringBuilder displayName = new StringBuilder();
        //解析颜色
        if (jsonObject.containsKey("color")) {
            String color = (String) jsonObject.get("color");
            displayName.append(parseColor(color));
        }

        if (jsonObject.containsKey("italic") && (Boolean) jsonObject.get("italic")) {
            displayName.append(ChatColor.ITALIC);
        }
        if (jsonObject.containsKey("bold") && (Boolean) jsonObject.get("bold")) {
            displayName.append(ChatColor.BOLD);
        }
        if (jsonObject.containsKey("underlined") && (Boolean) jsonObject.get("underlined")) {
            displayName.append(ChatColor.UNDERLINE);
        }
        String text = (String) jsonObject.getOrDefault("text", "");
        displayName.append(text);
        if (jsonObject.containsKey("extra")) {
            JSONArray extras = (JSONArray) jsonObject.get("extra");
            for (Object obj : extras) {
                if (obj instanceof JSONObject childObj) {
                    displayName.append(parseTextObject(childObj));
                }
            }
        }
        return displayName.toString();
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

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static List<String> color(List<String> list) {
        return list.stream()
                .map(ChatColorUtil::color)
                .toList();
    }
}