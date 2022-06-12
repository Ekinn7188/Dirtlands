package net.dirtlands.listeners.shopkeepers;

import dirtlands.db.Tables;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.dirtlands.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NPCDeletion implements Listener {

    @EventHandler
    public void onNPCDespawn(NPCDespawnEvent e) {
        Bukkit.broadcast(Component.text("NPC Despawned: " + e.getNPC().getId()));

        Main.getPlugin().getDslContext().deleteFrom(Tables.SHOPKEEPERS)
                .where(Tables.SHOPKEEPERS.SHOPKEEPERID.eq(e.getNPC().getId())).execute();

    }

}
