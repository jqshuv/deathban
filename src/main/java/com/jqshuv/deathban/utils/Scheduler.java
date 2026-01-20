package com.jqshuv.deathban.utils;

import com.jqshuv.deathban.DeathBan;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Scheduler {

    private static final boolean IS_FOLIA = isFoliaCheck();

    private static boolean isFoliaCheck() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionScheduler");
            return true;
        } catch (ClassNotFoundException ignored) {}
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.RegionScheduler");
            return true;
        } catch (ClassNotFoundException ignored) {}
        return Bukkit.getVersion().contains("Folia");
    }

    public static void runDelayed(Player player, Runnable runnable, long delayTicks) {
        DeathBan.debug("Scheduling task for player " + player.getName() + " with delay " + delayTicks + " ticks");

        // Wrap the runnable to check player state before execution
        Runnable safeRunnable = () -> {
            if (player.isOnline()) {
                DeathBan.debug("Executing scheduled task for " + player.getName());
                runnable.run();
            } else {
                DeathBan.debug("Skipping scheduled task - player " + player.getName() + " is offline");
            }
        };

        if (IS_FOLIA) {
            DeathBan.debug("Using Folia EntityScheduler");
            try {
                // Try to get the EntityScheduler from the player
                Object scheduler = player.getClass().getMethod("getScheduler").invoke(player);
                // Folia EntityScheduler.runDelayed signature:
                // ScheduledTask runDelayed(Plugin plugin, Consumer<ScheduledTask> task, Runnable retired, long delayTicks)
                scheduler.getClass().getMethod("runDelayed", org.bukkit.plugin.Plugin.class, java.util.function.Consumer.class, Runnable.class, long.class)
                        .invoke(scheduler, DeathBan.getInstance(), (java.util.function.Consumer<Object>) task -> safeRunnable.run(), null, delayTicks);
                DeathBan.debug("Task scheduled successfully using Folia");
                return; // Success
            } catch (Exception e) {
                DeathBan.getInstance().getLogger().warning("Folia detected but failed to use EntityScheduler: " + e.getMessage());
                // If it fails, we don't fallback to Bukkit scheduler on Folia because it WILL throw UnsupportedOperationException
                // Instead, we run it immediately or log a severe error
                DeathBan.getInstance().getLogger().severe("COULD NOT SCHEDULE TASK ON FOLIA! Running immediately as fallback.");
                safeRunnable.run();
                return;
            }
        }
        
        // Fallback for Spigot/Paper
        DeathBan.debug("Using Bukkit scheduler");
        Bukkit.getScheduler().runTaskLater(DeathBan.getInstance(), safeRunnable, delayTicks);
        DeathBan.debug("Task scheduled successfully using Bukkit scheduler");
    }

    public static boolean isFolia() {
        return IS_FOLIA;
    }

    public static void teleportAsync(Player player, org.bukkit.Location location) {
        DeathBan.debug("Attempting to teleport player " + player.getName() + " to " + location.getWorld().getName() + " (" + location.getX() + ", " + location.getY() + ", " + location.getZ() + ")");

        if (IS_FOLIA) {
            try {
                // Use Folia's teleportAsync method
                Object scheduler = player.getClass().getMethod("getScheduler").invoke(player);
                scheduler.getClass().getMethod("teleport", org.bukkit.plugin.Plugin.class, org.bukkit.Location.class)
                        .invoke(scheduler, DeathBan.getInstance(), location);
                DeathBan.debug("Player teleported successfully using Folia teleportAsync");
                return;
            } catch (Exception e) {
                DeathBan.getInstance().getLogger().warning("Folia teleport failed: " + e.getMessage());
            }
        }

        // Fallback for Spigot/Paper
        try {
            player.teleport(location);
            DeathBan.debug("Player teleported successfully using Bukkit teleport");
        } catch (Exception e) {
            DeathBan.getInstance().getLogger().warning("Teleport failed: " + e.getMessage());
        }
    }

    public static void kick(Player player, String reason) {
        DeathBan.debug("Attempting to kick player " + player.getName() + " with reason: " + reason);
        try {
            // Use MiniMessage to deserialize the reason string properly
            Object reasonComponent = DeathBan.getMiniMessage().deserialize(reason);

            // Use the native Paper/Adventure API
            player.kick((net.kyori.adventure.text.Component) reasonComponent);
            DeathBan.debug("Player kicked successfully using Adventure API");
        } catch (Exception e) {
            DeathBan.debug("Failed to kick using Adventure API: " + e.getMessage() + " - trying fallback");
            // Fallback - kick with plain text
            try {
                net.kyori.adventure.text.Component plainComponent = net.kyori.adventure.text.Component.text(reason);
                player.kick(plainComponent);
                DeathBan.debug("Player kicked successfully using plain text fallback");
            } catch (Exception ex) {
                // Last resort fallback
                DeathBan.getInstance().getLogger().warning("Could not kick player using Adventure API: " + ex.getMessage());
                DeathBan.debug("All kick methods failed");
            }
        }
    }
}
