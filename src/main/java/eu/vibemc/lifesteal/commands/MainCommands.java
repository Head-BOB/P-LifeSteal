package eu.vibemc.lifesteal.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import eu.vibemc.lifesteal.Main;
import eu.vibemc.lifesteal.bans.BanStorageUtil;
import eu.vibemc.lifesteal.other.Config;
import eu.vibemc.lifesteal.other.Debug;
import eu.vibemc.lifesteal.other.Items;
import eu.vibemc.lifesteal.other.LootPopulator;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.logging.Level;

import static eu.vibemc.lifesteal.other.Debug.URL;
import static eu.vibemc.lifesteal.other.Items.Recipes.registerRecipes;
import static eu.vibemc.lifesteal.other.Items.Recipes.unregisterRecipes;

public class MainCommands {
    public static CommandAPICommand getMainCommands() {
        return new CommandAPICommand("lifesteal")
                .withShortDescription("Main LifeSteal command.")
                .withAliases("ls", "pls", "p-ls", "plifesteal", "p-lifesteal")
                .executes((sender, args) -> {
                    sender.sendMessage("§aP-LifeSteal §a§l" + Main.getInstance().getDescription().getVersion());
                    sender.sendMessage("§aCreated by §e§ldevPrzemuS");
                    sender.sendMessage("§6§lhttps://www.spigotmc.org/resources/101967/");
                })
                .withSubcommand(MainCommands.getWithdrawCommand())
                .withSubcommand(MainCommands.getHelpCommand())
                .withSubcommand(MainCommands.getReloadCommand())
                .withSubcommand(MainCommands.getDebugInfoCommand());

    }

    private static CommandAPICommand getHelpCommand() {
        return new CommandAPICommand("help")
                .withShortDescription("Help command.")
                .executes((sender, args) -> {
                    sender.sendMessage("§aAnswers to most questions can be found in the documentation:");
                    sender.sendMessage("§6§lhttps://ls.przemus.xyz/");
                    sender.sendMessage("§aIf you §c§ldon't §r§afind an answer to your question there, ask for help on discord:");
                    sender.sendMessage("§6§lhttps://discord.gg/8sjwaQTHGC/");
                });
    }

    public static CommandAPICommand getDebugInfoCommand() {
        return new CommandAPICommand("debug")
                .withPermission("lifesteal.debug")
                .withShortDescription("Debug command.")
                .executes((sender, args) -> {

                    Debug.postDebug(Debug.makeDebug(Main.getInstance())).whenComplete((key, exception) -> {
                        if (exception != null) {
                            Main.getInstance().getLogger().log(Level.WARNING, "Failed to post Debug Info.", exception);

                            sender.sendMessage("§cFailed to post Debug Info, check console.");
                            return;
                        }

                        sender.sendMessage("§aDebug Info Link: §e§l" + URL + key);
                    });


//                    File config = new File(Main.getInstance().getDataFolder(), "config.yml");
//                    // make bukkit async
//                    Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
//                        URL url = new URL("https://pastebin.com/api/api_post.php");
//                        URLConnection con = url.openConnection();
//                        HttpURLConnection http = (HttpURLConnection)con;
//                        http.setRequestMethod("POST");
//                        http.setDoInput(true);
//                        Map<String,String> arguments = new HashMap<>();
//                        arguments.put("lang", "Plaintext");
//                        http.connect();
//                    });
                });
    }

    private static CommandAPICommand getWithdrawCommand() {
        return new CommandAPICommand("withdraw")
                .withShortDescription("Withdraws heart.")
                .executes((sender, args) -> {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        if (!Config.getBoolean("heartItem.withdraw-enabled")) {
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 100, 1);
                            sender.sendMessage(Config.getMessage("featureDisabled"));
                            return;
                        }
                        if (player.getAttribute(Attribute.MAX_HEALTH).getBaseValue() - 2 <= 0) {
                            try {
                                player.getInventory().addItem(Items.ExtraHeart.getExtraHeart(100));
                                player.updateInventory();
                                player.sendMessage(Config.getMessage("heartWithdrawn"));
                                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 100, 1);
                                BanStorageUtil.createBan(player);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(player.getAttribute(Attribute.MAX_HEALTH).getBaseValue() - 2);
                            // check if player's inventory isnt full
                            if (player.getInventory().firstEmpty() == -1) {
                                player.getWorld().dropItem(player.getLocation(), Items.ExtraHeart.getExtraHeart(100));
                                player.sendMessage(Config.getMessage("heartWithdrawn"));
                                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 100, 1);
                            } else {
                                player.getInventory().addItem(Items.ExtraHeart.getExtraHeart(100));
                                player.updateInventory();
                                player.sendMessage(Config.getMessage("heartWithdrawn"));
                                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 100, 1);
                            }
                        }

                    } else {
                        sender.sendMessage(Config.getMessage("urNotPlayer"));
                    }
                });
    }

    private static CommandAPICommand getReloadCommand() {
        return new CommandAPICommand("reload")
                .withPermission("lifesteal.reload")
                .withShortDescription("Reloads config.")
                .executes((sender, args) -> {
                    if (Config.getBoolean("recipe.enabled")) {
                        Main.getInstance().getConfig().getConfigurationSection("recipe.recipes").getKeys(false).forEach(recipe -> {
                            if (Config.getBoolean("recipe.recipes." + recipe + ".recipe-enabled")) {
                                if (Config.getBoolean("recipe.recipes." + recipe + ".discover")) {
                                    String itemName = Config.getString("recipe.recipes." + recipe + ".item");
                                    for (Player player : Bukkit.getOnlinePlayers()) {
                                        player.undiscoverRecipe(new NamespacedKey("lifesteal", itemName + recipe));
                                    }
                                }
                            }
                        });
                    }
                    unregisterRecipes();
                    for (World world : Bukkit.getServer().getWorlds()) {
                        world.getPopulators().removeIf(blockPopulator -> blockPopulator instanceof LootPopulator);
                    }
                    Main.getInstance().reloadConfig();
                    sender.sendMessage(Config.getMessage("configReloaded"));
                    registerRecipes();
                    if (Config.getBoolean("recipe.enabled")) {
                        Main.getInstance().getConfig().getConfigurationSection("recipe.recipes").getKeys(false).forEach(recipe -> {
                            if (Config.getBoolean("recipe.recipes." + recipe + ".recipe-enabled")) {
                                if (Config.getBoolean("recipe.recipes." + recipe + ".discover")) {
                                    String itemName = Config.getString("recipe.recipes." + recipe + ".item");
                                    for (Player player : Bukkit.getOnlinePlayers()) {
                                        player.discoverRecipe(new NamespacedKey("lifesteal", itemName + recipe));
                                    }
                                }
                            }
                        });
                    }
                    sender.sendMessage(Config.getMessage("recipesReloaded"));
                    if (Config.getBoolean("loot.enabled")) {
                        for (World world : Bukkit.getServer().getWorlds()) {
                            world.getPopulators().add(new LootPopulator(Main.getInstance()));
                        }
                    }
                    sender.sendMessage(Config.getMessage("lootReloaded"));

                });
    }
}
