package net.dirtlands.listeners;

import net.dirtlands.tools.ItemTools;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;

public class DisableToolCrafting implements Listener {

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent e) {
        if (e.getRecipe() == null) {
            return;
        }
        ItemStack result = e.getRecipe().getResult();
        if (ItemTools.isTool(result)) {
            e.getInventory().setResult(new ItemStack(Material.AIR));
        }

    }

    @EventHandler
    public void onPrepareSmithing(PrepareSmithingEvent e) {
        e.setResult(new ItemStack(Material.AIR));
    }
}
