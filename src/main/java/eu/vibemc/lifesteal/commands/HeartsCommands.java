package eu.vibemc.lifesteal.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.PlayerProfileArgument;
import eu.vibemc.lifesteal.other.Config;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerProfile;

import java.util.List;

public class HeartsCommands {
    public static CommandAPICommand getAllHeartsCommands() {
        return new CommandAPICommand("hearts")
                .withPermission("lifesteal.heart")
                .withShortDescription("Command to manage hearts.")
                .withSubcommand(HeartsCommands.getCheckHeartsCommand())
                .withSubcommand(HeartsCommands.getSetHeartsCommand())
                .withSubcommand(HeartsCommands.getAddHeartsCommand())
                .withSubcommand(HeartsCommands.getRemoveHeartsCommand());
    }

    private static CommandAPICommand getAddHeartsCommand() {
        return new CommandAPICommand("add")
                .withPermission("lifesteal.heart.manage")
                .withArguments(new PlayerProfileArgument("player"), new IntegerArgument("amount"))
                .withShortDescription("Add hearts to player.")
                .executes((sender, args) -> {
                    List<PlayerProfile> profiles = (List<PlayerProfile>) args.get("player");
                    Player player = org.bukkit.Bukkit.getPlayer(profiles.getFirst().getUniqueId());
                    if (player == null) {
                        sender.sendMessage("§cPlayer must be online.");
                        return;
                    }
                    int amount = (int) args.get("amount");
                    player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(player.getAttribute(Attribute.MAX_HEALTH).getBaseValue() + amount);
                    player.sendMessage(Config.getMessage("heartAdded").replace("${amount}", String.valueOf(amount / 2)));
                    sender.sendMessage(Config.getMessage("heartAddedAdmin").replace("${amount}", String.valueOf(amount / 2)).replace("${player}", player.getName()));
                });
    }

    private static CommandAPICommand getSetHeartsCommand() {
        return new CommandAPICommand("set")
                .withPermission("lifesteal.heart.manage")
                .withArguments(new PlayerProfileArgument("player"), new IntegerArgument("amount"))
                .withShortDescription("Sets amount of player's hearts.")
                .executes((sender, args) -> {
                    List<PlayerProfile> profiles = (List<PlayerProfile>) args.get("player");
                    Player player = org.bukkit.Bukkit.getPlayer(profiles.getFirst().getUniqueId());
                    if (player == null) {
                        sender.sendMessage("§cPlayer must be online.");
                        return;
                    }
                    int amount = (int) args.get("amount");
                    player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(amount);
                    player.sendMessage(Config.getMessage("heartSetted").replace("${amount}", String.valueOf(amount / 2)));
                    sender.sendMessage(Config.getMessage("heartSettedAdmin").replace("${amount}", String.valueOf(amount / 2)).replace("${player}", player.getName()));

                });
    }

    private static CommandAPICommand getRemoveHeartsCommand() {
        return new CommandAPICommand("remove")
                .withPermission("lifesteal.heart.manage")
                .withArguments(new PlayerProfileArgument("player"), new IntegerArgument("amount"))
                .withShortDescription("Removes hearts from player.")
                .executes((sender, args) -> {
                    List<PlayerProfile> profiles = (List<PlayerProfile>) args.get("player");
                    Player player = org.bukkit.Bukkit.getPlayer(profiles.getFirst().getUniqueId());
                    if (player == null) {
                        sender.sendMessage("§cPlayer must be online.");
                        return;
                    }
                    int amount = (int) args.get("amount");
                    player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(player.getAttribute(Attribute.MAX_HEALTH).getBaseValue() - amount);
                    player.sendMessage(Config.getMessage("heartRemoved").replace("${amount}", String.valueOf(amount / 2)));
                    sender.sendMessage(Config.getMessage("heartRemovedAdmin").replace("${amount}", String.valueOf(amount / 2)).replace("${player}", player.getName()));

                });
    }

    private static CommandAPICommand getCheckHeartsCommand() {
        return new CommandAPICommand("check")
                .withPermission("lifesteal.heart.check")
                .withShortDescription("Check how many hearts player have.")
                .withArguments(new PlayerProfileArgument("player"))
                .executes((sender, args) -> {
                    List<PlayerProfile> profiles = (List<PlayerProfile>) args.get("player");
                    Player player = org.bukkit.Bukkit.getPlayer(profiles.getFirst().getUniqueId());
                    if (player == null) {
                        sender.sendMessage("§cPlayer must be online.");
                        return;
                    }
                    int amount = (int) player.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
                    sender.sendMessage(Config.getMessage("heartCheck").replace("${amount}", String.valueOf(amount / 2)).replace("${player}", player.getName()));
                });
    }
}
