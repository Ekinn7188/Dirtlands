package net.dirtlands.tools;

import net.dirtlands.Main;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class Durability {

    static final NamespacedKey DURABILITY_KEY = new NamespacedKey(Main.getPlugin(), "durability");
    static final NamespacedKey MAXIMUM_DURABILITY = new NamespacedKey(Main.getPlugin(), "maximum");
    static final NamespacedKey CURRENT_DURABILITY = new NamespacedKey(Main.getPlugin(), "value");

    public static void setCustomDurability(Player player, ItemStack item, int durability, int maxDurability) {
        if (durability <= 0) {
            Location loc = player.getEyeLocation();
            loc.getWorld().playSound(player, Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
            loc.getWorld().spawnParticle(Particle.ITEM_CRACK, loc.add(loc.getDirection()), 10,
                    0.3, 0.5, 0.3, 0, item);
            item.setAmount(0);
        }
        else {
            setCustomDurability(item, durability, maxDurability);
        }
    }

    public static void setCustomDurability(ItemStack item, int durability, int maxDurability) {
        if (durability < 0) {
            durability = 0;
        }
        if (maxDurability < 0) {
            maxDurability = 0;
        }
        if (durability > maxDurability) {
            durability = maxDurability;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable damageableMeta) {
            //make the damage bar change
            int maxDurabilityValue = item.getType().getMaxDurability();
            int damage = maxDurabilityValue - (maxDurabilityValue * durability / maxDurability);
            if (damage == 0 && maxDurability != durability) {
                damageableMeta.setDamage(1);
            } else {
                damageableMeta.setDamage(damage);
            }
        }

        PersistentDataContainer durabilityContainer = item.getItemMeta().getPersistentDataContainer()
                .getAdapterContext().newPersistentDataContainer();

        durabilityContainer.set(MAXIMUM_DURABILITY, PersistentDataType.INTEGER, maxDurability);
        durabilityContainer.set(CURRENT_DURABILITY, PersistentDataType.INTEGER, durability);

        PersistentDataContainer container = meta.getPersistentDataContainer();

        container.set(DURABILITY_KEY, PersistentDataType.TAG_CONTAINER, durabilityContainer);

        item.setItemMeta(meta);
    }

    public static void increaseDurability(ItemStack item, int amount) {
        setCustomDurability(item, getDurability(item) + amount, getMaxDurability(item));
    }

    public static void decreaseDurability(ItemStack item, int amount) {
        setCustomDurability(item, getDurability(item) - amount, getMaxDurability(item));
    }


    /**
     * Get the custom durability of an item
     * @param item the item to check durability for
     * @return the max durability, returns -1 if tag is not found.
     */
    public static int getMaxDurability(ItemStack item) {
        Integer maxDurability = getDurabilityValue(item, MAXIMUM_DURABILITY);

        if (maxDurability == null) {
            return -1;
        }

        return maxDurability;
    }

    /**
     * Get the custom durability of an item
     * @param item the item to check durability for
     * @return the durability, returns -1 if tag is not found.
     */
    public static int getDurability(ItemStack item) {
        Integer maxDurability = getDurabilityValue(item, CURRENT_DURABILITY);

        if (maxDurability == null) {
            return -1;
        }

        return maxDurability;
    }


    private static Integer getDurabilityValue(ItemStack item, NamespacedKey durabilityKey) {
        PersistentDataContainer durabilityContainer = getDurabilityContainer(item);

        if (durabilityContainer == null) {
            return -1;
        }

        return durabilityContainer.get(durabilityKey, PersistentDataType.INTEGER);
    }

    private static PersistentDataContainer getDurabilityContainer(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().get(DURABILITY_KEY, PersistentDataType.TAG_CONTAINER);
    }

}
