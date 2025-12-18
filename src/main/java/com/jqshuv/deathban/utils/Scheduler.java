package com.jqshuv.deathban.utils;

import com.jqshuv.deathban.DeathBan;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Scheduler {

    private static final boolean IS_FOLIA = isClassPresent("io.papermc.paper.threadedregions.RegionScheduler");

    public static void runDelayed(Player player, Runnable runnable, long delayTicks) {
        if (IS_FOLIA) {
            try {
                Object scheduler = player.getClass().getMethod("getScheduler").invoke(player);
                scheduler.getClass().getMethod("runDelayed", org.bukkit.plugin.Plugin.class, java.util.function.Consumer.class, String.class, long.class)
                        .invoke(scheduler, DeathBan.getInstance(), (java.util.function.Consumer<Object>) task -> runnable.run(), null, delayTicks);
            } catch (Exception e) {
                Bukkit.getScheduler().runTaskLater(DeathBan.getInstance(), runnable, delayTicks);
            }
        } else {
            Bukkit.getScheduler().runTaskLater(DeathBan.getInstance(), runnable, delayTicks);
        }
    }

    private static boolean isClassPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
