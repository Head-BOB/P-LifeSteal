package eu.vibemc.lifesteal.other;

import com.samjakob.spigui.SGMenu;
import com.samjakob.spigui.buttons.SGButton;
import eu.vibemc.lifesteal.Main;
import eu.vibemc.lifesteal.bans.BanStorageUtil;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Items {

    public static class ReviveBook {
        public static ItemStack getReviveBook() {
            ItemStack reviveBook = new ItemStack(Material.getMaterial(Config.getString("reviveBook.material")));
            reviveBook.setAmount(1);
            ItemMeta reviveBookMeta = reviveBook.getItemMeta();
            reviveBookMeta.setDisplayName(Config.translateHexCodes(Config.getString("reviveBook.name")));
            ArrayList<String> lore = new ArrayList<>();
            List<String> configLoreList = Config.getStringList("reviveBook.lore");
            for (String loreLine : configLoreList) {
                lore.add(Config.translateHexCodes(loreLine));
            }
            reviveBookMeta.setLore(lore);
            reviveBookMeta.getPersistentDataContainer().set(new NamespacedKey("lifesteal", "item"), PersistentDataType.STRING, "reviveBook");
            reviveBook.setItemMeta(reviveBookMeta);
            return reviveBook;
        }

        public static boolean isReviveBook(ItemStack item) {
            return item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey("lifesteal", "item"), PersistentDataType.STRING).equalsIgnoreCase("reviveBook");
        }

        public static void useReviveBook(Player player, ItemStack item) throws IOException {
            if (!Config.getBoolean("reviveBook.enabled")) {
                player.sendMessage(Config.getMessage("featureDisabled"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 100, 1);
                return;
            }
            final SGMenu menu = Main.spiGUI.create(ChatColor.translateAlternateColorCodes('&', Config.getString("reviveBook.inventory-title")), 5);
            BanStorageUtil.findAllBans().forEach(ban -> {
                OfflinePlayer bannedPlayer = Bukkit.getOfflinePlayer(ban.getPlayerUUID());
                ItemStack bannedPlayerSkull = new ItemStack(Material.PLAYER_HEAD);
                bannedPlayerSkull.setAmount(1);
                SkullMeta bannedPlayerSkullMeta = (SkullMeta) bannedPlayerSkull.getItemMeta();
                bannedPlayerSkullMeta.setOwningPlayer(bannedPlayer);
                bannedPlayerSkullMeta.setDisplayName("§6§l" + bannedPlayer.getName());
                ArrayList<String> lore = new ArrayList<>();
                List<String> configLoreList = Config.getStringList("reviveBook.skull-lore");
                // set lore from config
                for (String loreLine : configLoreList) {
                    lore.add(Config.translateHexCodes(loreLine));
                }
                bannedPlayerSkullMeta.setLore(lore);
                bannedPlayerSkull.setItemMeta(bannedPlayerSkullMeta);
                final SGButton playerToReviveButton = new SGButton(bannedPlayerSkull).withListener((InventoryClickEvent e) -> {
                    e.setCancelled(true);
                    ItemStack clickedItem = e.getCurrentItem();
                    OfflinePlayer target = Main.getInstance().getServer().getOfflinePlayer(clickedItem.getItemMeta().getDisplayName().substring(4));
                    try {
                        if (BanStorageUtil.deleteBan(target.getUniqueId())) {
                            if (Config.getString("custom-commands.mode").equalsIgnoreCase("enabled")) {
                                List<String> commands = Config.getStringList("custom-commands.onRevive");
                                for (String command : commands) {
                                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.replace("%reviving%", player.getName()).replace("%revived%", target.getName()).replace("${reviving}", player.getName()).replace("${revived}", target.getName()));
                                }
                                return;
                            }
                            if (Config.getString("custom-commands.mode").equalsIgnoreCase("both")) {
                                List<String> commands = Config.getStringList("custom-commands.onRevive");
                                for (String command : commands) {
                                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.replace("%reviving%", player.getName()).replace("%revived%", target.getName()).replace("${reviving}", player.getName()).replace("${revived}", target.getName()));
                                }
                            }
                            player.sendMessage(Config.getMessage("playerRevived").replace("${player}", target.getName()));
                            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 100, 1);
                            if (!Config.getBoolean("reviveBook.unbreakable")) {
                                player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
                            }
                            player.updateInventory();
                            player.closeInventory();
                        } else {
                            player.sendMessage(Config.getMessage("playerNotDead").replace("${player}", target.getName()));
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 100, 1);
                        }
                    } catch (final IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
                menu.addButton(playerToReviveButton);
            });
            player.openInventory(menu.getInventory());
        }
    }

    public static class ExtraHeart {
        public static ItemStack getExtraHeart(int chance) {
            ItemStack extraHeart = new ItemStack(Material.getMaterial(Config.getString("heartItem.material")));
            extraHeart.setAmount(1);
            ItemMeta extraHeartMeta = extraHeart.getItemMeta();
            extraHeartMeta.setDisplayName(Config.translateHexCodes(Config.getString("heartItem.name")));
            ArrayList<String> lore = new ArrayList<>();
            List<String> configLoreList = Config.getStringList("heartItem.lore");
            // set lore from config
            for (String loreLine : configLoreList) {
                lore.add(Config.translateHexCodes(loreLine).replace("${chance}", String.valueOf(chance)));
            }
            extraHeartMeta.setLore(lore);
            extraHeartMeta.getPersistentDataContainer().set(new NamespacedKey("lifesteal", "item"), PersistentDataType.STRING, "extraHeart");
            extraHeartMeta.getPersistentDataContainer().set(new NamespacedKey("lifesteal", "chance"), PersistentDataType.INTEGER, chance);
            extraHeart.setItemMeta(extraHeartMeta);
            return extraHeart;
        }

        private static int getChance(ItemStack item) {
            return item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey("lifesteal", "chance"), PersistentDataType.INTEGER);
        }

        public static boolean isExtraHeart(ItemStack item) {
            return item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey("lifesteal", "item"), PersistentDataType.STRING).equalsIgnoreCase("extraHeart");
        }

        public static void useExtraHeart(Player player, ItemStack item) throws IOException {
            if (!Config.getBoolean("heartItem.enabled")) {
                player.sendMessage(Config.getMessage("featureDisabled"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 100, 1);
                return;
            }
            // get chance
            int chance = ExtraHeart.getChance(item);
            // generate random number between 0 and 100 and check if it is less than the chance
            int random = (int) (Math.random() * 100);
            if (chance > random) {
                if (Config.getInt("heartItem.addLimit") == 0 || player.getAttribute(Attribute.MAX_HEALTH).getBaseValue() + 2 <= Config.getInt("heartItem.addLimit")) {
                    item.setAmount(item.getAmount() - 1);
                    player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(player.getAttribute(Attribute.MAX_HEALTH).getBaseValue() + 2);
                    player.sendMessage(Config.getMessage("heartReceived"));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 100, 1);
                } else {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 100, 1);
                    player.sendMessage(Config.getMessage("maxHeartsFromExtraHeart").replace("${max}", String.valueOf(Config.getInt("heartItem.addLimit") / 2)));
                }
            } else {
                item.setAmount(item.getAmount() - 1);
                // create another chance
                int secondRandom = (int) (Math.random() * 100);
                if (secondRandom >= Config.getInt("heartItem.loseChance")) {
                    player.sendMessage(Config.getMessage("heartFailure"));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 100, 1);
                } else {
                    if (player.getAttribute(Attribute.MAX_HEALTH).getBaseValue() - 2 <= 0) {
                        BanStorageUtil.createBan(player);
                    } else {
                        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(player.getAttribute(Attribute.MAX_HEALTH).getBaseValue() - 2);
                        player.sendMessage(Config.getMessage("heartLost"));
                        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 100, 2);
                    }
                }
            }
        }
    }

    public class Recipes {
        public static void registerRecipes() {
            if (Config.getBoolean("recipe.enabled")) {
                Main.getInstance().getConfig().getConfigurationSection("recipe.recipes").getKeys(false).forEach(recipe -> {
                    if (Config.getBoolean("recipe.recipes." + recipe + ".recipe-enabled")) {
                        String itemName = Config.getString("recipe.recipes." + recipe + ".item");
                        if (Config.getBoolean("recipe.recipes." + recipe + ".shaped")) {
                            if (itemName.equalsIgnoreCase("revive_book")) {
                                ShapedRecipe shapedRecipe = new ShapedRecipe(new NamespacedKey("lifesteal", itemName + recipe), Items.ReviveBook.getReviveBook());
                                shapedRecipe.shape("ABC", "DEF", "GHI");
                                AtomicInteger itemId = new AtomicInteger(0);
                                Config.getStringList("recipe.recipes." + recipe + ".items").forEach(item -> {
                                    var material = Material.getMaterial(item);
                                    itemId.addAndGet(1);
                                    if (itemId.get() == 1 && material != null) {
                                        shapedRecipe.setIngredient('A', material);
                                    }
                                    if (itemId.get() == 2 && material != null) {
                                        shapedRecipe.setIngredient('B', material);
                                    }
                                    if (itemId.get() == 3 && material != null) {
                                        shapedRecipe.setIngredient('C', material);
                                    }
                                    if (itemId.get() == 4 && material != null) {
                                        shapedRecipe.setIngredient('D', material);
                                    }
                                    if (itemId.get() == 5 && material != null) {
                                        shapedRecipe.setIngredient('E', material);
                                    }
                                    if (itemId.get() == 6 && material != null) {
                                        shapedRecipe.setIngredient('F', material);
                                    }
                                    if (itemId.get() == 7 && material != null) {
                                        shapedRecipe.setIngredient('G', material);
                                    }
                                    if (itemId.get() == 8 && material != null) {
                                        shapedRecipe.setIngredient('H', material);
                                    }
                                    if (itemId.get() == 9 && material != null) {
                                        shapedRecipe.setIngredient('I', material);
                                    }
                                });
                                Main.getInstance().getServer().addRecipe(shapedRecipe);
                            }
                            if (itemName.equalsIgnoreCase("extra_heart")) {
                                ShapedRecipe shapedRecipe = new ShapedRecipe(new NamespacedKey("lifesteal", itemName + recipe), Items.ExtraHeart.getExtraHeart(Config.getInt("recipe.recipes." + recipe + ".extraHeartItemUseSuccess")));
                                shapedRecipe.shape("ABC", "DEF", "GHI");
                                AtomicInteger itemId = new AtomicInteger(0);
                                Config.getStringList("recipe.recipes." + recipe + ".items").forEach(item -> {
                                    var material = Material.getMaterial(item);
                                    itemId.addAndGet(1);
                                    if (itemId.get() == 1 && material != null) {
                                        shapedRecipe.setIngredient('A', material);
                                    }
                                    if (itemId.get() == 2 && material != null) {
                                        shapedRecipe.setIngredient('B', material);
                                    }
                                    if (itemId.get() == 3 && material != null) {
                                        shapedRecipe.setIngredient('C', material);
                                    }
                                    if (itemId.get() == 4 && material != null) {
                                        shapedRecipe.setIngredient('D', material);
                                    }
                                    if (itemId.get() == 5 && material != null) {
                                        shapedRecipe.setIngredient('E', material);
                                    }
                                    if (itemId.get() == 6 && material != null) {
                                        shapedRecipe.setIngredient('F', material);
                                    }
                                    if (itemId.get() == 7 && material != null) {
                                        shapedRecipe.setIngredient('G', material);
                                    }
                                    if (itemId.get() == 8 && material != null) {
                                        shapedRecipe.setIngredient('H', material);
                                    }
                                    if (itemId.get() == 9 && material != null) {
                                        shapedRecipe.setIngredient('I', material);
                                    }
                                });
                                Main.getInstance().getServer().addRecipe(shapedRecipe);
                            }

                        } else {
                            if (itemName.equalsIgnoreCase("extra_heart")) {
                                ShapelessRecipe shapelessRecipe = new ShapelessRecipe(new NamespacedKey("lifesteal", itemName + recipe), Items.ExtraHeart.getExtraHeart(Config.getInt("recipe.recipes." + recipe + ".extraHeartItemUseSuccess")));
                                Config.getStringList("recipe.recipes." + recipe + ".items").forEach(item -> {
                                    shapelessRecipe.addIngredient(Material.getMaterial(item));
                                });
                                Main.getInstance().getServer().addRecipe(shapelessRecipe);
                            }
                            if (itemName.equalsIgnoreCase("revive_book")) {
                                ShapelessRecipe shapelessRecipe = new ShapelessRecipe(new NamespacedKey("lifesteal", itemName + recipe), Items.ReviveBook.getReviveBook());
                                Config.getStringList("recipe.recipes." + recipe + ".items").forEach(item -> {
                                    shapelessRecipe.addIngredient(Material.getMaterial(item));
                                });
                                Main.getInstance().getServer().addRecipe(shapelessRecipe);
                            }
                        }
                    }

                });
            }
        }

        public static void unregisterRecipes() {
            Main.getInstance().getConfig().getConfigurationSection("recipe.recipes").getKeys(false).forEach(recipe -> {
                if (Config.getBoolean("recipe.recipes." + recipe + ".recipe-enabled")) {
                    String item = Config.getString("recipe.recipes." + recipe + ".item");
                    Main.getInstance().getServer().removeRecipe(new NamespacedKey("lifesteal", item + recipe));
                }
            });
        }
    }
}
