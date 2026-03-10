package eu.vibemc.lifesteal.other.expansions;

import eu.vibemc.lifesteal.Main;
import eu.vibemc.lifesteal.bans.BanStorageUtil;
import eu.vibemc.lifesteal.other.Config;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class LSExpansion extends PlaceholderExpansion {

    private final Main plugin;

    public LSExpansion(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "ls";
    }

    @Override
    public @NotNull String getAuthor() {
        return "devPrzemuS (P-LifeSteal)";
    }

    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }

    @Override
    public @NotNull String getVersion() {
        return Main.getInstance().getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null || !player.isOnline()) {
            if (params.equalsIgnoreCase("hearts") || params.equalsIgnoreCase("health")) {
                return "0";
            }
        }

        Player onlinePlayer = player != null ? player.getPlayer() : null;

        if (params.equalsIgnoreCase("hearts")) {
            if (onlinePlayer != null) {
                return String.valueOf((int) onlinePlayer.getAttribute(Attribute.MAX_HEALTH).getBaseValue() / 2);
            }
        }

        if (params.equalsIgnoreCase("health")) {
            if (onlinePlayer != null) {
                return String.valueOf((int) onlinePlayer.getAttribute(Attribute.MAX_HEALTH).getBaseValue());
            }
        }

        if (params.equalsIgnoreCase("banned")) {
            if (player != null) {
                try {
                    if (BanStorageUtil.getBan(player.getUniqueId()) != null) {
                        return ChatColor.translateAlternateColorCodes('&', Config.getString("placeholder-api.banned-text"));
                    } else {
                        return ChatColor.translateAlternateColorCodes('&', Config.getString("placeholder-api.not-banned-text"));
                    }
                } catch (IOException ignored) {
                }
            }
        }

        return "notfound"; // Placeholder is unknown by the Expansion
    }
}
