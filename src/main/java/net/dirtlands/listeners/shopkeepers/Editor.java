package net.dirtlands.listeners.shopkeepers;

import dirtlands.db.Tables;
import jeeper.utils.MessageTools;
import net.citizensnpcs.api.CitizensAPI;
import net.dirtlands.Main;
import net.dirtlands.database.ItemSerialization;
import net.dirtlands.economy.Currency;
import net.dirtlands.tools.ItemTools;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
import org.bukkit.persistence.PersistentDataType;
import org.jooq.DSLContext;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
 * Good luck figuring out how to debug this
 */
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
    public static final List<ItemStack> editorHotbar = Stream.of(
            makeHotbarItem(generateGuiBackground()),

            ItemTools.createGuiItem(Material.BARRIER, MessageTools.parseText("&cDelete Shop"), 1),

            ItemTools.createGuiItem(Material.BOOK, MessageTools.parseText("<#856f2d>Example Item"), 1,
                    List.of(Component.text(" "),
                            MessageTools.parseText("<#856f2d>Example Item Description"),
                            Component.text(" "),
                            MessageTools.parseText("<#856f2d>Buy/Sell: Buy: 1.10")
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
    ).map(Editor::makeHotbarItem).collect(Collectors.toList());

    /**
     * not an event!
     *
     * @see Shopkeeper#playerInteractWithEntity(PlayerInteractEntityEvent)
     */
    protected static void openEditor(PlayerInteractEntityEvent e) {
        int npcID = CitizensAPI.getNPCRegistry().getNPC(e.getRightClicked()).getId();
        closingEditor.put(e.getPlayer(), new ClosedEditorData(npcID, null));

        var npcId = dslContext.selectFrom(Tables.SHOPKEEPERS).where(Tables.SHOPKEEPERS.SHOPKEEPERID.eq(npcID)).fetchOne();
        Inventory inventory;
        if (npcId != null) {
            try {
                ItemSerialization invData = ItemSerialization.fromBase64(npcId.get(Tables.SHOPKEEPERS.INVENTORYBASE64));
                inventory = Bukkit.createInventory(null, invData.getInventory().getSize(), MessageTools.parseText("Shopkeeper Editor"));
                inventory.setContents(invData.getInventory().getContents());
            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }
        } else {
            inventory = Bukkit.createInventory(null, 54, MessageTools.parseText("Shopkeeper Editor"));
        }

        for (int i = 9; i > 0; i--) {
            switch (i) {
                case 9 -> inventory.setItem(inventory.getSize() - i, editorHotbar.get(1));
                case 8 -> inventory.setItem(inventory.getSize() - i, editorHotbar.get(2));
                case 6 -> inventory.setItem(inventory.getSize() - i, editorHotbar.get(3));
                case 5 -> {
                    ItemStack item = inventory.getItem(inventory.getSize() - i);

                    if (item == null || !item.getType().equals(Material.CHEST)) {
                        inventory.setItem(inventory.getSize() - i, updatedChestLore(inventory.getSize()));
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

        if (e.getCurrentItem() != null && isHotbarItem(e.getCurrentItem())) {
            e.setCancelled(true);
            if (e.getCurrentItem().getType().equals(Material.LIME_STAINED_GLASS_PANE)) {
                var whoClickedData = closingEditor.get(e.getWhoClicked());
                whoClickedData.setSave(true);
                closingEditor.replace(e.getWhoClicked(), closingEditor.get(e.getWhoClicked()), whoClickedData);

                e.getWhoClicked().closeInventory();

            } else if (e.getCurrentItem().getType().equals(Material.RED_STAINED_GLASS_PANE)) {
                var whoClickedData = closingEditor.get(e.getWhoClicked());
                whoClickedData.setSave(false);
                closingEditor.replace(e.getWhoClicked(), closingEditor.get(e.getWhoClicked()), whoClickedData);
                e.getWhoClicked().closeInventory();
            } else if (e.getCurrentItem().getType().equals(Material.CHEST)) {
                var whoClickedData = closingEditor.get(e.getWhoClicked());
                whoClickedData.setSave(true);
                closingEditor.replace(e.getWhoClicked(), closingEditor.get(e.getWhoClicked()), whoClickedData);
                int npcID = whoClickedData.getNpc();
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
                            inv = Bukkit.createInventory(null, updatedSize, MessageTools.parseText("Shopkeeper Editor"));
                            //oldSize/size helps deal with making the inventory work smaller and bigger
                            inv.setContents(Arrays.copyOfRange(contents, 0, oldSize < updatedSize ? oldSize - 9 : updatedSize - 9));

                            ItemStack[] hotbar = Arrays.copyOfRange(contents, contents.length-9, contents.length);

                            for (int i = 0; i < 9; i++) {
                                ItemStack hotbarItem = hotbar[i];
                                if (hotbarItem.getType().equals(Material.CHEST)) {
                                    inv.setItem(inv.getSize() - 9 + i, updatedChestLore(size));
                                    continue;
                                }
                                inv.setItem(inv.getSize() - 9 + i, hotbarItem);
                            }

                            invReference.inv = inv;
                            return AnvilGUI.Response.close();
                        })
                        .onClose((Player player) ->
                                Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                                    player.openInventory(invReference.inv);
                                    //disable players from closing the new editor window
                                    closingEditor.put(e.getWhoClicked(), new ClosedEditorData(npcID, null));
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
                int npcID = whoClickedData.getNpc();
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
                        .onClose((Player player) ->
                            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                                player.openInventory(invReference.inv);
                                //disable players from closing the new editor window
                                closingEditor.put(e.getWhoClicked(), new ClosedEditorData(npcID, null));
                                //keep track of this inventory being open
                                openEditors.put(e.getWhoClicked().getUniqueId(), invReference.inv);
                            }, 5L))
                        .text("<green>New Name")
                        .title("Shop Name Editor")
                        .plugin(Main.getPlugin())
                        .open((Player) e.getWhoClicked());
            } else if (e.getCurrentItem().getType().equals(Material.BARRIER)) {
                if (closingEditor.containsKey(e.getWhoClicked())) {
                    var whoClickedData = closingEditor.get(e.getWhoClicked());

                    int npcID = whoClickedData.getNpc();

                    dslContext.deleteFrom(Tables.SHOPKEEPERS)
                            .where(Tables.SHOPKEEPERS.SHOPKEEPERID.eq(npcID)).execute();

                    whoClickedData.setSave(false);
                    closingEditor.replace(e.getWhoClicked(), closingEditor.get(e.getWhoClicked()), whoClickedData);
                    e.getWhoClicked().closeInventory();

                }
            }
            return;
        }

        if (!e.isCancelled()) {
            parseNewItem(e.getCursor());
        }


    }

    private static ItemStack updatedChestLore(int newSize) {
        ItemStack chest = editorHotbar.get(4);
        ItemMeta meta = chest.getItemMeta();
        List<Component> lore = meta.lore();
        if (lore == null) {
            return chest;
        }
        lore.set(1, ItemTools.enableItalicUsage(MessageTools.parseText("&6Editor Size: &e" + newSize)));
        meta.lore(lore);
        chest.setItemMeta(meta);
        return makeHotbarItem(chest);
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
        return makeHotbarItem(nameTag);
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
                Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> e.getInventory().setItem(key, parseNewItem(value)), 1L);
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

                    // For some reason if a message like "\<green>" gets passed, newName becomes "\\\<green>".
                    // This regex will remove 2 backslashes if they exist, so the escaped character parses correctly
                    String newName = MiniMessage.miniMessage().serialize(name).replaceAll("\\\\{1,2}<", "<");

                    meta.displayName(ItemTools.enableItalicUsage(MessageTools.parseText(newName)));
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
                if (!plainLore.contains(Currency.EXPENSIVE_TOKEN_CHARACTER + "") && !plainLore.contains(Currency.TOKEN_CHARACTER + "")) {
                    if (plainLore.toUpperCase().startsWith("BUY: ")) {
                        var temp = createSellBuyLore(plainLore, line, "Buy");
                        if (temp != null) {
                            lore.set(lore.indexOf(line), temp);
                        }
                        continue;
                    } else if (plainLore.toUpperCase().startsWith("SELL: ")) {
                        var temp = createSellBuyLore(plainLore, line, "Sell");
                        if (temp != null) {
                            lore.set(lore.indexOf(line), temp);
                        }
                        continue;
                    } else if (plainLore.equalsIgnoreCase("Carbon Copy")) {
                        lore.set(lore.indexOf(line), ItemTools.enableItalicUsage(MessageTools.parseText("<italic><#2BD5D5>Carbon Copy")));
                        continue;
                    }
                }
                lore.set(lore.indexOf(line),
                        ItemTools.enableItalicUsage(
                                MessageTools.parseText(

                                        // For some reason if a message like "\<green>" gets passed,
                                        // newName becomes "\\\<green>".
                                        // This regex will remove 2 backslashes if they exist,
                                        // so the escaped character parses correctly
                                        MiniMessage.miniMessage().serialize(line)
                                                .replaceAll("\\\\{1,2}<", "<")
                                )
                        ));
            }
            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private Component createSellBuyLore(String plainLore, Component line, String buySell) {
        String noExtraText = plainLore.toUpperCase().replaceFirst("(BUY: )|(SELL: )", "").trim();
        try {
            //Confirms that this is a double
            Double.parseDouble(noExtraText);
        } catch (NumberFormatException e) {
            return null;
        }
        // splitCost[0] == expensive tokens, splitCost[1] == token
        String[] splitCost = noExtraText.split("\\.");
        if (splitCost.length < 1) {
            return null;
        }
        if ((splitCost[0].equals("0") || splitCost[0].equals("")) ||
                (noExtraText.contains(".") && noExtraText.indexOf(".") < noExtraText.toUpperCase().indexOf(splitCost[0]))) {
            try {
                Currency currency = new Currency(0, Integer.parseInt(splitCost[1]));
                currency.convertTokensToExpensiveTokens();
                if (currency.getExpensiveTokens() > 0) {
                    if (currency.getTokens() == 0) {
                        return ItemTools.enableItalicUsage(MessageTools.parseText("<#2BD5D5>" + buySell + ": <dark_aqua>" +
                                currency.getExpensiveTokens() + " " + Currency.EXPENSIVE_TOKEN_CHARACTER));
                    } else {
                        return ItemTools.enableItalicUsage(MessageTools.parseText("<#2BD5D5>" + buySell + ": <dark_aqua>" +
                                currency.getExpensiveTokens() + " " + Currency.EXPENSIVE_TOKEN_CHARACTER + " <gold>" + currency.getTokens() + " " + Currency.TOKEN_CHARACTER));
                    }
                } else {
                    return ItemTools.enableItalicUsage(MessageTools.parseText("<#2BD5D5>" + buySell + ": <gold>" +
                            currency.getTokens() + " " + Currency.TOKEN_CHARACTER));
                }
            } catch (NumberFormatException e) {
                return ItemTools.enableItalicUsage(MessageTools.parseText("<#2BD5D5>" + buySell + ": <gold>" +
                        splitCost[0] + " " + Currency.TOKEN_CHARACTER));
            }
        }
        else if (splitCost[1].equals("0") || splitCost[1].equals("")) {
            return ItemTools.enableItalicUsage(MessageTools.parseText("<#2BD5D5>" + buySell + ": <dark_aqua>" +
                            splitCost[0] + " " + Currency.EXPENSIVE_TOKEN_CHARACTER));
        } else {
            return ItemTools.enableItalicUsage(MessageTools.parseText("<#2BD5D5>" + buySell + ": <dark_aqua>" +
                    splitCost[0] + " " + Currency.EXPENSIVE_TOKEN_CHARACTER + " <gold>" + splitCost[1] + " " + Currency.TOKEN_CHARACTER));
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (e.getInventory().getType().equals(InventoryType.CRAFTING)) {
            return;
        }
        if (closingEditor.containsKey(e.getPlayer())) {
            if (openEditors.containsKey(e.getPlayer().getUniqueId())) {
                Boolean save = closingEditor.get(e.getPlayer()).getSave();
                if (save == null) {
                    editorCooldown.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> e.getPlayer().openInventory(e.getInventory()), 5L);
                }
                else {
                   if (save) {
                       openEditors.remove(e.getPlayer().getUniqueId());
                       int npcID = closingEditor.get(e.getPlayer()).getNpc();
                       closingEditor.remove(e.getPlayer());

                       var inventoryDataRecord = dslContext.select(Tables.SHOPKEEPERS.INVENTORYBASE64)
                               .from(Tables.SHOPKEEPERS).where(Tables.SHOPKEEPERS.SHOPKEEPERID.eq(npcID)).fetchAny();

                       String inventoryData = inventoryDataRecord == null ? null : inventoryDataRecord.get(Tables.SHOPKEEPERS.INVENTORYBASE64);

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
                                   .values(npcID, ItemSerialization.toBase64(new ItemSerialization(e.getInventory(), GsonComponentSerializer.gson().serialize(name)))).execute();
                           return;
                       }

                       dslContext.update(Tables.SHOPKEEPERS)
                               .set(Tables.SHOPKEEPERS.INVENTORYBASE64, ItemSerialization.toBase64(new ItemSerialization(e.getInventory(), GsonComponentSerializer.gson().serialize(name))))
                               .where(Tables.SHOPKEEPERS.SHOPKEEPERID.eq(npcID)).execute();
                   }
                   else {
                       openEditors.remove(e.getPlayer().getUniqueId());
                       closingEditor.remove(e.getPlayer());
                   }
                }
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

    public static ItemStack generateGuiBackground() {
        ItemStack item = ItemTools.createGuiItem(Material.BLACK_STAINED_GLASS_PANE, Component.text(" "), 1);
        NamespacedKey backgroundKey = new NamespacedKey(Main.getPlugin(), "GuiBackground");
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(backgroundKey, PersistentDataType.BYTE, (byte)1);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack makeHotbarItem(ItemStack item) {
        NamespacedKey editorHotbarKey = new NamespacedKey(Main.getPlugin(), "EditorHotbar");
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(editorHotbarKey, PersistentDataType.BYTE, (byte)1);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack removeHotbarItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().remove(new NamespacedKey(Main.getPlugin(), "EditorHotbar"));
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isHotbarItem(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer()
                .has(new NamespacedKey(Main.getPlugin(), "EditorHotbar"));
    }
}

class ClosedEditorData {
    //npc id of the shopkeeper
    int npc;
    //will the editor data be saved?
    Boolean save;

    public ClosedEditorData(int npc, Boolean save) {
        this.npc = npc;
        this.save = save;
    }

    public int getNpc() {
        return npc;
    }

    public void setNpc(int npc) {
        this.npc = npc;
    }

    public Boolean getSave() {
        return save;
    }

    public void setSave(Boolean save) {
        this.save = save;
    }

}
