package net.dirtlands.listeners;

import net.dirtlands.database.DatabaseTools;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class Join implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        //if player isn't in db, add them
        if (DatabaseTools.getUserID(e.getPlayer().getUniqueId()) == -1) {
            DatabaseTools.addUser(e.getPlayer().getUniqueId());
        }
    }

}
