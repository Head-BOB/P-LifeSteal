package eu.vibemc.lifesteal.events;

import eu.vibemc.lifesteal.bans.BanStorageUtil;
import eu.vibemc.lifesteal.other.Config;
import eu.vibemc.lifesteal.other.Items;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.io.IOException;

public class PlayerDeath implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) throws IOException {
        Player killed = e.getEntity();
        Player killer = killed.getKiller();
        if (Config.getBoolean("removeHeartOnlyIfKilledByPlayer")) {
            if (killer != null) {
                if (Config.getBoolean("security.alt-farming.ip-check")) {
                    if (killed.getAddress().getAddress().toString().equalsIgnoreCase(killer.getAddress().getAddress().toString())) {
                        if (killed.hasPermission("lifesteal.security.ip-check-bypass")) {
                            if (!killer.hasPermission("lifesteal.security.ip-check-bypass")) {
                                killed.sendMessage(Config.getMessage("altFarmingIgnore").replace("${killed}", killed.getName()));
                                killer.sendMessage(Config.getMessage("altFarmingIgnore").replace("${killed}", killed.getName()));
                                return;
                            }
                        }
                        if (killer.hasPermission("lifesteal.security.ip-check-bypass")) {
                            if (!killed.hasPermission("lifesteal.security.ip-check-bypass")) {
                                killed.sendMessage(Config.getMessage("altFarmingIgnore").replace("${killed}", killed.getName()));
                                killer.sendMessage(Config.getMessage("altFarmingIgnore").replace("${killed}", killed.getName()));
                                return;
                            }
                        }
                        if (!killer.hasPermission("lifesteal.security.ip-check-bypass")) {
                            if (!killed.hasPermission("lifesteal.security.ip-check-bypass")) {
                                killed.sendMessage(Config.getMessage("altFarmingIgnore").replace("${killed}", killed.getName()));
                                killer.sendMessage(Config.getMessage("altFarmingIgnore").replace("${killed}", killed.getName()));
                                return;
                            }
                        }
                    }
                }
                if (killed.getAttribute(Attribute.MAX_HEALTH).getBaseValue() - 2 <= 0) {
                    BanStorageUtil.createBan(killed);
                } else {
                    // remove 2 from max health of killed killed
                    killed.getAttribute(Attribute.MAX_HEALTH).setBaseValue(killed.getAttribute(Attribute.MAX_HEALTH).getBaseValue() - 2);
                    // send actionbar to killed killed
                    killed.sendMessage(Config.getMessage("heartLost"));
                    // send thunder sound to killed killed
                    killed.playSound(killed.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 100, 2);
                }
                if (Config.getInt("killHeartLimit") == 0 || killer.getAttribute(Attribute.MAX_HEALTH).getBaseValue() + 2 <= Config.getInt("killHeartLimit")) {
                    if (Config.getString("heartItem.drop.mode").equalsIgnoreCase("always")) {
                        killed.getWorld().dropItemNaturally(killed.getLocation(), Items.ExtraHeart.getExtraHeart(100));
                    } else {
                        // add 2 to max health of killer
                        killer.getAttribute(Attribute.MAX_HEALTH).setBaseValue(killer.getAttribute(Attribute.MAX_HEALTH).getBaseValue() + 2);
                        // send actionbar to killer
                        killer.sendMessage(Config.getMessage("heartGained").replace("${player}", killed.getName()));
                        // send level up sound to killer
                        killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 100, 1);
                    }
                } else {
                    if (Config.getString("heartItem.drop.mode").equalsIgnoreCase("always") || Config.getString("heartItem.drop.mode").equalsIgnoreCase("limit_exceeded")) {
                        killed.getWorld().dropItemNaturally(killed.getLocation(), Items.ExtraHeart.getExtraHeart(100));
                        killer.sendMessage(Config.getMessage("maxHeartsDropped").replace("${player}", killed.getName()));
                    } else {
                        killer.playSound(killer.getLocation(), Sound.ENTITY_VILLAGER_NO, 100, 1);
                        killer.sendMessage(Config.getMessage("maxHearts").replace("${max}", String.valueOf(Config.getInt("killHeartLimit") / 2)));
                    }
                }
            } else {
                return;
            }
        } else {
            if (killer != null) {
                if (Config.getBoolean("security.alt-farming.ip-check")) {
                    if (killed.getAddress().getAddress().toString().equalsIgnoreCase(killer.getAddress().getAddress().toString())) {
                        if (killed.hasPermission("lifesteal.security.ip-check-bypass")) {
                            if (!killer.hasPermission("lifesteal.security.ip-check-bypass")) {
                                killed.sendMessage(Config.getMessage("altFarmingIgnore").replace("${killed}", killed.getName()));
                                killer.sendMessage(Config.getMessage("altFarmingIgnore").replace("${killed}", killed.getName()));
                                return;
                            }
                        }
                        if (killer.hasPermission("lifesteal.security.ip-check-bypass")) {
                            if (!killed.hasPermission("lifesteal.security.ip-check-bypass")) {
                                killed.sendMessage(Config.getMessage("altFarmingIgnore").replace("${killed}", killed.getName()));
                                killer.sendMessage(Config.getMessage("altFarmingIgnore").replace("${killed}", killed.getName()));
                                return;
                            }
                        }
                        if (!killer.hasPermission("lifesteal.security.ip-check-bypass")) {
                            if (!killed.hasPermission("lifesteal.security.ip-check-bypass")) {
                                killed.sendMessage(Config.getMessage("altFarmingIgnore").replace("${killed}", killed.getName()));
                                killer.sendMessage(Config.getMessage("altFarmingIgnore").replace("${killed}", killed.getName()));
                                return;
                            }
                        }
                    }
                }
            }
            if (killed.getAttribute(Attribute.MAX_HEALTH).getBaseValue() - 2 <= 0) {
                BanStorageUtil.createBan(killed);
            } else {
                if (killer == null) {
                    if (Config.getString("heartItem.drop.mode").equalsIgnoreCase("always")) {
                        killed.getWorld().dropItemNaturally(killed.getLocation(), Items.ExtraHeart.getExtraHeart(100));
                    }
                }
                // remove 2 from max health of killed killed
                killed.getAttribute(Attribute.MAX_HEALTH).setBaseValue(killed.getAttribute(Attribute.MAX_HEALTH).getBaseValue() - 2);
                // send actionbar to killed killed
                killed.sendMessage(Config.getMessage("heartLost"));
                // send thunder sound to killed killed
                killed.playSound(killed.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 100, 2);
            }
            if (killer != null) {
                if (Config.getInt("killHeartLimit") == 0 || killer.getAttribute(Attribute.MAX_HEALTH).getBaseValue() + 2 <= Config.getInt("killHeartLimit")) {
                    if (Config.getString("heartItem.drop.mode").equalsIgnoreCase("always")) {
                        killed.getWorld().dropItemNaturally(killed.getLocation(), Items.ExtraHeart.getExtraHeart(100));
                    } else {
                        // add 2 to max health of killer
                        killer.getAttribute(Attribute.MAX_HEALTH).setBaseValue(killer.getAttribute(Attribute.MAX_HEALTH).getBaseValue() + 2);
                        // send actionbar to killer
                        killer.sendMessage(Config.getMessage("heartGained").replace("${player}", killed.getName()));
                        // send level up sound to killer
                        killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 100, 1);
                    }
                } else {
                    if (Config.getString("heartItem.drop.mode").equalsIgnoreCase("always") || Config.getString("heartItem.drop.mode").equalsIgnoreCase("limit_exceeded")) {
                        killed.getWorld().dropItemNaturally(killed.getLocation(), Items.ExtraHeart.getExtraHeart(100));
                        killer.sendMessage(Config.getMessage("maxHeartsDropped").replace("${player}", killed.getName()));
                    } else {
                        killer.playSound(killer.getLocation(), Sound.ENTITY_VILLAGER_NO, 100, 1);
                        killer.sendMessage(Config.getMessage("maxHearts").replace("${max}", String.valueOf(Config.getInt("killHeartLimit") / 2)));
                    }
                }
            }
        }


    }
}
