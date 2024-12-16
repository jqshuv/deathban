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
        if (!p.hasPermission("deathban.immune")) {
            FileConfiguration fl = DeathBan.getInstance().getCustomConfig();
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

            if (tillBan == 0) {
                if (banSpectator) {
                    p.setGameMode(GameMode.SURVIVAL);
                    p.setHealth(20.0);
                    p.setFoodLevel(20);
                    p.teleport(p.getWorld().getSpawnLocation());
                }
                if (doIpBan) {
                    p.ban(fl.getString("settings.banreason"), date, "console", true);
                } else {
                    p.ban(fl.getString("settings.banreason"), date, "console", true);
                }
            } else if (tillBan > 0) {
                Date finalDate = date;
                Bukkit.getScheduler().scheduleSyncDelayedTask(DeathBan.getInstance(), () -> {
                    if (banSpectator) {
                        p.setGameMode(GameMode.SURVIVAL);
                        p.setHealth(20.0);
                        p.setFoodLevel(20);
                        p.teleport(p.getWorld().getSpawnLocation());
                    }
                    if (doIpBan) {
                        p.banIp(fl.getString("settings.banreason"), finalDate, "console", true);
                    } else {
                        p.ban(fl.getString("settings.banreason"), finalDate, "console", true);
                    }
                }, tillBan * 20L);
            }

        }
    }
}
