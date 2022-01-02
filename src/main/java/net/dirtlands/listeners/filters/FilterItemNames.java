package net.dirtlands.listeners.filters;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class FilterItemNames implements Listener {

    @EventHandler
    public void onSmithItem(InventoryClickEvent e) {
        if (e.getSlotType().equals(InventoryType.SlotType.RESULT)) {
            if (e.getInventory().getType().equals(InventoryType.ANVIL)) {

                ItemStack item = e.getCurrentItem();
                if (item == null) {
                    return;
                }
                ItemMeta meta = item.getItemMeta();
                if (meta == null) {
                    return;
                }
                Component itemNameComponent = meta.displayName();
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
