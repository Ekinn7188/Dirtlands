package net.dirtlands.listeners;

import net.dirtlands.tools.Durability;
import net.dirtlands.tools.ItemTools;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Random;

public class OnItemDamage implements Listener {

    @EventHandler
    public void onPlayerItemDamage(PlayerItemDamageEvent e) {
        int durability = Durability.getDurability(e.getItem());
        int maxDurability = Durability.getMaxDurability(e.getItem());

        if (maxDurability == -1 || durability == -1) {
            return;
        }

        e.setCancelled(true);

        if (e.getItem().containsEnchantment(Enchantment.DURABILITY)) {
            double randomVal = new Random().nextDouble();

            // Formulas from https://minecraft.fandom.com/wiki/Unbreaking#Usage
            // Both unbreaking variables are the chance that durability is reduced
            double unbreaking;
            if (ItemTools.isArmor(e.getItem())) {
                unbreaking = 100.0d / (e.getItem().getEnchantmentLevel(Enchantment.DURABILITY) + 1);
            }
            else {
                unbreaking = 60 + (40.0d / (e.getItem().getEnchantmentLevel(Enchantment.DURABILITY) + 1)) / 100;
            }

            unbreaking /= 100; // turn percents into decimals

            // randomVal < unbreaking is the probability that durability is reduced
            // The opposite will be for no durability taken
            if (randomVal > unbreaking) {
                return;
            }
        }

        Durability.setCustomDurability(e.getPlayer(), e.getItem(), durability-1, maxDurability);
    }

    @EventHandler
    public void onMend(PlayerItemMendEvent e) {
        e.setCancelled(true);
        Durability.increaseDurability(e.getItem(), e.getRepairAmount());
    }

    @EventHandler
    public void onAnvilRepair(PrepareAnvilEvent e) {
        ItemStack result = e.getResult();
        if (result == null || result.getType().equals(Material.AIR)) {
            return;
        }

        AnvilInventory inv = e.getInventory();

        ItemStack firstItem = inv.getFirstItem();
        ItemStack secondItem = inv.getSecondItem();


        if (firstItem == null || firstItem.getType().equals(Material.AIR)
                || secondItem == null || secondItem.getType().equals(Material.AIR)) {
            return;
        }

        ItemMeta firstItemMeta = firstItem.getItemMeta();
        ItemMeta secondItemMeta = secondItem.getItemMeta();

        if (firstItemMeta == null || secondItemMeta == null) {
            return;
        }

        // Repair item with same item (e.g. dia pickaxe + dia pickaxe repair)
        if (firstItem.getType().equals(secondItem.getType())) {
            if ((firstItemMeta instanceof Damageable firstDamageable) && (secondItemMeta instanceof Damageable secondDamageable)) {
                int maxDurability = firstItem.getType().getMaxDurability();
                int firstDurability = maxDurability - firstDamageable.getDamage();
                int secondDurability = maxDurability - secondDamageable.getDamage();

                ItemStack firstItemCopy = new ItemStack(firstItem);

                Durability.increaseDurability(firstItemCopy, firstDurability+secondDurability);

                e.setResult(firstItemCopy);
            }
            return;
        }

        // Item + material repair (1 item repairs +25% of the max durability)
        if ((firstItemMeta instanceof Damageable firstDamageable)) {
            int maxDurability = firstItem.getType().getMaxDurability();
            int itemDurability = maxDurability - firstDamageable.getDamage();

            ItemStack firstItemCopy = new ItemStack(firstItem);

            Durability.increaseDurability(firstItemCopy, secondItem.getAmount()*(Durability.getMaxDurability(firstItem)/4));

            e.setResult(firstItemCopy);
        }
    }
}
