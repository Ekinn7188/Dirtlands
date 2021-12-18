package net.dirtlands.listeners.filters;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.util.List;
import java.util.stream.Collectors;

public class FilterSigns implements Listener {

    @EventHandler
    public void onSignChange(SignChangeEvent e) {

        //check if sign has blocked words in it
        List<String> lines = e.lines().stream().map(line -> PlainTextComponentSerializer.plainText().serialize(line)).collect(Collectors.toList());

        for (String line : lines) {
            if (Filter.blockCheck(e.getPlayer(), line)) {
                e.setCancelled(true);
                return;
            }
        }
    }
}
