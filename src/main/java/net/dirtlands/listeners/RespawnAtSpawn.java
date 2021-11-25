package net.dirtlands.listeners;

import dirtlands.db.Tables;
import jeeper.utils.LocationParser;
import net.dirtlands.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jooq.DSLContext;

public class RespawnAtSpawn implements Listener {
    DSLContext dslContext = Main.getPlugin().getDslContext();
    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        try {
            e.setRespawnLocation(LocationParser.stringToLocation(dslContext.select(Tables.WARPS.WARPLOCATION).from(Tables.WARPS)
                    .where(Tables.WARPS.WARPNAME.equalIgnoreCase("spawn")).fetch().getValue(0, Tables.WARPS.WARPLOCATION)));
        } catch (IndexOutOfBoundsException exception) {
            //do nothing, there's no spawn location set
        }
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent e){
        if (e.getPlayer().hasPlayedBefore()){
            return;
        }
        try {
            e.getPlayer().teleport(LocationParser.stringToLocation(dslContext.select(Tables.WARPS.WARPLOCATION).from(Tables.WARPS)
                    .where(Tables.WARPS.WARPNAME.equalIgnoreCase("spawn")).fetch().getValue(0, Tables.WARPS.WARPLOCATION)));
        } catch (IndexOutOfBoundsException exception) {
            //do nothing, there's no spawn location set
        }

    }

}
