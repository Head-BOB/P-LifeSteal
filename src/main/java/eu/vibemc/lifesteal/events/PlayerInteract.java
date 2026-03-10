package eu.vibemc.lifesteal.events;

import eu.vibemc.lifesteal.other.Items;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

public class PlayerInteract implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) throws IOException {
        Player player = e.getPlayer();
        Action action = e.getAction();
        if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType().isAir() || !item.hasItemMeta()) {
                return;
            }
            if (Items.ExtraHeart.isExtraHeart(item)) {
                Items.ExtraHeart.useExtraHeart(player, item);
            } else if (Items.ReviveBook.isReviveBook(item)) {
                Items.ReviveBook.useReviveBook(player, item);
            }
        }
    }
}
