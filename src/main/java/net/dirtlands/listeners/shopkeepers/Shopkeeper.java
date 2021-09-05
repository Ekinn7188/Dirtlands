package net.dirtlands.listeners.shopkeepers;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.dirtlands.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class Shopkeeper implements Listener {

    @EventHandler
    public void playerInteractWithEntity(PlayerInteractEntityEvent e) {
        if (e.getHand() != EquipmentSlot.HAND){
            return;
        }
        if (!CitizensAPI.getNPCRegistry().isNPC(e.getRightClicked())){
            return;
        }

        NPC npc = CitizensAPI.getNPCRegistry().getNPC(e.getRightClicked());

        Player player = e.getPlayer();

        final Inventory inventory;

        inventory = Bukkit.createInventory(player, 54, Component.text(npc.getName()));

        inventory.setItem(5, createGuiItem(Material.DIRT, "test"));

        Bukkit.getServer().getScheduler().runTask(Main.getPlugin(), new Runnable() {
            @Override
            public void run() {
                player.openInventory(inventory);
            }
        });
    }

    private static ItemStack createGuiItem(final Material material, final String name, final Component... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(name));
        meta.lore(Arrays.asList(lore));

        item.setItemMeta(meta);

        return item;
    }



}
