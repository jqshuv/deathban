package com.jqshuv.deathban.listeners;

import com.jqshuv.deathban.DeathBan;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Date;

public class DeathListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity().getPlayer();
        assert p != null;
        if (!p.hasPermission("deathban.immune")) {
            FileConfiguration fl = DeathBan.getInstance().getCustomConfig();
            p.ban(fl.getString("settings.banreason"), (Date) null, "console");
        }
    }
}
