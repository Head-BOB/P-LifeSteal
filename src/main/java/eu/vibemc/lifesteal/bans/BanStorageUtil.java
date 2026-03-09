package eu.vibemc.lifesteal.bans;

import com.google.gson.Gson;
import eu.vibemc.lifesteal.Main;
import eu.vibemc.lifesteal.bans.models.Ban;
import eu.vibemc.lifesteal.other.Config;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BanStorageUtil {

    private static ArrayList<Ban> bans = new ArrayList<>();

    public static Ban createBan(Player player) throws IOException {
        if (getBan(player.getUniqueId()) != null) {
            return null;
        }
        if (Config.getString("custom-commands.mode").equalsIgnoreCase("enabled")) {
            List<String> commands = Config.getStringList("custom-commands.onBan");
            for (String command : commands) {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()).replace("${player}", player.getName()));
            }
            return null;
        }
        if (Config.getString("custom-commands.mode").equalsIgnoreCase("both")) {
            List<String> commands = Config.getStringList("custom-commands.onBan");
            for (String command : commands) {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()).replace("${player}", player.getName()));
            }
        }
        Ban createdBan;
        if (Config.getInt("banTime") > 0) {
            int banTime = Config.getInt("banTime") * 60;
            long unixTime = System.currentTimeMillis() / 1000L + banTime;
            Ban ban = new Ban(player.getUniqueId(), unixTime);
            bans.add(ban);
            saveBans();
            createdBan = ban;
        } else {
            Ban ban = new Ban(player.getUniqueId(), 5283862620L);
            bans.add(ban);
            saveBans();
            createdBan = ban;
        }
        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(Config.getInt("reviveHeartAmount"));
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
            if (player.isOnline()) {
                if (Config.getBoolean("banOn0Hearts")) {
                    player.kickPlayer(Config.getMessage("noMoreHeartsBan"));
                    if (Config.getBoolean("broadcastBanFrom0Hearts")) {
                        player.getServer().broadcastMessage(Config.getMessage("bannedNoMoreHeartsBroadcast").replace("${player}", player.getName()));
                    }
                }
            }
        }, 10L);

        return createdBan;
    }


    public static OfflinePlayer getOfflinePlayerByBan(Ban ban) {
        return Main.getInstance().getServer().getOfflinePlayer(ban.getPlayerUUID());
    }

    public static Ban getBan(UUID uuid) throws IOException {
        for (Ban ban : bans) {
            if (ban.getPlayerUUID().equals(uuid)) {
                // check if ban is still valid
                if (ban.getUnbanTime() > System.currentTimeMillis() / 1000L) {
                    return ban;
                } else {
                    bans.remove(ban);
                    saveBans();
                    return null;
                }
            }
        }
        return null;
    }

    public static boolean deleteBan(UUID uuid) throws IOException {
        if (getBan(uuid) == null) {
            return false;
        }
        bans.remove(getBan(uuid));
        saveBans();
        return true;
    }

    public static void saveBans() throws IOException {
        Gson gson = new Gson();
        File file = new File(Main.getInstance().getDataFolder().getAbsolutePath() + "/bans.json");
        file.getParentFile().mkdir();
        file.createNewFile();
        Writer writer = null;
        writer = new FileWriter(file, false);
        gson.toJson(bans, writer);
        writer.flush();
        writer.close();
    }

    public static void loadBans() throws IOException {
        Gson gson = new Gson();
        File file = new File(Main.getInstance().getDataFolder().getAbsolutePath() + "/bans.json");
        if (file.exists()) {
            Reader reader = new FileReader(file);
            Ban[] b = gson.fromJson(reader, Ban[].class);
            bans = new ArrayList<>(Arrays.asList(b));
        }
    }

    public static List<Ban> findAllBans() {
        return bans;
    }
}
