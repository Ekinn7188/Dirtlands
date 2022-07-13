package net.dirtlands.listeners;

import jeeper.utils.MessageTools;
import net.dirtlands.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HorseMount implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getHand() == null) {
            return;
        }
        if (e.getHand().equals(EquipmentSlot.OFF_HAND)) {
            return;
        }
        if (e.getHand().equals(EquipmentSlot.HAND)) {
            ItemStack saddle = e.getItem();

            if (saddle == null) {
                return;
            }

            if (e.getItem().getType().equals(Material.SADDLE)) {
                Player p = e.getPlayer();

                if (p.getVehicle() != null) {
                    return;
                }
                List<Component> lore = e.getItem().lore();
                if (lore == null || lore.size() < 4) {
                    return;
                }

                int speedVal = getHorseSpeed(lore.get(0));
                int jumpVal = getJumpStrength(lore.get(1));
                Horse.Color color = getColor(lore.get(2));
                Horse.Style style = getStyle(lore.get(3));

                Location loc = p.getLocation();
                // Check blocks surrounding horse to see if it can spawn
                for (int x = (int) loc.getX() - 1; x < loc.getX()+1; x++) {
                    for (int z = (int) loc.getZ() - 1; z < loc.getZ()+1; z++) {
                        for (int y = (int) loc.getY(); y < loc.getY()+2; y++) {
                            Location testLocation = new Location(loc.getWorld(),
                                    x, y, z);
                            if (testLocation.getBlock().getState().isCollidable()) {
                                p.sendMessage(MessageTools.parseFromPath(Main.getPlugin().config(),
                                        "Unsafe Spawn Location"));
                                return;
                            }
                        }
                    }
                }

                Horse horse = p.getWorld()
                        .spawn(p.getLocation(), Horse.class);

                horse.addPassenger(p);

                horse.setOwner(p);
                horse.setTamed(true);

                horse.getInventory().addItem(new ItemStack(saddle));

                horse.setColor(color);

                horse.setStyle(style);

                NamespacedKey mountKey = new NamespacedKey(Main.getPlugin(), "HorseMount");
                horse.getPersistentDataContainer().set(mountKey, PersistentDataType.BYTE, (byte)1);

                NamespacedKey speedKey = new NamespacedKey(Main.getPlugin(), "HorseSpeed");
                horse.getPersistentDataContainer().set(speedKey, PersistentDataType.INTEGER, speedVal);

                NamespacedKey jumpKey = new NamespacedKey(Main.getPlugin(), "HorseJump");
                horse.getPersistentDataContainer().set(jumpKey, PersistentDataType.INTEGER, jumpVal);

                horse.setJumpStrength(0.25D*(double)jumpVal + 0.3D);
                var speed = horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
                if (speed != null) {
                    speed.setBaseValue(0.05*speedVal);
                }
                var maxHealth = horse.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (maxHealth != null) {
                    maxHealth.setBaseValue(10.0);
                    horse.setHealth(10.0);
                }
            }
        }
    }

    private static final Map<UUID, Double> distanceBetweenLastEvent = new HashMap<>();

    @EventHandler
    public void onHorseMountMove(PlayerMoveEvent e) {
        if (e.getPlayer().getVehicle() == null) {
            return;
        }
        if (e.getPlayer().getVehicle().getType().equals(EntityType.HORSE)) {
            Horse horse = (Horse) e.getPlayer().getVehicle();
            horse.setRearing(false);

            NamespacedKey mountKey = new NamespacedKey(Main.getPlugin(), "HorseMount");

            Byte mountByte = horse.getPersistentDataContainer().get(mountKey, PersistentDataType.BYTE);

            if (mountByte == null || mountByte != 1) {
                return;
            }

            NamespacedKey speedKey = new NamespacedKey(Main.getPlugin(), "HorseSpeed");

            Integer speedVal = horse.getPersistentDataContainer().get(speedKey, PersistentDataType.INTEGER);

            if (speedVal == null) {
                return;
            }

            var speed = horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);

            if (speed == null) {
                return;
            }


            Double previousDistance = distanceBetweenLastEvent.get(e.getPlayer().getUniqueId());
            if (previousDistance == null) {
                previousDistance = 0.0;
            }

            double currentDistance = e.getFrom().distanceSquared(e.getTo());
            distanceBetweenLastEvent.put(e.getPlayer().getUniqueId(), currentDistance);

            double maxSpeed = ((double)speedVal*9D+2D)/43D;
            if (previousDistance >= 0 && previousDistance <= 0.01) {
                speed.setBaseValue(0.05*speedVal);
            }
            else if (previousDistance <= currentDistance && speed.getBaseValue() <= maxSpeed) {
                speed.setBaseValue(speed.getValue()+0.005D);
            }
            // + 0.3D to give some leeway before quickly slowing down
            else if (previousDistance > currentDistance + 0.3D && speed.getBaseValue() >= 0.05*speedVal && !horse.isJumping()) {
                speed.setBaseValue(speed.getValue()-0.01D);
            }


        }
    }

    public void onHorseDamage(EntityDamageEvent e) {
        if (e.getEntity().getType().equals(EntityType.HORSE)) {
            Horse horse = (Horse) e.getEntity();
            horse.setRearing(true);
        }
    }

    private int getHorseSpeed(Component line) {
        String text = PlainTextComponentSerializer.plainText().serialize(line);

        text = text.replaceAll("Speed: ", "");

        int speed;
        try {
            speed = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            Bukkit.getLogger().warning("Issue getting horse speed from message: " + text + ". Setting horse speed to 1.");
            speed = 1;
        }
        return speed;
    }

    private int getJumpStrength(Component line) {
        String text = PlainTextComponentSerializer.plainText().serialize(line);

        text = text.replaceAll("Jump: ", "");

        int jump;
        try {
            jump = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            Bukkit.getLogger().warning("Issue getting horse jump strength from message: " + text +
                    ". Setting horse jump strength to 1.");
            jump = 1;
        }
        return jump;
    }

    private Horse.Color getColor(Component line) {
        String text = PlainTextComponentSerializer.plainText().serialize(line);

        text = text.replaceAll("Color: ", "");
        text = text.replaceAll("\\s", "_");

        Horse.Color color;
        try {
            color = Horse.Color.valueOf(text.toUpperCase());
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("Issue getting horse color from message: " + text +
                    ". Setting horse color to dark brown.");
            color = Horse.Color.DARK_BROWN;
        }

        return color;

    }

    private Horse.Style getStyle(Component line) {
        String text = PlainTextComponentSerializer.plainText().serialize(line);

        text = text.replaceAll("Style: ", "");
        text = text.replaceAll("\\s", "_");

        Horse.Style style;
        try {
            style = Horse.Style.valueOf(text.toUpperCase());
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("Issue getting horse style from message: " + text +
                    ". Setting horse style to none.");
            style = Horse.Style.NONE;
        }

        return style;
    }



    /*
     Prevent players from right-clicking horses to equip a saddle.

     Prevent players from feeding horses
     */
    @EventHandler
    public void onHorseInteract(PlayerInteractAtEntityEvent e) {
        if (!e.getRightClicked().getType().equals(EntityType.HORSE)) {
            return;
        }

        if (e.getRightClicked().getPassengers().size() > 0) {
            e.setCancelled(true);
        }
    }

    // Prevent players from editing horse inventories
    @EventHandler
    public void horseInventoryClickEvent(InventoryClickEvent e) {
        if (e.getInventory() instanceof HorseInventory horseInventory) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onHorseDismount(EntityDismountEvent e) {
        if (!(e.getDismounted() instanceof Horse h)) {
            return;
        }
        if (!(e.getEntity() instanceof Player p)) {
            return;
        }

        h.remove();
    }

    // People can't abuse their horses for leather
    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (!(e.getEntity() instanceof Horse h)) {
            return;
        }

        if (e.getEntity().getPassengers().size() > 0) {
            e.getDrops().clear();
        }
    }
}
