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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Items {

    private static final NamespacedKey KEY_ITEM = new NamespacedKey("lifesteal", "item");
    private static final NamespacedKey KEY_CHANCE = new NamespacedKey("lifesteal", "chance");
    private static final NamespacedKey KEY_PLAYER_UUID = new NamespacedKey("lifesteal", "player_uuid");
    private static final char[] RECIPE_KEYS = "ABCDEFGHI".toCharArray();

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
            reviveBookMeta.getPersistentDataContainer().set(KEY_ITEM, PersistentDataType.STRING, "reviveBook");
            reviveBook.setItemMeta(reviveBookMeta);
            return reviveBook;
        }

        public static boolean isReviveBook(ItemStack item) {
            if (item == null || !item.hasItemMeta()) return false;
            PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
            if (!pdc.has(KEY_ITEM, PersistentDataType.STRING)) return false;
            return "reviveBook".equalsIgnoreCase(pdc.get(KEY_ITEM, PersistentDataType.STRING));
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
                // store uuid for reliable lookup
                bannedPlayerSkullMeta.getPersistentDataContainer().set(KEY_PLAYER_UUID, PersistentDataType.STRING, ban.getPlayerUUID().toString());
                bannedPlayerSkull.setItemMeta(bannedPlayerSkullMeta);
                final SGButton playerToReviveButton = new SGButton(bannedPlayerSkull).withListener((InventoryClickEvent e) -> {
                    e.setCancelled(true);
                    ItemStack clickedItem = e.getCurrentItem();
                    if (clickedItem == null || !clickedItem.hasItemMeta()) return;
                    String uuidStr = clickedItem.getItemMeta().getPersistentDataContainer().get(KEY_PLAYER_UUID, PersistentDataType.STRING);
                    if (uuidStr == null) return;
                    OfflinePlayer target = Bukkit.getOfflinePlayer(UUID.fromString(uuidStr));
                    try {
                        if (BanStorageUtil.deleteBan(target.getUniqueId())) {
                            String mode = Config.getString("custom-commands.mode");
                            if (mode.equalsIgnoreCase("enabled") || mode.equalsIgnoreCase("both")) {
                                List<String> commands = Config.getStringList("custom-commands.onRevive");
                                for (String command : commands) {
                                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.replace("%reviving%", player.getName()).replace("%revived%", target.getName()).replace("${reviving}", player.getName()).replace("${revived}", target.getName()));
                                }
                                if (mode.equalsIgnoreCase("enabled")) {
                                    player.closeInventory();
                                    return;
                                }
                            }
                            player.sendMessage(Config.getMessage("playerRevived").replace("${player}", target.getName()));
                            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 100, 1);
                            if (!Config.getBoolean("reviveBook.unbreakable")) {
                                player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
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
            extraHeartMeta.getPersistentDataContainer().set(KEY_ITEM, PersistentDataType.STRING, "extraHeart");
            extraHeartMeta.getPersistentDataContainer().set(KEY_CHANCE, PersistentDataType.INTEGER, chance);
            extraHeart.setItemMeta(extraHeartMeta);
            return extraHeart;
        }

        private static int getChance(ItemStack item) {
            return item.getItemMeta().getPersistentDataContainer().get(KEY_CHANCE, PersistentDataType.INTEGER);
        }

        public static boolean isExtraHeart(ItemStack item) {
            if (item == null || !item.hasItemMeta()) return false;
            PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
            if (!pdc.has(KEY_ITEM, PersistentDataType.STRING)) return false;
            return "extraHeart".equalsIgnoreCase(pdc.get(KEY_ITEM, PersistentDataType.STRING));
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

    public static class Recipes {

        private static void setShapedIngredients(ShapedRecipe recipe, List<String> items) {
            for (int i = 0; i < items.size() && i < 9; i++) {
                Material material = Material.getMaterial(items.get(i));
                if (material != null) {
                    recipe.setIngredient(RECIPE_KEYS[i], material);
                }
            }
        }

        private static void registerShapedRecipe(String recipeName, String itemName, ItemStack result) {
            ShapedRecipe shapedRecipe = new ShapedRecipe(new NamespacedKey("lifesteal", itemName + recipeName), result);
            shapedRecipe.shape("ABC", "DEF", "GHI");
            setShapedIngredients(shapedRecipe, Config.getStringList("recipe.recipes." + recipeName + ".items"));
            Main.getInstance().getServer().addRecipe(shapedRecipe);
        }

        private static void registerShapelessRecipe(String recipeName, String itemName, ItemStack result) {
            ShapelessRecipe shapelessRecipe = new ShapelessRecipe(new NamespacedKey("lifesteal", itemName + recipeName), result);
            Config.getStringList("recipe.recipes." + recipeName + ".items").forEach(item -> {
                shapelessRecipe.addIngredient(Material.getMaterial(item));
            });
            Main.getInstance().getServer().addRecipe(shapelessRecipe);
        }

        private static ItemStack getResultItem(String itemName, String recipeName) {
            if (itemName.equalsIgnoreCase("revive_book")) {
                return Items.ReviveBook.getReviveBook();
            } else if (itemName.equalsIgnoreCase("extra_heart")) {
                return Items.ExtraHeart.getExtraHeart(Config.getInt("recipe.recipes." + recipeName + ".extraHeartItemUseSuccess"));
            }
            return null;
        }

        public static void registerRecipes() {
            if (Config.getBoolean("recipe.enabled")) {
                Main.getInstance().getConfig().getConfigurationSection("recipe.recipes").getKeys(false).forEach(recipe -> {
                    if (Config.getBoolean("recipe.recipes." + recipe + ".recipe-enabled")) {
                        String itemName = Config.getString("recipe.recipes." + recipe + ".item");
                        ItemStack result = getResultItem(itemName, recipe);
                        if (result == null) return;

                        if (Config.getBoolean("recipe.recipes." + recipe + ".shaped")) {
                            registerShapedRecipe(recipe, itemName, result);
                        } else {
                            registerShapelessRecipe(recipe, itemName, result);
                        }
                    }
                });
            }
        }

        public static void discoverRecipesForPlayer(Player player) {
            if (!Config.getBoolean("recipe.enabled")) return;
            Main.getInstance().getConfig().getConfigurationSection("recipe.recipes").getKeys(false).forEach(recipe -> {
                if (Config.getBoolean("recipe.recipes." + recipe + ".recipe-enabled")) {
                    if (Config.getBoolean("recipe.recipes." + recipe + ".discover")) {
                        String itemName = Config.getString("recipe.recipes." + recipe + ".item");
                        player.discoverRecipe(new NamespacedKey("lifesteal", itemName + recipe));
                    }
                }
            });
        }

        public static void undiscoverRecipesForPlayer(Player player) {
            if (!Config.getBoolean("recipe.enabled")) return;
            Main.getInstance().getConfig().getConfigurationSection("recipe.recipes").getKeys(false).forEach(recipe -> {
                if (Config.getBoolean("recipe.recipes." + recipe + ".recipe-enabled")) {
                    if (Config.getBoolean("recipe.recipes." + recipe + ".discover")) {
                        String itemName = Config.getString("recipe.recipes." + recipe + ".item");
                        player.undiscoverRecipe(new NamespacedKey("lifesteal", itemName + recipe));
                    }
                }
            });
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
