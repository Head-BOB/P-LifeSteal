package eu.vibemc.lifesteal.events;

import eu.vibemc.lifesteal.other.Items;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuit implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        Items.Recipes.undiscoverRecipesForPlayer(player);
    }
}