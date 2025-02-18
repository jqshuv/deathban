package com.jqshuv.deathban.listeners;

import com.jqshuv.deathban.DeathBan;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Calendar;
import java.util.Date;

public class DeathListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity().getPlayer();
        assert p != null;
        FileConfiguration fl = DeathBan.getInstance().getCustomConfig();
        if (!p.hasPermission("deathban.immune") || fl.getBoolean("settings.ignore-permission")) {
            int tillBan = fl.getInt("settings.ban-delay");
            boolean banSpectator = fl.getBoolean("settings.spectator-after-death");
            boolean doIpBan = fl.getBoolean("settings.ban-ip");
            int banTime = fl.getInt("settings.ban-time");
            Date date = new Date();

            if (banSpectator) {
                p.setGameMode(GameMode.SPECTATOR);
            }

            if (banTime == 0) {
                date = null;
            } else if (banTime > 0) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.add(Calendar.MINUTE, banTime);
                date = cal.getTime();
            }

            Date finalDate = date;
            if (tillBan == 0) {
                banFunction(p, fl, banSpectator, doIpBan, finalDate);

            } else if (tillBan > 0) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(DeathBan.getInstance(), () -> {
                    banFunction(p, fl, banSpectator, doIpBan, finalDate);
                }, tillBan * 20L);
            }

        }
    }

    private void banFunction(Player p, FileConfiguration fl, boolean banSpectator, boolean doIpBan, Date finalDate) {

        Bukkit.getScheduler().scheduleSyncDelayedTask(DeathBan.getInstance(), () -> {
            if (banSpectator) {
                p.setGameMode(GameMode.SURVIVAL);
                p.setHealth(20.0);
                p.setFoodLevel(20);
                p.teleport(p.getWorld().getSpawnLocation());
            }

        }, 1L);

        Bukkit.getScheduler().scheduleSyncDelayedTask(DeathBan.getInstance(), () -> {
            if (doIpBan) {
                p.banIp(fl.getString("settings.banreason"), finalDate, "console", true);
            } else {
                p.ban(fl.getString("settings.banreason"), finalDate, "console", true);
            }
        }, 10L);
    }
}
