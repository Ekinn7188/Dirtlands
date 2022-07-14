package net.dirtlands.listeners.blocks;

import jeeper.utils.MessageTools;
import net.dirtlands.Main;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class DirtProgression implements Listener {

    public static final byte DIRT_LEVEL = -11;
    public static final byte MUD_LEVEL = -59;
    public static final List<Material> CAN_BREAK_DIRT =
            List.of(Material.STONE_SHOVEL, Material.IRON_SHOVEL, Material.DIAMOND_SHOVEL,
                    Material.NETHERITE_SHOVEL, Material.GOLDEN_SHOVEL);
    public static final List<Material> CAN_BREAK_MUD =
            List.of(Material.IRON_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL);

    public static final List<Material> DIRTY_BLOCKS =
            List.of(Material.GRASS_BLOCK, Material.COARSE_DIRT, Material.DIRT, Material.MUD, Material.MYCELIUM,
                    Material.ROOTED_DIRT, Material.PODZOL);

    @EventHandler
    public void changeGrassLoot(BlockBreakEvent e) {
        if (e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
            return;
        }if (!DIRTY_BLOCKS.contains(e.getBlock().getType())) {
            return;
        }


        if (e.getBlock().getType().equals(Material.GRASS_BLOCK)) {
            e.setDropItems(false);
            Location location = e.getBlock().getLocation();
            location.getWorld().dropItemNaturally(location, new ItemStack(Material.COARSE_DIRT, 1));
        }

        if (e.getBlock().getType().equals(Material.DIRT)) {
            if (e.getBlock().getLocation().getY() <= DIRT_LEVEL) {
                if (!CAN_BREAK_DIRT.contains(e.getPlayer().getInventory().getItemInMainHand().getType())) {
                    e.getPlayer().sendMessage(MessageTools.parseFromPath(Main.getPlugin().config(), "Cant Break Block"));
                    e.setCancelled(true);
                }
            }
        }

        if (e.getBlock().getType().equals(Material.MUD)) {
            if (e.getBlock().getLocation().getY() <= MUD_LEVEL) {
                if (!CAN_BREAK_MUD.contains(e.getPlayer().getInventory().getItemInMainHand().getType())) {
                    e.getPlayer().sendMessage(MessageTools.parseFromPath(Main.getPlugin().config(), "Cant Break Block"));
                    e.setCancelled(true);
                }
            }
        }

        e.setExpToDrop(1);
    }



}
