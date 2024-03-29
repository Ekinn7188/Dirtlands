package net.dirtlands.tools;

import jeeper.utils.MessageTools;
import net.dirtlands.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

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

    /**
     * Includes Armor
     * @param item The item to check
     */
    public static boolean isTool(ItemStack item) {
        for (EnchantmentTarget target : EnchantmentTarget.values()) {
            if (target.includes(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Does not include Tools
     * @param item The item to check
     */
    public static boolean isArmor(ItemStack item) {
        EnchantmentTarget.ARMOR.includes(item);
        return true;
    }

    public static boolean canFitItem(Inventory inv, ItemStack item) {
        return canFitItem(inv, item, item.getAmount());
    }

    public static boolean canFitItem(Inventory inv, ItemStack item, int quantity) {
        List<ItemStack> slots = Arrays.stream(Arrays.copyOfRange(inv.getContents(), 0, 36)).filter(a -> a == null || a.getAmount() != a.getMaxStackSize()).collect(Collectors.toList());
        ItemMeta itemMeta = item.getItemMeta();
        int totalRoom = 0;

        for (ItemStack slot : slots) {
            if (slot == null) {
                totalRoom += 64;
                continue;
            }
            if (slot.getItemMeta().equals(itemMeta)) {
                totalRoom += slot.getMaxStackSize() - slot.getAmount();
            }
        }
        return totalRoom >= quantity;
    }

    /**
     * @param inv the inventory to take from
     * @param item the item to take
     * @param amount how many of the item to take
     * @return whether the interaction was successful or not
     */
    public static boolean takeItems(Inventory inv, ItemStack item, int amount) {
        if (amount > countItems(inv, item)) {
            return false;
        }

        for (ItemStack invItem : inv.getContents()) {
            if (amount <= 0) {
                break;
            }
            if (invItem == null) {
                continue;
            }
            if (!isSimilar(invItem,item)) {
                continue;
            }

            int amountLeft = amount - invItem.getAmount();
            amount = Math.max(amountLeft, 0);
            invItem.setAmount(amountLeft >= 0 ? 0 : Math.abs(amountLeft-amount));
        }

        return true;
    }

    /**
     * Counts the items in an inventory ignoring the display name
     * @param inv inventory to count in
     * @param item the item to search for
     * @return how many items were found
     */
    public static int countItems(Inventory inv, ItemStack item) {
        int count = 0;
        for (ItemStack invItem : inv.getContents()) {
            if (invItem == null) {
                continue;
            }
            if (isSimilar(invItem, item)) {
                count += invItem.getAmount();
            }
        }
        return count;
    }

    public static boolean isSimilar(ItemStack item1, ItemStack item2) {
        if (item1.isSimilar(item2)) {
            return true;
        }

        ItemStack item1Copy = new ItemStack(item1);
        ItemStack item2Copy = new ItemStack(item2);

        ItemMeta invMeta = item1Copy.getItemMeta();
        ItemMeta itemMeta = item2Copy.getItemMeta();

        invMeta.displayName(Component.text(""));
        List<Component> invLore = invMeta.lore();
        if (invLore != null) {
            if (invLore.size() == 0) {
                invMeta.lore(null);
            }
        }
        else {
            invMeta.lore(null);
        }
        itemMeta.displayName(Component.text(""));
        List<Component> itemLore = invMeta.lore();
        if (itemLore != null) {
            if (itemLore.size() == 0) {
                itemMeta.lore(null);
            }
        }
        else {
            itemMeta.lore(null);
        }

        item1Copy.setItemMeta(invMeta);
        item2Copy.setItemMeta(itemMeta);

        return item1Copy.isSimilar(item2Copy);
    }

    public static final Map<Item, UUID> droppedItems = new HashMap<>();

    public static void dropItemForOnlyPlayer(HumanEntity player, ItemStack item) {
        dropItemForOnlyPlayer(player, List.of(item).toArray(new ItemStack[1]));
    }

    public static void dropItemForOnlyPlayer(HumanEntity player, ItemStack ... items) {
        for (ItemStack item : items) {
            if (item.getAmount() == 0) {
                continue;
            }
            Location location = player.getLocation();
            Item dropped = location.getWorld().dropItemNaturally(location, item);

            Entity droppedEntity = ((CraftItem) dropped).getHandle();

            Bukkit.getOnlinePlayers().stream().filter(p -> !p.equals(player)).forEach(p ->
                    ((CraftPlayer) p).getHandle().connection.send(new ClientboundRemoveEntitiesPacket(droppedEntity.getId())));

            droppedItems.put(dropped, player.getUniqueId());
        }

        player.sendMessage(MessageTools.parseFromPath(Main.getPlugin().config(), "No Space Drop"));
    }

}
