package net.dirtlands.listeners.shopkeepers;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.dirtlands.Main;
import net.dirtlands.files.NpcInventory;
import net.dirtlands.tools.ConfigTools;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Shopkeeper implements Listener {
    
    NpcInventory npcInventory;
    
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


        npcInventory = Main.getPlugin().npcInventory();
        player.openInventory(configParser(player, npcInventory.get().getInt("Npc Ids." + npc.getId() + ".inventory")));
    }

    protected Inventory configParser(Player player, int inventoryNumber) {
        Inventory inventory;

        String inventoryPrefix = "Inventories." + inventoryNumber;

        inventory = Bukkit.createInventory(player, npcInventory.get().getInt(inventoryPrefix + ".inventory size"),
                ConfigTools.parseText(npcInventory.get().getString(inventoryPrefix + ".inventory name")));

        ConfigurationSection slots = npcInventory.get().getConfigurationSection(inventoryPrefix + ".slots");

        if (slots == null) {
            return inventory;
        }

        List<String> itemSlotsWithDefault = new ArrayList<>(slots.getKeys(false).stream().toList());

        Collections.sort(itemSlotsWithDefault, Collections.reverseOrder());


        for (String slotName : itemSlotsWithDefault) {
            Material material = Material.matchMaterial(npcInventory.get().getString(inventoryPrefix + ".slots." + slotName + ".block"));
            String name = npcInventory.get().getString(inventoryPrefix + ".slots." + slotName + ".name");
            if (name == null) {
                name = "";
            }

            String loreString = npcInventory.get().getString(inventoryPrefix + ".slots." + slotName + ".lore");
            List<Component> lore = new ArrayList<Component>();
            boolean hasLore = false;
            if (loreString != null){
                lore = Arrays.stream(loreString.split("\\|")).map(s -> ConfigTools.parseText(s)).collect(Collectors.toList());
                hasLore = true;
            }

            if (slotName.equals("default")){
                for (int j = npcInventory.get().getInt(inventoryPrefix + ".inventory size")-1; 0 <= j; j--) {
                    if (hasLore){
                        inventory.setItem(j, createGuiItem(material, name, lore.toArray(new Component[lore.size()])));
                    } else {
                        inventory.setItem(j, createGuiItem(material, name));
                    }

                }
            } else {
                if (hasLore){
                    inventory.setItem(Integer.valueOf(slotName), createGuiItem(material, name, lore.toArray(new Component[lore.size()])));
                } else {
                    inventory.setItem(Integer.valueOf(slotName), createGuiItem(material, name));
                }

            }
        }
        return inventory;
    }

    protected ItemStack createGuiItem(final Material material, final String name, final Component... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();

        meta.displayName(ConfigTools.parseText(name).decoration(TextDecoration.ITALIC, false));

        if (lore.length > 0){
            meta.lore(Arrays.stream(lore).map(c -> c.decoration(TextDecoration.ITALIC, false)).toList());
        }

        item.setItemMeta(meta);

        return item;
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        ConfigurationSection inventories = npcInventory.get().getConfigurationSection("Inventories");
        if (inventories == null){
            return;
        }

        inventories.getKeys(false).forEach(key -> {

            if (e.getView().title().equals(ConfigTools.parseText(
                    npcInventory.get().getString("Inventories." + key + ".inventory name")))) {


                npcInventory.get().getConfigurationSection("Inventories." + key + ".slots").getKeys(false).forEach(index -> {

                    ItemStack clickedStack = e.getCurrentItem();
                    if (clickedStack == null) {
                        return;
                    }
                    ItemMeta clickedMeta = clickedStack.getItemMeta();

                    Bukkit.getLogger().warning(clickedStack.toString());

                    if (clickedStack.getType() != Material.matchMaterial(npcInventory.get().getString("Inventories." + key + ".slots." + index + ".block"))){
                        return;
                    }

                    //if (clickedStack.getAmount() == /*numberAmount*/)

                    String name = npcInventory.get().getString("Inventories." + key + ".slots." + index + ".name");
                    if (name == null) {
                        name = "";
                    }

                    if (!clickedMeta.displayName().equals(ConfigTools.parseText(name).decoration(TextDecoration.ITALIC, false))) {
                        return;
                    }


                    String loreString = npcInventory.get().getString("Inventories." + key + ".slots." + index + ".lore");
                    List<Component> lore = new ArrayList<Component>();
                    boolean hasLore = false;

                    if (loreString != null){
                        lore = Arrays.stream(loreString.split("\\|")).map(s -> ConfigTools.parseText(s)).collect(Collectors.toList());
                        lore = lore.stream().map(c -> c.decoration(TextDecoration.ITALIC, false)).toList();
                        hasLore = true;
                    }

                    if (!clickedMeta.hasLore() && !hasLore || clickedMeta.lore().equals(lore)){
                        String inventoryChange = npcInventory.get().getString("Inventories." + key + ".slots." + index + ".inventory");
                        if (inventoryChange != null) {
                            e.getView().getPlayer().openInventory(configParser((Player) e.getView().getPlayer(), Integer.valueOf(inventoryChange)));
                        }

                    }


                });
                e.setCancelled(true);
            }
        });

    }

}
