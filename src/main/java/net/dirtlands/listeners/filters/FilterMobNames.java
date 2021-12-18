package net.dirtlands.listeners.filters;

import io.papermc.paper.event.player.PlayerNameEntityEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class FilterMobNames implements Listener {

    @EventHandler
    public void onPlayerNameEntity(PlayerNameEntityEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (e.getName() == null) {
            return;
        }

        String name = PlainTextComponentSerializer.plainText().serialize(e.getName());


        if (Filter.blockCheck(e.getPlayer(), name)) {
            e.setCancelled(true);
        }
    }
}
