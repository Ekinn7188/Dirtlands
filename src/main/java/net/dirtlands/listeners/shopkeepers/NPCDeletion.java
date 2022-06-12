package net.dirtlands.listeners.shopkeepers;

import dirtlands.db.Tables;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.dirtlands.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NPCDeletion implements Listener {

    @EventHandler
    public void onNPCDespawn(NPCDespawnEvent e) {
        Main.getPlugin().getDslContext().deleteFrom(Tables.SHOPKEEPERS)
                .where(Tables.SHOPKEEPERS.SHOPKEEPERID.eq(e.getNPC().getId())).execute();

    }

}
