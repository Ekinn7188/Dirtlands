package net.dirtlands.listeners.filters;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class FilterItemNames implements Listener {

    @EventHandler
    public void onSmithItem(InventoryClickEvent e) {
        if (e.getSlotType().equals(InventoryType.SlotType.RESULT)) {
            if (e.getInventory().getType().equals(InventoryType.ANVIL)) {

                ItemStack item = e.getCurrentItem();
                if (item == null) {
                    return;
                }
                Component itemNameComponent = item.getItemMeta().displayName();
                if (itemNameComponent == null) {
                    return;
                }

                String name = PlainTextComponentSerializer.plainText().serialize(itemNameComponent);
                if (Filter.blockCheck(e.getView().getPlayer(), name)) {
                    e.setCancelled(true);
                }
            }
        }
    }

}
