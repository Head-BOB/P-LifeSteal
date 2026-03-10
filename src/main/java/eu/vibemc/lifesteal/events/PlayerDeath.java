package eu.vibemc.lifesteal.events;

import eu.vibemc.lifesteal.bans.BanStorageUtil;
import eu.vibemc.lifesteal.other.Config;
import eu.vibemc.lifesteal.other.Items;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.io.IOException;

public class PlayerDeath implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) throws IOException {
        Player killed = e.getEntity();
        Player killer = killed.getKiller();

        if (Config.getBoolean("removeHeartOnlyIfKilledByPlayer")) {
            if (killer == null) {
                return;
            }
            if (isAltFarming(killed, killer)) {
                return;
            }
            removeHeartFromKilled(killed);
            giveHeartToKiller(killed, killer);
        } else {
            if (killer != null && isAltFarming(killed, killer)) {
                return;
            }
            if (killer == null && Config.getString("heartItem.drop.mode").equalsIgnoreCase("always")) {
                if (killed.getAttribute(Attribute.MAX_HEALTH).getBaseValue() - 2 > 0) {
                    killed.getWorld().dropItemNaturally(killed.getLocation(), Items.ExtraHeart.getExtraHeart(100));
                }
            }
            removeHeartFromKilled(killed);
            if (killer != null) {
                giveHeartToKiller(killed, killer);
            }
        }
    }

    private boolean isAltFarming(Player killed, Player killer) {
        if (!Config.getBoolean("security.alt-farming.ip-check")) {
            return false;
        }
        if (!killed.getAddress().getAddress().toString().equalsIgnoreCase(killer.getAddress().getAddress().toString())) {
            return false;
        }
        // allow if both players have bypass permission
        if (killed.hasPermission("lifesteal.security.ip-check-bypass") && killer.hasPermission("lifesteal.security.ip-check-bypass")) {
            return false;
        }
        killed.sendMessage(Config.getMessage("altFarmingIgnore").replace("${killed}", killed.getName()));
        killer.sendMessage(Config.getMessage("altFarmingIgnore").replace("${killed}", killed.getName()));
        return true;
    }

    private void removeHeartFromKilled(Player killed) throws IOException {
        if (killed.getAttribute(Attribute.MAX_HEALTH).getBaseValue() - 2 <= 0) {
            BanStorageUtil.createBan(killed);
        } else {
            // remove 2 from max health of killed player
            killed.getAttribute(Attribute.MAX_HEALTH).setBaseValue(killed.getAttribute(Attribute.MAX_HEALTH).getBaseValue() - 2);
            // send message to killed player
            killed.sendMessage(Config.getMessage("heartLost"));
            // send thunder sound to killed player
            killed.playSound(killed.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 100, 2);
        }
    }

    private void giveHeartToKiller(Player killed, Player killer) {
        if (Config.getInt("killHeartLimit") == 0 || killer.getAttribute(Attribute.MAX_HEALTH).getBaseValue() + 2 <= Config.getInt("killHeartLimit")) {
            if (Config.getString("heartItem.drop.mode").equalsIgnoreCase("always")) {
                killed.getWorld().dropItemNaturally(killed.getLocation(), Items.ExtraHeart.getExtraHeart(100));
            } else {
                // add 2 to max health of killer
                killer.getAttribute(Attribute.MAX_HEALTH).setBaseValue(killer.getAttribute(Attribute.MAX_HEALTH).getBaseValue() + 2);
                // send message to killer
                killer.sendMessage(Config.getMessage("heartGained").replace("${player}", killed.getName()));
                // send level up sound to killer
                killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 100, 1);
            }
        } else {
            if (Config.getString("heartItem.drop.mode").equalsIgnoreCase("always") || Config.getString("heartItem.drop.mode").equalsIgnoreCase("limit_exceeded")) {
                killed.getWorld().dropItemNaturally(killed.getLocation(), Items.ExtraHeart.getExtraHeart(100));
                killer.sendMessage(Config.getMessage("maxHeartsDropped").replace("${player}", killed.getName()));
            } else {
                killer.playSound(killer.getLocation(), Sound.ENTITY_VILLAGER_NO, 100, 1);
                killer.sendMessage(Config.getMessage("maxHearts").replace("${max}", String.valueOf(Config.getInt("killHeartLimit") / 2)));
            }
        }
    }
}
