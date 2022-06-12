package net.dirtlands.listeners.shopkeepers;

import dirtlands.db.Tables;
import jeeper.utils.MessageTools;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.dirtlands.Main;
import net.dirtlands.database.DatabaseTools;
import net.dirtlands.database.ItemSerialization;
import net.dirtlands.tools.ItemTools;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jooq.DSLContext;

import java.io.IOException;
import java.util.*;

public class Editor implements Listener {

    private static final DSLContext dslContext = Main.getPlugin().getDslContext();
    //holds all open editors
    //<UUID of player using editor, open inventory>
    private static final Map<UUID, Inventory> openEditors = new HashMap<>();
    //if there is a human entity in the list, they can close the inventory
    //<Player, (NPC Edited, Will inventory be saved?)
    private static final HashMap<HumanEntity, ClosedEditorData> closingEditor = new HashMap<>();
    //gives a 1-second cooldown before opening any inventories
    private static final Map<UUID, Long> editorCooldown = new HashMap<>();
    protected static final List<ItemStack> editorHotbar = List.of(
            ItemTools.createGuiItem(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "), 1),

            ItemTools.createGuiItem(Material.BARRIER, MessageTools.parseText("&cDelete Shop"), 1),

            ItemTools.createGuiItem(Material.BOOK, MessageTools.parseText("<#856f2d>Example Item"), 1,
                    List.of(Component.text(" "),
                            MessageTools.parseText("<#856f2d>Example Item Description"),
                            Component.text(" "),
                            MessageTools.parseText("<#856f2d>Buy/Sell: <#7c3e12>{price} <#856f2d>expensive diamonds")
                    ).toArray(new Component[0])
            ),

            ItemTools.createGuiItem(Material.RED_STAINED_GLASS_PANE, MessageTools.parseText("<dark_red>Leave Without Saving"), 1),
            ItemTools.createGuiItem(Material.CHEST, MessageTools.parseText("<gold>Adjust Inventory Size"), 1,
                    List.of(MessageTools.parseText("&7"),
                            ItemTools.enableItalicUsage(MessageTools.parseText("&6Editor Size:&e unknown"))).toArray(new Component[0])),
            ItemTools.createGuiItem(Material.LIME_STAINED_GLASS_PANE, MessageTools.parseText("<green>Leave And Save"), 1),
            ItemTools.createGuiItem(Material.NAME_TAG, MessageTools.parseText("<red>Edit Shop Name"), 1,
                    List.of(MessageTools.parseText("&7"),
                            ItemTools.enableItalicUsage(MessageTools.parseText("<red>Name: <dark_red>unknown"))).toArray(new Component[0]))
    );

    /**
     * not an event!
     *
     * @see Shopkeeper#playerInteractWithEntity(PlayerInteractEntityEvent)
     */
    protected static void openEditor(PlayerInteractEntityEvent e) {
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(e.getRightClicked());
        closingEditor.put(e.getPlayer(), new ClosedEditorData(npc, false));

        var npcId = dslContext.selectFrom(Tables.SHOPKEEPERS).where(Tables.SHOPKEEPERS.SHOPKEEPERID.eq(npc.getId())).fetchOne();
        Inventory inventory;
        if (npcId != null) {
            try {
                ItemSerialization invData = ItemSerialization.fromBase64(npcId.get(Tables.SHOPKEEPERS.INVENTORYBASE64));
                inventory = Bukkit.createInventory(null, invData.getInventory().getSize(), MessageTools.parseText("Shopkeeper Editor (Page 1)"));
                inventory.setContents(invData.getInventory().getContents());
            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }
        } else {
            inventory = Bukkit.createInventory(null, 54, MessageTools.parseText("Shopkeeper Editor (Page 1)"));
        }

        for (int i = 9; i > 0; i--) {
            switch (i) {
                case 9 -> inventory.setItem(inventory.getSize() - i, editorHotbar.get(1));
                case 8 -> inventory.setItem(inventory.getSize() - i, editorHotbar.get(2));
                case 6 -> inventory.setItem(inventory.getSize() - i, editorHotbar.get(3));
                case 5 -> {
                    ItemStack item = inventory.getItem(inventory.getSize() - i);

                    if (item == null || !item.getType().equals(Material.CHEST)) {
                        inventory.setItem(inventory.getSize() - i, getChestNewLore(inventory.getSize()));
                    }
                }
                case 4 -> inventory.setItem(inventory.getSize() - i, editorHotbar.get(5));
                case 2 -> {
                    ItemStack item = inventory.getItem(inventory.getSize() - i);
                    //if name is null or is not a name tag, rename the item
                    if (item == null || !item.getType().equals(Material.NAME_TAG)) {
                        inventory.setItem(inventory.getSize() - i, getNewShopName(MessageTools.parseText("<gold>Shop")));
                    }
                }
                //set to glass if not a special item
                default -> inventory.setItem(inventory.getSize() - i, editorHotbar.get(0));
            }
        }


        openEditors.put(e.getPlayer().getUniqueId(), inventory);
        e.getPlayer().openInventory(inventory);
    }

    @EventHandler
    public void onEditorInteract(InventoryClickEvent e) {
        if (!openEditors.containsKey(e.getWhoClicked().getUniqueId())) {
            return;
        }
        if (e.getClickedInventory() == null || e.getClickedInventory().getType().equals(InventoryType.PLAYER)) {
            return;
        }


        if (e.getCurrentItem() != null) {
            for (ItemStack item : editorHotbar) {
                ItemMeta hotbarMeta = item.getItemMeta();
                ItemStack itemCopy = new ItemStack(item);
                if (hotbarMeta != null) {
                    itemCopy.lore(null);
                }
                ItemStack clickedCopy = new ItemStack(e.getCurrentItem());
                if (clickedCopy.getItemMeta() != null) {
                    clickedCopy.lore(null);
                }

                if (clickedCopy.equals(itemCopy)) {
                    e.setCancelled(true);
                    if (e.getCurrentItem().getType().equals(Material.LIME_STAINED_GLASS_PANE)) {
                        var whoClickedData = closingEditor.get(e.getWhoClicked());
                        whoClickedData.setSave(true);
                        closingEditor.replace(e.getWhoClicked(), closingEditor.get(e.getWhoClicked()), whoClickedData);
                        openEditors.remove(e.getWhoClicked().getUniqueId());
                        e.getWhoClicked().closeInventory();

                    } else if (e.getCurrentItem().getType().equals(Material.RED_STAINED_GLASS_PANE)) {
                        var whoClickedData = closingEditor.get(e.getWhoClicked());
                        whoClickedData.setSave(true);
                        closingEditor.replace(e.getWhoClicked(), closingEditor.get(e.getWhoClicked()), whoClickedData);
                        e.getWhoClicked().closeInventory();
                    } else if (e.getCurrentItem().getType().equals(Material.CHEST)) {
                        var whoClickedData = closingEditor.get(e.getWhoClicked());
                        whoClickedData.setSave(true);
                        closingEditor.replace(e.getWhoClicked(), closingEditor.get(e.getWhoClicked()), whoClickedData);
                        NPC npc = whoClickedData.getNpc();
                        var invReference = new Object() {
                            Inventory inv = e.getClickedInventory();
                        };
                        new AnvilGUI.Builder()
                                .onComplete((player, text) -> {
                                    Inventory inv = invReference.inv;
                                    int size;
                                    int updatedSize;
                                    try {
                                        size = Integer.parseInt(text);
                                        updatedSize = (size == 54 ? size : size + 9);
                                        if (size % 9 != 0) {
                                            throw new NumberFormatException();
                                        }
                                    } catch (NumberFormatException ex) {
                                        player.sendMessage(MessageTools.parseFromPath(Main.getPlugin().config(), "Invalid Number"));
                                        return AnvilGUI.Response.close();
                                    }

                                    ItemStack[] contents = inv.getContents();
                                    int oldSize = inv.getSize();
                                    inv = Bukkit.createInventory(null, updatedSize, MessageTools.parseText("Shopkeeper Editor (Page 1)"));
                                    //oldSize/size helps deal with making the inventory work smaller and bigger
                                    inv.setContents(Arrays.copyOfRange(contents, 0, oldSize < updatedSize ? oldSize - 9 : updatedSize - 9));

                                    for (int i = 0; i < 9; i++) {
                                        inv.setItem(i + updatedSize - 9, editorHotbar.get(0));
                                    }

                                    inv.setItem(updatedSize - 9, editorHotbar.get(1));
                                    inv.setItem(updatedSize - 8, editorHotbar.get(2));
                                    inv.setItem(updatedSize - 6, editorHotbar.get(3));
                                    inv.setItem(updatedSize - 5, getChestNewLore(size));
                                    inv.setItem(updatedSize - 4, editorHotbar.get(5));
                                    inv.setItem(updatedSize - 2, editorHotbar.get(6));
                                    inv.setItem(updatedSize - 1, editorHotbar.get(7));

                                    invReference.inv = inv;
                                    return AnvilGUI.Response.close();
                                })
                                .onClose((Player player) ->
                                        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                                            player.openInventory(invReference.inv);
                                            //disable players from closing the new editor window
                                            closingEditor.put(e.getWhoClicked(), new ClosedEditorData(npc, false));
                                            //keep track of this inventory being open
                                            openEditors.put(e.getWhoClicked().getUniqueId(), invReference.inv);
                                        }, 5L))
                                .text("9, 18, 27, 36, 45, 54")
                                .title("Shop Size Editor")
                                .plugin(Main.getPlugin())
                                .open((Player) e.getWhoClicked());
                    } else if (e.getCurrentItem().getType().equals(Material.NAME_TAG)) {
                        var whoClickedData = closingEditor.get(e.getWhoClicked());
                        whoClickedData.setSave(true);
                        closingEditor.replace(e.getWhoClicked(), closingEditor.get(e.getWhoClicked()), whoClickedData);
                        NPC npc = whoClickedData.getNpc();
                        var invReference = new Object() {
                            Inventory inv = e.getClickedInventory();
                        };
                        new AnvilGUI.Builder()
                                .onComplete((player, text) -> {
                                    Inventory inv = invReference.inv;
                                    Component name = MessageTools.parseText(text);
                                    inv.setItem(inv.getSize() - 2, getNewShopName(name));
                                    invReference.inv = inv;
                                    return AnvilGUI.Response.close();
                                })
                                .onClose((Player player) -> {
                                    player.openInventory(invReference.inv);
                                    //disable players from closing the new editor window
                                    closingEditor.put(e.getWhoClicked(), new ClosedEditorData(npc, false));
                                    //keep track of this inventory being open
                                    openEditors.put(e.getWhoClicked().getUniqueId(), invReference.inv);
                                })
                                .text("<green>New Name")
                                .title("Shop Name Editor")
                                .plugin(Main.getPlugin())
                                .open((Player) e.getWhoClicked());
                    } else if (e.getCurrentItem().getType().equals(Material.BARRIER)) {
                        if (closingEditor.containsKey(e.getWhoClicked())) {
                            var whoClickedData = closingEditor.get(e.getWhoClicked());

                            NPC npc = whoClickedData.getNpc();

                            dslContext.deleteFrom(Tables.SHOPKEEPERS)
                                    .where(Tables.SHOPKEEPERS.SHOPKEEPERID.eq(npc.getId())).execute();

                            whoClickedData.setSave(true);
                            closingEditor.replace(e.getWhoClicked(), closingEditor.get(e.getWhoClicked()), whoClickedData);
                            e.getWhoClicked().closeInventory();

                        }
                    }
                    return;
                }
            }
        }


        if (!e.isCancelled()) {
            parseNewItem(e.getCursor());
        }


    }

    private static ItemStack getChestNewLore(int newSize) {
        ItemStack chest = editorHotbar.get(4);
        ItemMeta meta = chest.getItemMeta();
        List<Component> lore = meta.lore();
        if (lore == null) {
            return chest;
        }
        lore.set(1, ItemTools.enableItalicUsage(MessageTools.parseText("&6Editor Size: &e" + newSize)));
        meta.lore(lore);
        chest.setItemMeta(meta);
        return chest;
    }

    private static ItemStack getNewShopName(Component name) {
        ItemStack nameTag = editorHotbar.get(6);
        ItemMeta meta = nameTag.getItemMeta();
        List<Component> lore = meta.lore();
        if (lore == null) {
            return nameTag;
        }
        lore.set(1, ItemTools.enableItalicUsage(MessageTools.parseText("<red>Name: <reset><name>", Placeholder.component("name", name))));
        meta.lore(lore);
        nameTag.setItemMeta(meta);
        return nameTag;
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (!openEditors.containsKey(e.getWhoClicked().getUniqueId())) {
            return;
        }
        if (e.getInventory().getType().equals(InventoryType.PLAYER)) {
            return;
        }
        e.getNewItems().forEach((key, value) -> {
            if (e.getInventory().getSize() > key) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> e.getInventory().setItem(key, parseNewItem(value)), 10L);
            }
        });
    }

    private ItemStack parseNewItem(ItemStack item) {
        if (item != null && !item.getType().equals(Material.AIR)) {
            Component name = item.getItemMeta().displayName();
            if (name != null) {
                String plainName = PlainTextComponentSerializer.plainText().serialize(name);
                if (!plainName.equals(PlainTextComponentSerializer.plainText().serialize(MessageTools.parseText(plainName)))) {

                    ItemMeta meta = item.getItemMeta();

                    meta.displayName(ItemTools.enableItalicUsage(MessageTools.parseText(MiniMessage.miniMessage().serialize(name))));
                    item.setItemMeta(meta);

                }
            }

            List<Component> lore = item.getItemMeta().lore();
            if (lore == null) {
                return item;
            }
            ItemMeta meta = item.getItemMeta();
            for (Component line : lore) {
                String plainLore = PlainTextComponentSerializer.plainText().serialize(line);
                if (plainLore.toUpperCase().startsWith("BUY: ")) {
                    String noBuy = plainLore.substring(5);
                    lore.set(lore.indexOf(line), ItemTools.enableItalicUsage(MessageTools.parseText("<#7c3e12>Buy: <#b8a567>" + noBuy)));
                    continue;
                } else if (plainLore.toUpperCase().startsWith("SELL: ")) {
                    String noSell = plainLore.substring(6);
                    lore.set(lore.indexOf(line), ItemTools.enableItalicUsage(MessageTools.parseText("<#7c3e12>Sell: <#b8a567>" + noSell)));
                    continue;
                } else if (plainLore.equalsIgnoreCase("Carbon Copy")) {
                    lore.set(lore.indexOf(line), ItemTools.enableItalicUsage(MessageTools.parseText("<italic><#b8a567>Carbon Copy")));
                    continue;
                }
                lore.set(lore.indexOf(line), ItemTools.enableItalicUsage(MessageTools.parseText(MiniMessage.miniMessage().serialize(line))));
            }
            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (closingEditor.containsKey(e.getPlayer())) {
            if (closingEditor.get(e.getPlayer()).getSave()) {
                if (!openEditors.containsKey(e.getPlayer().getUniqueId())) {
                    NPC npc = closingEditor.get(e.getPlayer()).getNpc();
                    closingEditor.remove(e.getPlayer());

                    var inventoryData = DatabaseTools.firstString(dslContext.select(Tables.SHOPKEEPERS.INVENTORYBASE64).from(Tables.SHOPKEEPERS).where(Tables.SHOPKEEPERS.SHOPKEEPERID.eq(npc.getId())).fetchAny());

                    //get the inventory name from the lore of the name tag in slot inventory.getSize() - 2
                    Component name = MessageTools.parseText("<gold>Shop");

                    ItemStack nameTag = e.getInventory().getItem(e.getInventory().getSize() - 2);
                    if (nameTag != null && nameTag.getType() == Material.NAME_TAG) {
                        ItemMeta meta = nameTag.getItemMeta();
                        List<Component> lore = meta.lore();
                        if (lore != null) {
                            name = lore.get(1).replaceText(x -> x.match("Name: ").replacement(Component.empty()));
                        }
                    }

                    if (inventoryData == null) {
                        dslContext.insertInto(Tables.SHOPKEEPERS)
                                .columns(Tables.SHOPKEEPERS.SHOPKEEPERID, Tables.SHOPKEEPERS.INVENTORYBASE64)
                                .values(npc.getId(), ItemSerialization.toBase64(new ItemSerialization(e.getInventory(), GsonComponentSerializer.gson().serialize(name)))).execute();
                        return;
                    }

                    dslContext.update(Tables.SHOPKEEPERS)
                            .set(Tables.SHOPKEEPERS.INVENTORYBASE64, ItemSerialization.toBase64(new ItemSerialization(e.getInventory(), GsonComponentSerializer.gson().serialize(name))))
                            .where(Tables.SHOPKEEPERS.SHOPKEEPERID.eq(npc.getId())).execute();
                } else {
                    closingEditor.remove(e.getPlayer());
                }
            } else {
                editorCooldown.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
                Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> e.getPlayer().openInventory(e.getInventory()), 5L);
            }
        }


    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (!editorCooldown.containsKey(e.getPlayer().getUniqueId())) {
            return;
        }
        if (!openEditors.containsKey(e.getPlayer().getUniqueId())) {
            return;
        }
        if(openEditors.get(e.getPlayer().getUniqueId()).equals(e.getInventory())) {
            return;
        }
        //a bit less than 5 ticks
        long secondsLeft = editorCooldown.get(e.getPlayer().getUniqueId()) + 225 - System.currentTimeMillis();
        if (secondsLeft > 0) {
            e.getPlayer().sendMessage(MessageTools.parseFromPath(Main.getPlugin().config(), "Quick Inventory"));
            e.setCancelled(true);
        }
    }
}

class ClosedEditorData {
    //npc editor that will be closed
    NPC npc;
    //will the editor data be saved?
    boolean save;

    public ClosedEditorData(NPC npc, boolean save) {
        this.npc = npc;
        this.save = save;
    }

    public NPC getNpc() {
        return npc;
    }

    public void setNpc(NPC npc) {
        this.npc = npc;
    }

    public boolean getSave() {
        return save;
    }

    public void setSave(boolean save) {
        this.save = save;
    }

}
