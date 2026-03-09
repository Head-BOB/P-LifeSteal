package eu.vibemc.lifesteal.other;

import org.bukkit.Bukkit;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

public class CommandAPIDependency {

    private static final String COMMANDAPI_VERSION = "11.1.0";
    private static final String COMMANDAPI_JAR = "CommandAPI-" + COMMANDAPI_VERSION + "-Paper.jar";
    private static final String DOWNLOAD_URL = "https://github.com/CommandAPI/CommandAPI/releases/download/"
            + COMMANDAPI_VERSION + "/" + COMMANDAPI_JAR;

    public static boolean ensureInstalled(Logger logger) {
        File pluginsFolder = eu.vibemc.lifesteal.Main.getInstance().getDataFolder().getParentFile();
        if (pluginsFolder == null || !pluginsFolder.exists() || !pluginsFolder.isDirectory()) {
            logger.severe("Could not find the plugins folder.");
            return false;
        }
        File commandApiFile = new File(pluginsFolder, COMMANDAPI_JAR);

        if (Bukkit.getPluginManager().getPlugin("CommandAPI") != null) {
            return true;
        }

        File[] files = pluginsFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().startsWith("CommandAPI-") && file.getName().endsWith(".jar")) {
                    return true;
                }
            }
        }

        logger.info("CommandAPI not found! Downloading " + COMMANDAPI_JAR + "...");
        try {
            URL url = URI.create(DOWNLOAD_URL).toURL();
            try (InputStream in = url.openStream()) {
                Files.copy(in, commandApiFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            logger.info("Successfully downloaded " + COMMANDAPI_JAR + " to plugins folder.");
            logger.warning("*** SERVER RESTART REQUIRED ***");
            logger.warning("CommandAPI has been downloaded. Please restart the server for P-LifeSteal to work.");
            return false;
        } catch (Exception e) {
            logger.severe("Failed to download CommandAPI: " + e.getMessage());
            logger.severe("Please manually download " + COMMANDAPI_JAR + " from:");
            logger.severe(DOWNLOAD_URL);
            logger.severe("And place it in your plugins folder, then restart the server.");
            return false;
        }
    }
}
