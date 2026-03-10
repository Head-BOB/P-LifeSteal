package eu.vibemc.lifesteal.events;

import eu.vibemc.lifesteal.Main;
import eu.vibemc.lifesteal.other.Config;
import eu.vibemc.lifesteal.other.Items;
import eu.vibemc.lifesteal.other.UpdateChecker;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (player.hasPermission("lifesteal.update") || player.isOp()) {
            new UpdateChecker(Main.getInstance()).getVersion(version -> {
                if (!Main.getInstance().getDescription().getVersion().equals(version)) {
                    player.sendMessage("§a§lP-LifeSteal §7§l> §c§lA NEW UPDATE HAS BEEN RELEASED! §6(" + version + ")");
                }
            });
        }
        Items.Recipes.discoverRecipesForPlayer(player);
        if (Config.getBoolean("security.limits.auto-revert")) {
            int killLimit = Config.getInt("killHeartLimit");
            int addLimit = Config.getInt("heartItem.addLimit");
            int max = Math.max(killLimit, addLimit);
            // if player's max health is bigger than max, set max health to max
            if (killLimit > 0 && addLimit > 0 && max > 0 && player.getAttribute(Attribute.MAX_HEALTH).getBaseValue() > max) {
                Main.getInstance().getLogger().info(player.getAttribute(Attribute.MAX_HEALTH).getBaseValue() + " > " + max);
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(max);
                player.sendMessage(Config.getMessage("abuseDetected"));
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 100, 1);
            }
        }
    }
}