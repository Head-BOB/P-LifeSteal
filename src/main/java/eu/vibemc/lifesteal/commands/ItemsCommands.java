package eu.vibemc.lifesteal.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.PlayerProfileArgument;
import eu.vibemc.lifesteal.other.Items;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerProfile;

import java.util.List;

public class ItemsCommands {
    public static CommandAPICommand getAllItemsCommands() {
        return new CommandAPICommand("give")
                .withPermission("lifesteal.give")
                .withShortDescription("Gives you specified item.")
                .withSubcommand(ItemsCommands.getGiveReviveBookCommand())
                .withSubcommand(ItemsCommands.getGiveExtraHeartCommand());
    }

    private static CommandAPICommand getGiveExtraHeartCommand() {
        return new CommandAPICommand("extra_heart")
                .withPermission("lifesteal.give.extraheart")
                .withArguments(new PlayerProfileArgument("player"), new IntegerArgument("chance_of_success"), new IntegerArgument("amount")).executes((sender, args) -> {
                    List<PlayerProfile> profiles = (List<PlayerProfile>) args.get("player");
                    Player player = org.bukkit.Bukkit.getPlayer(profiles.getFirst().getUniqueId());
                    if (player == null) {
                        sender.sendMessage("§cPlayer must be online.");
                        return;
                    }
                    int chance = (int) args.get("chance_of_success");
                    int amount = (int) args.get("amount");
                    for (int i = 0; i < amount; i++) {
                        player.getInventory().addItem(Items.ExtraHeart.getExtraHeart(chance));
                    }
                    player.updateInventory();
                });
    }

    private static CommandAPICommand getGiveReviveBookCommand() {
        return new CommandAPICommand("revive_book")
                .withPermission("lifesteal.give.revivebook")
                .withArguments(new PlayerProfileArgument("player"), new IntegerArgument("amount")).
                executes((sender, args) -> {
                    List<PlayerProfile> profiles = (List<PlayerProfile>) args.get("player");
                    Player player = org.bukkit.Bukkit.getPlayer(profiles.getFirst().getUniqueId());
                    if (player == null) {
                        sender.sendMessage("§cPlayer must be online.");
                        return;
                    }
                    int amount = (int) args.get("amount");
                    for (int i = 0; i < amount; i++) {
                        player.getInventory().addItem(Items.ReviveBook.getReviveBook());
                    }
                    player.updateInventory();
                });
    }
}
