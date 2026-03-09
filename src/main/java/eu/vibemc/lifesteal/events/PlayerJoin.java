package eu.vibemc.lifesteal.events;

import eu.vibemc.lifesteal.Main;
import eu.vibemc.lifesteal.other.Config;
import eu.vibemc.lifesteal.other.UpdateChecker;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.IOException;

public class PlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) throws IOException {
        Player player = e.getPlayer();
        if (player.hasPermission("lifesteal.update") || player.isOp()) {
            new UpdateChecker(Main.getInstance()).getVersion(version -> {
                if (!Main.getInstance().getDescription().getVersion().equals(version)) {
                    player.sendMessage("§a§lP-LifeSteal §7§l> §c§lA NEW UPDATE HAS BEEN RELEASED! §6(" + version + ")");
                }
            });
        }
        if (Config.getBoolean("recipe.enabled")) {
            Main.getInstance().getConfig().getConfigurationSection("recipe.recipes").getKeys(false).forEach(recipe -> {
                if (Config.getBoolean("recipe.recipes." + recipe + ".recipe-enabled")) {
                    if (Config.getBoolean("recipe.recipes." + recipe + ".discover")) {
                        String itemName = Config.getString("recipe.recipes." + recipe + ".item");
                        player.discoverRecipe(new NamespacedKey("lifesteal", itemName + recipe));
                    }
                }
            });
        }
        if (Config.getBoolean("security.limits.auto-revert")) {
            // get which value is bigger from Config.getInt("heartItem.addLimit") and Config.getInt("killHeartLimit")
            int max = Config.getInt("heartItem.addLimit") > Config.getInt("killHeartLimit") ? Config.getInt("heartItem.addLimit") : Config.getInt("killHeartLimit");
            // if player's max health is bigger than max, set max health to max
            if (Config.getInt("killHeartLimit") > 0 && Config.getInt("heartItem.addLimit") > 0 && max > 0 && player.getAttribute(Attribute.MAX_HEALTH).getBaseValue() > max) {
                Main.getInstance().getLogger().info(player.getAttribute(Attribute.MAX_HEALTH).getBaseValue() + " > " + max);
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(max);
                player.sendMessage(Config.getMessage("abuseDetected"));
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 100, 1);
            }
        }
    }
}