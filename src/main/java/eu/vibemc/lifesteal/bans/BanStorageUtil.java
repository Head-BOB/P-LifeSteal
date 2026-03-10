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
import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class BanStorageUtil {

    private static final Gson gson = new Gson();
    private static List<Ban> bans = Collections.synchronizedList(new ArrayList<>());

    public static Ban createBan(Player player) throws IOException {
        if (getBan(player.getUniqueId()) != null) {
            return null;
        }
        String mode = Config.getString("custom-commands.mode");
        if (mode.equalsIgnoreCase("enabled") || mode.equalsIgnoreCase("both")) {
            dispatchBanCommands(player);
            if (mode.equalsIgnoreCase("enabled")) {
                return null;
            }
        }
        Ban createdBan;
        if (Config.getInt("banTime") > 0) {
            int banTime = Config.getInt("banTime") * 60;
            long unixTime = System.currentTimeMillis() / 1000L + banTime;
            createdBan = new Ban(player.getUniqueId(), unixTime);
        } else {
            createdBan = new Ban(player.getUniqueId(), Ban.PERMANENT_BAN_TIME);
        }
        bans.add(createdBan);
        saveBans();
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

    private static void dispatchBanCommands(Player player) {
        List<String> commands = Config.getStringList("custom-commands.onBan");
        for (String command : commands) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()).replace("${player}", player.getName()));
        }
    }

    public static OfflinePlayer getOfflinePlayerByBan(Ban ban) {
        return Main.getInstance().getServer().getOfflinePlayer(ban.getPlayerUUID());
    }

    public static Ban getBan(UUID uuid) throws IOException {
        synchronized (bans) {
            Iterator<Ban> iterator = bans.iterator();
            while (iterator.hasNext()) {
                Ban ban = iterator.next();
                if (ban.getPlayerUUID().equals(uuid)) {
                    // check if ban is still valid
                    if (ban.getUnbanTime() > System.currentTimeMillis() / 1000L) {
                        return ban;
                    } else {
                        iterator.remove();
                        saveBans();
                        return null;
                    }
                }
            }
        }
        return null;
    }

    public static boolean deleteBan(UUID uuid) throws IOException {
        Ban ban = getBan(uuid);
        if (ban == null) {
            return false;
        }
        bans.remove(ban);
        saveBans();
        return true;
    }

    public static void saveBans() throws IOException {
        File file = new File(Main.getInstance().getDataFolder().getAbsolutePath() + "/bans.json");
        file.getParentFile().mkdir();
        file.createNewFile();
        try (Writer writer = new FileWriter(file, false)) {
            gson.toJson(bans, writer);
            writer.flush();
        }
    }

    public static void loadBans() throws IOException {
        File file = new File(Main.getInstance().getDataFolder().getAbsolutePath() + "/bans.json");
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                Ban[] b = gson.fromJson(reader, Ban[].class);
                bans = Collections.synchronizedList(new ArrayList<>(Arrays.asList(b)));
            }
        }
    }

    public static List<Ban> findAllBans() {
        return bans;
    }
}
