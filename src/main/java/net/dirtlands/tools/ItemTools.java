package net.dirtlands.tools;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ItemTools {

    public static ItemStack createGuiItem(final Material material, final Component name, int amount, final Component... lore) {
        if (amount == -1) {
            amount = 1;
        }
        final ItemStack item = new ItemStack(material, amount);

        return createGuiItem(item, material, name, amount, lore);
    }

    public static ItemStack createGuiItem(@NotNull final ItemStack item, final Material material, final Component name, int amount, final Component... lore) {
        final ItemMeta meta = item.getItemMeta();

        if (material != null) {
            item.setType(material);
        }
        if (!name.equals(Component.empty())){
            meta.displayName(enableItalicUsage(name));
        }
        if (amount == -1){
            item.setAmount(amount);
        }
        if (lore.length > 0){
            meta.lore(Arrays.stream(lore).filter(c -> c!=Component.empty()).map(ItemTools::enableItalicUsage).toList());
        }

        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack getHead(OfflinePlayer player) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(player);
        item.setItemMeta(meta);
        return item;
    }

    public static Component enableItalicUsage(Component message){
        return message.decoration(TextDecoration.ITALIC, false).mergeStyle(message);
    }
}
