package net.dirtlands.listeners;

import jeeper.utils.LocationParser;
import net.dirtlands.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Objects;

public class RespawnAtSpawn implements Listener {

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        //e.getPlayer().spigot().respawn();
        try {
            e.setRespawnLocation(LocationParser.stringToLocation(Objects.requireNonNull(Main.getPlugin().warps().get().getString("Spawn.Coords"))));
        } catch (AssertionError exception) {
            //do nothing, there's no spawn location set
        }
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent e){
        if (e.getPlayer().hasPlayedBefore()){
            return;
        }
        try {
            e.getPlayer().teleport(LocationParser.stringToLocation(Objects.requireNonNull(Main.getPlugin().warps().get().getString("Spawn.Coords"))));
        } catch (AssertionError exception) {
            //do nothing, there's no spawn location set
        }

    }

}
