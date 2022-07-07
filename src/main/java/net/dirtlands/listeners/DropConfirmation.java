package net.dirtlands.listeners;

import jeeper.utils.MessageTools;
import net.dirtlands.Main;
import net.dirtlands.tools.ItemTools;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class DropConfirmation implements Listener {

    private static final HashMap<UUID, ItemStack> itemList = new HashMap<>();

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        ItemStack droppedItem = e.getItemDrop().getItemStack();
        if (ItemTools.isTool(droppedItem)) {
            if (itemList.get(e.getPlayer().getUniqueId()) != null
                    && itemList.get(e.getPlayer().getUniqueId()).equals(droppedItem)) {

                itemList.remove(e.getPlayer().getUniqueId());
                return; // Let player drop item if they've already received the warning message
            }
            e.setCancelled(true);

            itemList.put(e.getPlayer().getUniqueId(), droppedItem);

            e.getPlayer().sendMessage(MessageTools.parseFromPath(Main.getPlugin().config(),
                    "Drop Confirmation"));

            Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getPlugin(),
                    () -> itemList.remove(e.getPlayer().getUniqueId()), 5 * 20L);
        }
    }
}
