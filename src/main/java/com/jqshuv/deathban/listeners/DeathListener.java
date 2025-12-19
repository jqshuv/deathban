package com.jqshuv.deathban.listeners;

import com.jqshuv.deathban.DeathBan;
import com.jqshuv.deathban.utils.Scheduler;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
                Scheduler.runDelayed(p, () -> {
                    banFunction(p, fl, banSpectator, doIpBan, finalDate);
                }, tillBan * 20L);
            }

        }
    }

    private void banFunction(Player p, FileConfiguration fl, boolean banSpectator, boolean doIpBan, Date finalDate) {
        String banReason = fl.getString("settings.banreason");

        Scheduler.runDelayed(p, () -> {
            if (banSpectator) {
                p.setGameMode(GameMode.SURVIVAL);
                p.setHealth(20.0);
                p.setFoodLevel(20);
                p.teleport(p.getWorld().getSpawnLocation());
            }

        }, 1L);

        Scheduler.runDelayed(p, () -> {
            TextComponent reasonComponent = (TextComponent) DeathBan.getMiniMessage().deserialize(banReason);
            String legacyReason = LegacyComponentSerializer.legacySection().serialize(reasonComponent);

            if (doIpBan) {
                Bukkit.getBanList(org.bukkit.BanList.Type.IP).addBan(p.getAddress().getAddress().getHostAddress(), legacyReason, finalDate, "console");
                Scheduler.kick(p, banReason);
            } else {
                Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(p.getName(), legacyReason, finalDate, "console");
                Scheduler.kick(p, banReason);
            }
        }, 10L);
    }
}
