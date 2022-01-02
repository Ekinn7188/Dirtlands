package net.dirtlands.listeners.shopkeepers;

import dirtlands.db.Tables;
import jeeper.utils.config.ConfigSetup;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.database.DatabaseTools;
import net.dirtlands.database.ItemSerialization;
import net.dirtlands.files.NpcInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jooq.DSLContext;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Shopkeeper implements Listener {
    
    NpcInventory npcInventory = Main.getPlugin().npcInventory();
    private static ConfigSetup config = Main.getPlugin().config();
    static DSLContext dslContext = Main.getPlugin().getDslContext();
    //player uuid, open inventory
    private static Map<UUID, Inventory> openShopMenus = new HashMap<>();

    
    @EventHandler
    public void playerInteractWithEntity(PlayerInteractEntityEvent e) {
        if (e.getHand() != EquipmentSlot.HAND){
            return;
        }
        if (!CitizensAPI.getNPCRegistry().isNPC(e.getRightClicked())){
            return;
        }
        if (e.getPlayer().isSneaking() && e.getPlayer().hasPermission(Permission.SHOPKEEPER.getName())) {
            Editor.openEditor(e);
            return;
        }
        openShop(e);
    }

    /**
     * not an event!
     * @see Shopkeeper#playerInteractWithEntity(PlayerInteractEntityEvent)
     */
    private void openShop(PlayerInteractEntityEvent e) {
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(e.getRightClicked());

        String base64 = DatabaseTools.firstString(dslContext.select(Tables.SHOPKEEPERS.INVENTORYBASE64).from(Tables.SHOPKEEPERS)
                .where(Tables.SHOPKEEPERS.SHOPKEEPERID.eq(npc.getId())).fetchAny());

        if (base64 == null) {
            return;
        }

        Inventory invSaved;
        Component invName;

        try {
            ItemSerialization shopData = ItemSerialization.fromBase64(base64);
            invSaved = shopData.getInventory();
            invName = GsonComponentSerializer.gson().deserialize(shopData.getTitle());
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        ItemStack[] items = invSaved.getContents();
        ItemStack chestItem = items[items.length-5];
        if (chestItem.getType() != Material.CHEST) {
            return;
        }
        //get the number from the lore here
        int invSize = Integer.parseInt(PlainTextComponentSerializer.plainText().serialize(chestItem.lore().get(1)).replaceAll("Editor Size: ", ""));
        Inventory inv = Bukkit.createInventory(null, invSize, invName);
        if (invSize==54) {
            for (int i = invSize-9; i < invSize; i++) {
                items[i] = Editor.editorHotbar.get(0);
            }
        } else {
            items = Arrays.copyOfRange(items, 0, items.length-9);
        }

        inv.setContents(items);
        e.getPlayer().openInventory(inv);
        openShopMenus.put(e.getPlayer().getUniqueId(), inv);
    }

    @EventHandler
    public void onShopInteract(InventoryClickEvent e) {
        if (!openShopMenus.containsKey(e.getWhoClicked().getUniqueId())) {
            return;
        }
        if (e.getClickedInventory() == null) {
            return;
        }
        //can move inventory is not a chest and if player is not shift clicking in their inventory
        if (e.getClickedInventory().getType() == InventoryType.PLAYER && !e.getClick().isShiftClick()) {
            return;
        }

        e.setCancelled(true);

/*        ItemStack item = e.getCurrentItem();
        if (item == null) {
            return;
        }
        List<Component> lore = item.lore();
        if (lore == null) {
            return;
        }

        for (Component line : lore) {
            String plainLine = PlainTextComponentSerializer.plainText().serialize(line);

            //Buy: 20 expensive diamonds
            if (plainLine.toUpperCase().startsWith("BUY: ")) {
                //20 expensive diamonds
                plainLine = plainLine.replace("BUY: ", "");
                //20
                plainLine = plainLine.substring(0, plainLine.indexOf(" "));


            } else if (plainLine.toUpperCase().startsWith("SELL: ")) {

            }
        }*/

    }

    @EventHandler
    public void onShopDrag(InventoryDragEvent e) {
        if (!openShopMenus.containsKey(e.getWhoClicked().getUniqueId())) {
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void playerCloseInventory(InventoryCloseEvent e) {
        if (openShopMenus.containsKey(e.getPlayer().getUniqueId())) {
            openShopMenus.remove(e.getPlayer().getUniqueId());
        }
    }

}
