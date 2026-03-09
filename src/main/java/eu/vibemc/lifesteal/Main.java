package eu.vibemc.lifesteal;

import com.samjakob.spigui.SpiGUI;
import eu.vibemc.lifesteal.bans.BanStorageUtil;
import eu.vibemc.lifesteal.events.*;
import eu.vibemc.lifesteal.other.*;
import eu.vibemc.lifesteal.other.expansions.LSExpansion;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

import static eu.vibemc.lifesteal.other.Items.Recipes.registerRecipes;
import static eu.vibemc.lifesteal.other.Items.Recipes.unregisterRecipes;

public final class Main extends JavaPlugin {

    public static SpiGUI spiGUI;
    private static Main instance;
    private boolean commandApiAvailable = false;

    public static Main getInstance() {
        return Main.instance;
    }

    @Override
    public void onLoad() {
        Main.instance = this;

        if (!CommandAPIDependency.ensureInstalled(getLogger())) {
            getLogger().severe("P-LifeSteal requires CommandAPI. It has been downloaded - please restart the server.");
            return;
        }

        commandApiAvailable = true;
        try {
            BanStorageUtil.loadBans();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        eu.vibemc.lifesteal.commands.CommandsManager.loadCommands();
    }

    @Override
    public void onEnable() {
        if (!commandApiAvailable) {
            getLogger().severe("*** P-LifeSteal is disabled because CommandAPI is not loaded. ***");
            getLogger().severe("*** Please restart the server to load the auto-downloaded CommandAPI. ***");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Plugin startup logic
        spiGUI = new SpiGUI(this);
        dev.jorel.commandapi.CommandAPI.onEnable();
        Metrics metrics = new Metrics(this, 15176);

        getConfig().options().copyDefaults();
        saveDefaultConfig();
        Config.Update();

        this.getServer().getPluginManager().registerEvents(new PlayerDeath(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerInteract(), this);
        this.getServer().getPluginManager().registerEvents(new AsyncPlayerPreLogin(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerJoin(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerQuit(), this);

        if (Config.getBoolean("loot.enabled")) {
            for (World world : Bukkit.getServer().getWorlds()) {
                world.getPopulators().removeIf(blockPopulator -> blockPopulator instanceof LootPopulator);
                world.getPopulators().add(new LootPopulator(this));
            }
        }

        registerRecipes();
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new LSExpansion(this).register();
        }
        UpdateChecker.init();
    }


    @Override
    public void onDisable() {
        if (!commandApiAvailable) return;
        // Plugin shutdown logic
        unregisterRecipes();
        for (World world : Bukkit.getServer().getWorlds()) {
            world.getPopulators().removeIf(blockPopulator -> blockPopulator instanceof LootPopulator);
        }
    }
}
