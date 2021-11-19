package net.dirtlands.listeners;

import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.tools.Countdown;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.UUID;

public class TeleportCancel implements Listener {
    private static ConfigSetup config = Main.getPlugin().config();

    @EventHandler
    public void cancelTeleport(PlayerMoveEvent e){
        UUID uuid = e.getPlayer().getUniqueId();
        Location to = e.getTo();
        Location from = e.getFrom();

        if (Countdown.getTasks().containsKey(uuid) && (to.getX() != from.getX() || to.getY() != from.getY() || to.getZ() != from.getZ()) && (!e.getPlayer().isFlying() && e.getPlayer().isOp())){
            e.getPlayer().sendMessage(MessageTools.parseFromPath(config, "Teleport Canceled"));
            Bukkit.getScheduler().cancelTask(Countdown.getTasks().get(uuid));
            Countdown.getTasks().remove(uuid);
        }
    }

}
