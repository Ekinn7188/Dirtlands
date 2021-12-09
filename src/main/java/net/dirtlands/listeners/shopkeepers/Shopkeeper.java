package net.dirtlands.listeners.shopkeepers;

import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.dirtlands.Main;
import net.dirtlands.files.NpcInventory;
import net.dirtlands.tools.ItemTools;
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
    
    NpcInventory npcInventory = Main.getPlugin().npcInventory();
    private static ConfigSetup config = Main.getPlugin().config();
    
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

        String npcIdAsString = npcInventory.get().getString("Npc Ids." + npc.getId() + ".inventory");

        if (npcIdAsString != null){
            player.openInventory(configParser(player, Integer.parseInt(npcIdAsString)));
        }
    }

    protected Inventory configParser(Player player, int inventoryNumber) {
        Inventory inventory;

        String inventoryPrefix = "Inventories." + inventoryNumber;

        //open the inventory with slots and name to the player
        inventory = Bukkit.createInventory(player, npcInventory.get().getInt(inventoryPrefix + ".inventory size"),
                MessageTools.parseText(npcInventory.get().getString(inventoryPrefix + ".inventory name")));

        ConfigurationSection slots = npcInventory.get().getConfigurationSection(inventoryPrefix + ".slots");
        //if no items have been defined for the inventory, just stop parsing
        if (slots == null) {
            return inventory;
        }

        //all the item slots ids written in config
        List<String> itemSlotsWithDefault = new ArrayList<>(slots.getKeys(false).stream().toList());
        //make it so "default" is on top of the list of ids
        Collections.sort(itemSlotsWithDefault, Collections.reverseOrder());

        //loop through every id
        for (String slotName : itemSlotsWithDefault) {

            //get the metadata option if set
            String metadataIndexAsString = npcInventory.get().getString(inventoryPrefix + ".slots." + slotName + ".metadata");
            ItemStack itemStack = null;
            if (metadataIndexAsString != null) {
                int metadataIndex = npcInventory.get().getInt(inventoryPrefix + ".slots." + slotName + ".metadata");
                itemStack = npcInventory.get().getItemStack("Metadata." + metadataIndex);
                Bukkit.broadcast(npcInventory.get().getItemStack("Metadata." + metadataIndex).displayName());
            }



            //get the item material to put in slot
            Material material = Material.matchMaterial(npcInventory.get().getString(inventoryPrefix + ".slots." + slotName + ".block"));

            //get name of item
            String name = "test";//npcInventory.get().getString(inventoryPrefix + ".slots." + slotName + ".name");
//            if (name == null) {
//                name = "";
//            }

            //get lore to put and seperate it into a list
            String loreString = npcInventory.get().getString(inventoryPrefix + ".slots." + slotName + ".lore");
            List<Component> lore = new ArrayList<Component>();
            boolean hasLore = false;
            if (loreString != null){
                lore = Arrays.stream(loreString.split("\\|")).map(s -> MessageTools.parseText(s)).collect(Collectors.toList());
                hasLore = true;
            }

            //find how many items to put in the spot. If not set, default is 1
            int amountDisplay = -1;
            String amountInConfig = npcInventory.get().getString(inventoryPrefix + ".slots." + slotName + ".amount");
            if (amountInConfig != null){
                amountDisplay = Integer.valueOf(amountInConfig);
            }

            //if the name is default, loop through all the chest slots and create its defined item
            if (slotName.equals("default")){
                for (int j = npcInventory.get().getInt(inventoryPrefix + ".inventory size")-1; 0 <= j; j--) {
                    if (hasLore){
                        if (itemStack != null) {
                            inventory.setItem(j, ItemTools.createGuiItem(itemStack, material, MessageTools.parseText(name), amountDisplay, lore.toArray(new Component[lore.size()])));
                        } else {
                            inventory.setItem(j, ItemTools.createGuiItem(material, MessageTools.parseText(name), amountDisplay, lore.toArray(new Component[lore.size()])));
                        }
                    } else {
                        if (itemStack != null) {
                            inventory.setItem(j, ItemTools.createGuiItem(itemStack, material, MessageTools.parseText(name), amountDisplay));
                        } else {
                            inventory.setItem(j, ItemTools.createGuiItem(material, MessageTools.parseText(name), amountDisplay));
                        }
                    }

                }
            } else { //if it's not deafult, just put an item in its slot
                if (hasLore){
                    if (itemStack != null) {
                        inventory.setItem(Integer.valueOf(slotName), ItemTools.createGuiItem(itemStack, material, MessageTools.parseText(name), amountDisplay, lore.toArray(new Component[lore.size()])));
                    } else {
                        inventory.setItem(Integer.valueOf(slotName), ItemTools.createGuiItem(material, MessageTools.parseText(name), amountDisplay, lore.toArray(new Component[lore.size()])));
                    }

                } else {
                    if (itemStack != null) {
                        inventory.setItem(Integer.valueOf(slotName), ItemTools.createGuiItem(itemStack, material, MessageTools.parseText(name), amountDisplay));
                    } else {
                        inventory.setItem(Integer.valueOf(slotName), ItemTools.createGuiItem(material, MessageTools.parseText(name), amountDisplay));
                    }
                }
            }
        }
        return inventory;
    }



    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        ConfigurationSection inventories = npcInventory.get().getConfigurationSection("Inventories");
        if (inventories == null){
            return;
        }

        //get all the inventory ids
        inventories.getKeys(false).forEach(key -> {

            //if titles match an inventory, move on
            if (e.getView().title().equals(MessageTools.parseText(
                    npcInventory.get().getString("Inventories." + key + ".inventory name")))) {

                //loop through all of the item slots of the menu
                npcInventory.get().getConfigurationSection("Inventories." + key + ".slots").getKeys(false).forEach(index -> {

                    //if there is nothing clicked on, return
                    ItemStack clickedStack = e.getCurrentItem();
                    if (clickedStack == null) {
                        return;
                    }
                    ItemMeta clickedMeta = clickedStack.getItemMeta();

                    //if materials are the same in config and clicked, continue
                    if (clickedStack.getType() != Material.matchMaterial(npcInventory.get().getString("Inventories." + key + ".slots." + index + ".block"))){
                        return;
                    }


                    //if amount is the same
                    int amountDisplay = 1;
                    String amountInConfig = npcInventory.get().getString("Inventories." + key + ".slots." + index + ".amount");
                    if (amountInConfig != null){
                        amountDisplay = Integer.valueOf(amountInConfig);
                    }
                    if (clickedStack.getAmount() != amountDisplay){
                        return;
                    }

                    //if name is the same
                    String name = npcInventory.get().getString("Inventories." + key + ".slots." + index + ".name");
                    if (name == null) {
                        name = "";
                    }
                    if (!clickedMeta.displayName().equals(MessageTools.parseText(name).decoration(TextDecoration.ITALIC, false))) {
                        return;
                    }

                    //if lore is the same
                    String loreString = npcInventory.get().getString("Inventories." + key + ".slots." + index + ".lore");
                    List<Component> lore = new ArrayList<Component>();
                    boolean hasLore = false;
                    if (loreString != null) {
                        lore = Arrays.stream(loreString.split("\\|")).map(s -> MessageTools.parseText(s)).collect(Collectors.toList());
                        lore = lore.stream().map(c -> c.decoration(TextDecoration.ITALIC, false)).toList();
                        hasLore = true;
                    }
                    if (!clickedMeta.hasLore() && !hasLore || clickedMeta.lore().equals(lore)){
                        //if there's an inventory option, set on an item, bring the player to the new inventory
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
