package net.dirtlands.listeners;

import net.dirtlands.tools.ItemTools;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;

public class FullInventory implements Listener {

    @EventHandler
    public void onItemPickup(PlayerAttemptPickupItemEvent e) {
        if (ItemTools.droppedItems.containsKey(e.getItem())) {
            if (!ItemTools.canFitItem(e.getPlayer().getInventory(), e.getItem().getItemStack(),
                    e.getItem().getItemStack().getAmount())) {
                return;
            }
            if (!ItemTools.droppedItems.get(e.getItem()).equals(e.getPlayer().getUniqueId())) {
                e.setCancelled(true);
            } else {
                ItemTools.droppedItems.remove(e.getItem());
            }
        }
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent e) {
        ItemTools.droppedItems.remove(e.getEntity());
    }
}
