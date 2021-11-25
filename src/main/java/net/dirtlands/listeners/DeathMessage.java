package net.dirtlands.listeners;

import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Objects;

public class DeathMessage implements Listener {
    private static ConfigSetup config = Main.getPlugin().config();

    @EventHandler
    public void onDeath(PlayerDeathEvent e){
        Player player = e.getEntity();
        e.deathMessage(MessageTools.parseFromPath(config, "Player Death", Template.template("message", Objects.requireNonNull(e.deathMessage()))));
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> player.spigot().respawn(), 1L);
    }

}
