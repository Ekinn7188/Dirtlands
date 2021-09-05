package net.dirtlands.listeners.shopkeepers;

import net.dirtlands.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class Shopkeeper {

    public static void openMenu(Player player, String name){

        final Inventory inventory;


        inventory = Bukkit.createInventory(player, 54, Component.text(name));

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
