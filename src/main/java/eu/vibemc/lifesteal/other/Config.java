package eu.vibemc.lifesteal.other;

import eu.vibemc.lifesteal.Main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Config {

    public static final Pattern HEX_PATTERN = Pattern.compile("&#(\\w{5}[\\da-f])");

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
        return translateHexCodes(ChatColor.translateAlternateColorCodes('&', Main.getInstance().getConfig().getString("messages." + path)));
    }

    public static String translateHexCodes(String textToTranslate) {

        Matcher matcher = Config.HEX_PATTERN.matcher(textToTranslate);
        StringBuilder builder = new StringBuilder();

        while (matcher.find()) {
            matcher.appendReplacement(builder, net.md_5.bungee.api.ChatColor.of("#" + matcher.group(1)).toString());
        }

        return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', matcher.appendTail(builder).toString());

    }

    public static void update() {
        File file = new File(Main.getInstance().getDataFolder() + "/config.yml");
        YamlConfiguration externalFile = YamlConfiguration.loadConfiguration(file);

        InputStreamReader defConfigStream = new InputStreamReader(Main.getInstance().getResource("config.yml"), StandardCharsets.UTF_8);
        YamlConfiguration internalFile = YamlConfiguration.loadConfiguration(defConfigStream);

        for (String string : internalFile.getKeys(true)) {
            if (!externalFile.contains(string)) {
                externalFile.set(string, internalFile.get(string));
            }
        }
        try {
            externalFile.save(file);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
}
