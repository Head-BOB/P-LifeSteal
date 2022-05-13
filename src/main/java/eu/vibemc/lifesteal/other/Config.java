package eu.vibemc.lifesteal.other;

import eu.vibemc.lifesteal.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Config {

    public static Integer getInt(String path) {
        return Main.getInstance().getConfig().getInt(path);
    }
    public static boolean getBoolean(String path) {
        return Main.getInstance().getConfig().getBoolean(path);
    }
    public static String getString(String path) {
        return Main.getInstance().getConfig().getString(path);
    }
    public static List<String> getStringList(String path) {
        return Main.getInstance().getConfig().getStringList(path);
    }
    public static String getMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&', Main.getInstance().getConfig().getString("messages." + path));
    }

    public static final Pattern HEX_PATTERN = Pattern.compile("&#(\\w{5}[0-9a-f])");

    public static Component textComponentFromString(final @NonNull String content) {
        Component component = Component.text(content);
        return component;
    }

    public static String translateHexCodes(String textToTranslate) {

        Matcher matcher = HEX_PATTERN.matcher(textToTranslate);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of("#" + matcher.group(1)).toString());
        }

        return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());

    }
}