package net.dirtlands.listeners;

import net.dirtlands.files.Warps;
import net.dirtlands.tools.LocationTools;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class RespawnAtSpawn implements Listener {

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        e.getPlayer().spigot().respawn();
        try {
            e.setRespawnLocation(LocationTools.stringToLocation(Warps.get().getString("Spawn.Coords")));
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
            e.getPlayer().teleport(LocationTools.stringToLocation(Warps.get().getString("Spawn.Coords")));
        } catch (AssertionError exception) {
            //do nothing, there's no spawn location set
        }

    }

}
