package net.dirtlands.listeners;

import net.dirtlands.tools.MessageTools;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Objects;

public class DeathMessage implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent e){
        Player player = e.getEntity();
        e.deathMessage(MessageTools.parseFromPath("Player Death", Template.of("Message", Objects.requireNonNull(e.deathMessage()))));
    }

}
