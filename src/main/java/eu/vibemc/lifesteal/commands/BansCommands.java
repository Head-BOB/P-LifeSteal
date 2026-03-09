package eu.vibemc.lifesteal.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerProfileArgument;
import eu.vibemc.lifesteal.Main;
import eu.vibemc.lifesteal.bans.BanStorageUtil;
import eu.vibemc.lifesteal.bans.models.Ban;
import eu.vibemc.lifesteal.other.Config;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.profile.PlayerProfile;

import java.io.IOException;
import java.util.List;

public class BansCommands {
    public static CommandAPICommand getAllBansCommands() {
        return new CommandAPICommand("bans")
                .withPermission("lifesteal.bans")
                .executes((sender, args) -> {
                    sender.sendMessage("§6§lBans:");
                    for (Ban ban : BanStorageUtil.findAllBans()) {
                        sender.sendMessage("§c" + Main.getInstance().getServer().getOfflinePlayer(ban.getPlayerUUID()).getName());
                    }
                })
                .withSubcommand(BansCommands.getRemoveBanCommand());
    }

    private static CommandAPICommand getRemoveBanCommand() {
        return new CommandAPICommand("remove")
                .withPermission("lifesteal.bans.remove")
                .withArguments(new PlayerProfileArgument("player"))
                .executes((sender, args) -> {
                    List<PlayerProfile> profiles = (List<PlayerProfile>) args.get("player");
                    PlayerProfile profile = profiles.getFirst();
                    OfflinePlayer player = Bukkit.getOfflinePlayer(profile.getUniqueId());
                    try {
                        if (player.getName() == null) {
                            sender.sendMessage(Config.getMessage("playerNotFound"));
                        } else {
                            if (BanStorageUtil.deleteBan(player.getUniqueId())) {
                                sender.sendMessage(Config.getMessage("banRemoved").replace("${player}", player.getName()));

                            } else {
                                sender.sendMessage(Config.getMessage("playerNotBanned").replace("${player}", player.getName()));
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
