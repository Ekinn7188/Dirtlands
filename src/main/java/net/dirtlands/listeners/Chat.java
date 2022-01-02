package net.dirtlands.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.dirtlands.listeners.filters.Filter;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class Chat implements Listener {

    //will override the chat from essentials
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void chat(AsyncChatEvent e) {
        String messageString = PlainTextComponentSerializer.plainText().serialize(e.message());
        if (Filter.blockCheck(e.getPlayer(), messageString)) {
            e.setCancelled(true);
        }
    }

}
