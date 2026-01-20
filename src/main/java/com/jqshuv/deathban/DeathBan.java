package com.jqshuv.deathban;

import com.jqshuv.deathban.listeners.DeathListener;
import com.jqshuv.deathban.utils.Scheduler;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class DeathBan extends JavaPlugin {

    private static DeathBan INSTANCE;
    private FileConfiguration customConfig;
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        INSTANCE = this;
        createCustomConfig();
        getLogger().info("DeathBan v1.3.0 enabling...");
        if (Scheduler.isFolia()) {
            getLogger().info("Folia/Paper detected! Using Folia scheduler.");
        } else {
            getLogger().info("Folia/Paper not detected. Using standard Bukkit scheduler.");
        }
        this.getServer().getPluginManager().registerEvents(new DeathListener(), this);
    }

    public FileConfiguration getCustomConfig() {
        return this.customConfig;
    }

    private void createCustomConfig() {
        File customConfigFile = new File(getDataFolder(), "config.yml");
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }

        customConfig = new YamlConfiguration();
        try {
            customConfig.load(customConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        /* User Edit:
            Instead of the above Try/Catch, you can also use
            YamlConfiguration.loadConfiguration(customConfigFile)
        */
    }

    public static DeathBan getInstance() {
        return INSTANCE;
    }

    public static MiniMessage getMiniMessage() {
        return miniMessage;
    }

    public static void debug(String message) {
        if (INSTANCE != null && INSTANCE.getCustomConfig().getBoolean("settings.debug", false)) {
            INSTANCE.getLogger().info("[DEBUG] " + message);
        }
    }
}
