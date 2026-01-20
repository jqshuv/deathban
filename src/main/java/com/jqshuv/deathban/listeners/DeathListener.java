package com.jqshuv.deathban.listeners;

import com.jqshuv.deathban.DeathBan;
import com.jqshuv.deathban.utils.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class DeathListener implements Listener {

    // Store pending ban data for players who died
    private static final HashMap<UUID, PendingBan> pendingBans = new HashMap<>();

    private static class PendingBan {
        final long scheduledTime;
        final boolean banSpectator;
        final boolean doIpBan;
        final Date banExpiry;
        final String banReason;

        PendingBan(long scheduledTime, boolean banSpectator, boolean doIpBan, Date banExpiry, String banReason) {
            this.scheduledTime = scheduledTime;
            this.banSpectator = banSpectator;
            this.doIpBan = doIpBan;
            this.banExpiry = banExpiry;
            this.banReason = banReason;
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity().getPlayer();
        assert p != null;

        DeathBan.debug("=== Player Death Event ===");
        DeathBan.debug("Player: " + p.getName() + " (UUID: " + p.getUniqueId() + ")");
        DeathBan.debug("Death cause: " + e.getEntity().getLastDamageCause());
        DeathBan.debug("Killer: " + (p.getKiller() != null ? p.getKiller().getName() : "None"));

        FileConfiguration fl = DeathBan.getInstance().getCustomConfig();
        if (!p.hasPermission("deathban.immune") || fl.getBoolean("settings.ignore-permission")) {
            DeathBan.debug("Player does not have immunity or immunity is ignored");

            // Check if player-kill-only mode is enabled
            boolean playerKillOnly = fl.getBoolean("settings.player-kill-only");
            DeathBan.debug("Player-kill-only mode: " + playerKillOnly);

            if (playerKillOnly && p.getKiller() == null) {
                // Player was not killed by another player, skip ban
                DeathBan.debug("Skipping ban: Player was not killed by another player");
                return;
            }

            int tillBan = fl.getInt("settings.ban-delay");
            boolean banSpectator = fl.getBoolean("settings.spectator-after-death");
            boolean doIpBan = fl.getBoolean("settings.ban-ip");
            int banTime = fl.getInt("settings.ban-time");
            Date date = new Date();

            DeathBan.debug("Ban delay: " + tillBan + " seconds");
            DeathBan.debug("Spectator mode: " + banSpectator);
            DeathBan.debug("IP ban: " + doIpBan);
            DeathBan.debug("Ban time: " + banTime + " minutes");

            if (banSpectator) {
                p.setGameMode(GameMode.SPECTATOR);
                DeathBan.debug("Set player to spectator mode");
            }

            if (banTime == 0) {
                date = null;
                DeathBan.debug("Permanent ban (no expiry)");
            } else if (banTime > 0) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.add(Calendar.MINUTE, banTime);
                date = cal.getTime();
                DeathBan.debug("Ban will expire at: " + date);
            }

            Date finalDate = date;
            String banReason = fl.getString("settings.banreason");
            DeathBan.debug("Ban reason: " + banReason);

            // Store the pending ban - it will be executed when player respawns
            long scheduledTime = System.currentTimeMillis() + (tillBan * 1000L);
            DeathBan.debug("Scheduling ban execution for: " + new Date(scheduledTime));

            PendingBan ban = new PendingBan(scheduledTime, banSpectator, doIpBan, finalDate, banReason);
            pendingBans.put(p.getUniqueId(), ban);
            DeathBan.debug("Total pending bans in queue: " + pendingBans.size());
            DeathBan.debug("=== End Player Death Event ===");
        } else {
            DeathBan.debug("Player has immunity and ignore-permission is false - skipping ban");
            DeathBan.debug("=== End Player Death Event ===");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        UUID playerId = p.getUniqueId();

        DeathBan.debug("=== Player Respawn Event ===");
        DeathBan.debug("Player: " + p.getName() + " (UUID: " + playerId + ")");
        DeathBan.debug("Total pending bans in queue: " + pendingBans.size());
        DeathBan.debug("Has pending ban: " + pendingBans.containsKey(playerId));

        if (!pendingBans.containsKey(playerId)) {
            DeathBan.debug("No pending ban found - skipping");
            DeathBan.debug("=== End Player Respawn Event ===");
            return;
        }

        PendingBan ban = pendingBans.get(playerId);
        DeathBan.debug("Found pending ban, checking if delay has passed...");

        long currentTime = System.currentTimeMillis();
        long delay = Math.max(0, ban.scheduledTime - currentTime);

        if (delay > 0) {
            DeathBan.debug("Delay not yet passed, scheduling for " + delay + "ms later");
            long delayTicks = delay / 50; // Convert to ticks
            Scheduler.runDelayed(p, () -> {
                DeathBan.debug("Delayed ban execution for " + p.getName());
                executeBan(p, ban);
                pendingBans.remove(playerId);
            }, delayTicks);
        } else {
            DeathBan.debug("Delay has passed, executing ban immediately");
            Scheduler.runDelayed(p, () -> {
                DeathBan.debug("Immediate ban execution for " + p.getName());
                executeBan(p, ban);
                pendingBans.remove(playerId);
            }, 1L); // Small delay to ensure respawn is complete
        }

        DeathBan.debug("=== End Player Respawn Event ===");
    }

    private void executeBan(Player p, PendingBan ban) {
        DeathBan.debug("=== Execute Ban ===");
        DeathBan.debug("Player: " + p.getName());

        if (!p.isOnline()) {
            DeathBan.debug("Player is not online - aborting ban execution");
            return;
        }

        DeathBan.debug("Player is online - proceeding with ban");

        if (ban.banSpectator) {
            p.setGameMode(GameMode.SPECTATOR);
            DeathBan.debug("Set player to spectator mode");
        }

        Scheduler.runDelayed(p, () -> {
            if (!p.isOnline()) {
                DeathBan.debug("Player went offline before teleport - aborting");
                return;
            }

            if (ban.banSpectator) {
                p.setGameMode(GameMode.SURVIVAL);
                p.setHealth(20.0);
                p.setFoodLevel(20);
                Scheduler.teleportAsync(p, p.getWorld().getSpawnLocation());
                DeathBan.debug("Restored player to survival mode and teleported to spawn");
            }
        }, 1L);

        Scheduler.runDelayed(p, () -> {
            if (!p.isOnline()) {
                DeathBan.debug("Player went offline before ban - aborting");
                return;
            }

            // Use plain text reason for ban storage to avoid Adventure/Legacy conflicts
            String plainReason = "You are banned from this server.";

            if (ban.doIpBan) {
                String ipAddress = p.getAddress().getAddress().getHostAddress();
                DeathBan.debug("Adding IP ban for: " + ipAddress);
                Bukkit.getBanList(org.bukkit.BanList.Type.IP).addBan(ipAddress, plainReason, ban.banExpiry, "console");
                DeathBan.debug("IP ban added, kicking player");
                Scheduler.kick(p, ban.banReason);
            } else {
                DeathBan.debug("Adding name ban for: " + p.getName());
                Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(p.getName(), plainReason, ban.banExpiry, "console");
                DeathBan.debug("Name ban added, kicking player");
                Scheduler.kick(p, ban.banReason);
            }

            DeathBan.debug("Ban execution completed");
            DeathBan.debug("=== End Execute Ban ===");
        }, 10L);
    }
}
