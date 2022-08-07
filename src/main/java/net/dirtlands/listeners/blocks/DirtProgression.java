package net.dirtlands.listeners.blocks;

import jeeper.utils.MessageTools;
import net.dirtlands.Main;
import net.dirtlands.economy.Currency;
import org.bukkit.*;
import org.bukkit.block.ShulkerBox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.ArrayList;
import java.util.Arrays;
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


        if (e.getBlock().getType().equals(Material.GRASS_BLOCK) ||
                e.getBlock().getType().equals(Material.DIRT_PATH) ||
                e.getBlock().getType().equals(Material.MYCELIUM)) {
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

    @EventHandler
    public void dropMoneyOnDeath(PlayerDeathEvent e) {
        if (Boolean.FALSE.equals(e.getPlayer().getLocation().getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY))) {
            return;
        }
        PlayerInventory inv = e.getEntity().getInventory();
        Inventory enderChest = e.getEntity().getEnderChest();

        List<ItemStack> shulkerBoxes = new ArrayList<>();

        Location location = e.getPlayer().getLocation();

        for (ItemStack item : inv.getContents()) {
            checkMoney(inv, shulkerBoxes, location, item);
        }

        for (ItemStack item : enderChest) {
            checkMoney(inv, shulkerBoxes, location, item);
        }

        shulkerBoxes.forEach(boxItem -> {
            ShulkerBox box = (ShulkerBox) ((BlockStateMeta) boxItem.getItemMeta()).getBlockState();
            Arrays.stream(box.getInventory().getContents()).forEach((item) -> {
                if (item == null) {
                    return;
                }
                if (item.asOne().equals(Currency.EXPENSIVE_TOKEN_ITEM) || item.asOne().equals(Currency.TOKEN_ITEM)) {
                    location.getWorld().dropItemNaturally(location, item);

                    box.getInventory().remove(item);

                    int invIndex = inv.first(boxItem);
                    int enderChestIndex = inv.first(boxItem);

                    BlockStateMeta meta = (BlockStateMeta) boxItem.getItemMeta();
                    meta.setBlockState(box);
                    boxItem.setItemMeta(meta);

                    if (invIndex != -1) {
                        inv.setItem(invIndex, boxItem);
                    }
                    if (enderChestIndex != -1) {
                        enderChest.setItem(enderChestIndex, boxItem);
                    }

                }
            });
        });

    }

    private void checkMoney(PlayerInventory inv, List<ItemStack> shulkerBoxes, Location location, ItemStack item) {
        if (item == null) {
            return;
        }
        if (item.asOne().equals(Currency.EXPENSIVE_TOKEN_ITEM) || item.asOne().equals(Currency.TOKEN_ITEM)) {
            location.getWorld().dropItemNaturally(location, item);
            inv.remove(item);
        }
        if (Tag.SHULKER_BOXES.getValues().contains(item.getType())) {
            if (item.getItemMeta() instanceof BlockStateMeta) {
                BlockStateMeta blockStateMeta = (BlockStateMeta) item.getItemMeta();
                if (blockStateMeta.getBlockState() instanceof ShulkerBox) {
                    shulkerBoxes.add(item);
                }
            }
        }
    }


}
