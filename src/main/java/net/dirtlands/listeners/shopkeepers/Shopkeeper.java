package net.dirtlands.listeners.shopkeepers;

import dirtlands.db.Tables;
import jeeper.utils.MessageTools;
import jeeper.utils.config.Config;
import net.citizensnpcs.api.CitizensAPI;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.database.ItemSerialization;
import net.dirtlands.economy.Economy;
import net.dirtlands.listeners.shopkeepers.custom.shops.HorseShop;
import net.dirtlands.tools.ItemTools;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.wesjd.anvilgui.AnvilGUI;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jooq.DSLContext;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/*
 * Good luck figuring out how to debug this
 */
public class Shopkeeper implements Listener {

    private static Config config = Main.getPlugin().config();
    static DSLContext dslContext = Main.getPlugin().getDslContext();
    //player uuid, open inventory
    public static Map<UUID, Inventory> openShopMenus = new HashMap<>();

    private static final ItemStack BLACK_GLASS_ITEM = Editor.generateGuiBackground();
    private static final ItemStack NAME_TAG_ITEM = ItemTools.createGuiItem(Material.NAME_TAG, ItemTools.enableItalicUsage(MessageTools.parseText("&cCustom Amount")), 1);
    private static final ItemStack EMERALD_BLOCK_ITEM = ItemTools.createGuiItem(Material.EMERALD_BLOCK, ItemTools.enableItalicUsage(MessageTools.parseText("<green>Sell All")), 1);


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
    int npcID = CitizensAPI.getNPCRegistry().getNPC(e.getRightClicked()).getId();

        var base64Record = dslContext.select(Tables.SHOPKEEPERS.INVENTORYBASE64).from(Tables.SHOPKEEPERS)
                .where(Tables.SHOPKEEPERS.SHOPKEEPERID.eq(npcID)).fetchAny();

        String base64 = base64Record == null ? null : base64Record.get(Tables.SHOPKEEPERS.INVENTORYBASE64);

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
                items[i] = Editor.removeHotbarItem(Editor.editorHotbar.get(0));
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

        ItemStack item = e.getCurrentItem();
        if (item == null) {
            if (e.getCursor() != null) {
                e.setCancelled(true);
            }
            return;
        }
        if (item.getType() == Material.AIR) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (meta.displayName() == null) {
                meta.displayName(ItemTools.enableItalicUsage(Component.text(WordUtils.capitalizeFully(item.getType().name().replaceAll("_", " ")))));
                item.setItemMeta(meta);
                e.setCurrentItem(item);
            }
        }

        e.setCancelled(true);

        if (!PlainTextComponentSerializer.plainText().serialize(e.getView().title()).contains("Buy or Sell")) {
            if (PlainTextComponentSerializer.plainText().serialize(e.getView().title()).contains("Buy ") ||
                    PlainTextComponentSerializer.plainText().serialize(e.getView().title()).contains("Sell ")) {
                tradeMenuAction(e);
                return;
            }
        }

        if (HorseShop.isHorseShopItem(e.getCurrentItem())) {
            return;
        }

        openBuySellMenu(e);
    }

    private static Pattern quantityChecker = Pattern.compile("(\\d+)x\\s*");
    private static Pattern trimComponentWhitespace = Pattern.compile("^[ \\t]+|[ \\t]+$.");
    /**
     * not an event!
     * @see Shopkeeper#onShopInteract(InventoryClickEvent)
     */
    public static void openBuySellMenu(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();

        if (item == null || item.getItemMeta() == null) {
            return;
        }
        ItemMeta meta = item.getItemMeta();

        if (PlainTextComponentSerializer.plainText().serialize(e.getView().title()).contains("Buy or Sell ")) {
            if (e.getSlot() >= 2 && e.getSlot() < e.getInventory().getSize()-2) {
                String itemName = PlainTextComponentSerializer.plainText().serialize(meta.displayName());

                if (item.getType().equals(Material.EMERALD) && itemName.contains("Sell ")) {
                    item = e.getInventory().getItem(e.getSlot()+2);
                    //remove opposing price
                    List<Component> lore = item.lore().stream().filter(l -> !PlainTextComponentSerializer.plainText().serialize(l).contains("Buy: ")).collect(Collectors.toList());
                    item.lore(lore);
                    e.setCurrentItem(item);
                    meta = item.getItemMeta();
                } else if (item.getType().equals(Material.NAME_TAG) && itemName.contains("Buy ")) {
                    item = e.getInventory().getItem(e.getSlot()-2);
                    List<Component> lore = item.lore().stream().filter(l -> !PlainTextComponentSerializer.plainText().serialize(l).contains("Sell: ")).collect(Collectors.toList());
                    item.lore(lore);
                    e.setCurrentItem(item);
                    meta = item.getItemMeta();
                }
            }
        }






        String plainName = PlainTextComponentSerializer.plainText().serialize(meta.displayName());
        Matcher matcher = quantityChecker.matcher(plainName);
        if (matcher.find()) {
            Component oldName = meta.displayName();


            plainName = plainName.replace(matcher.group(), "");
            Component newName = oldName.replaceText(x -> x.match(quantityChecker).replacement(Component.empty()));
            newName = newName.replaceText(x -> x.match(trimComponentWhitespace).replacement(Component.empty()));

            meta.displayName(newName);
            item.setItemMeta(meta);
        }

        List<Component> lore = item.lore();
        if (lore == null) {
            return;
        }



        int buyLoreIndex = -1, sellLoreIndex = -1;

        for (int i = 0 ; i < lore.size(); i++) {
            String plainLine = PlainTextComponentSerializer.plainText().serialize(lore.get(i)).toUpperCase();

            //Buy: 20 expensive diamonds
            if (plainLine.startsWith("BUY: ")) {
                buyLoreIndex = i;
            } else if (plainLine.toUpperCase().startsWith("SELL: ")) {
                sellLoreIndex = i;
            }
        }



        if (buyLoreIndex != -1 && sellLoreIndex != -1) {
            Inventory buyOrSell = Bukkit.createInventory(null, 27, MessageTools.parseText("Buy or Sell <name>", Placeholder.component("name", meta.displayName())));
            for (int i = 0; i < buyOrSell.getSize(); i++) {
                buyOrSell.setItem(i, BLACK_GLASS_ITEM);
            }

            buyOrSell.setItem(11, ItemTools.createGuiItem(Material.EMERALD, MessageTools.parseText("<green>Sell <name>", Placeholder.component("name", meta.displayName())), 1));
            buyOrSell.setItem(13, e.getCurrentItem());
            buyOrSell.setItem(15, ItemTools.createGuiItem(Material.NAME_TAG, MessageTools.parseText("<green>Buy <name>", Placeholder.component("name", meta.displayName())), 1));

            e.getWhoClicked().openInventory(buyOrSell);
            openShopMenus.put(e.getWhoClicked().getUniqueId(), buyOrSell);
        } else if (buyLoreIndex != -1 || sellLoreIndex != -1) {
            SellData data = getSellData(e.getWhoClicked(), new ItemStack(item));
            //not sellable item
            if (data == null) {
                return;
            }

            int generalIndex = buyLoreIndex != -1 ? buyLoreIndex : sellLoreIndex;
            Inventory shopMenu = Bukkit.createInventory(null, 45, MessageTools.parseText(data.getBuyOrSell() + " <name>", Placeholder.component("name", meta.displayName())));
            for (int i = 0; i < shopMenu.getSize(); i++) {
                shopMenu.setItem(i, BLACK_GLASS_ITEM);
            }

            ItemStack itemDisplay = item.asOne();

            ItemMeta displayMeta = itemDisplay.getItemMeta();

            List<Component> displayLore = displayMeta.lore();
            if (generalIndex != 0 && PlainTextComponentSerializer.plainText().serialize(displayLore.get(generalIndex-1)).trim().equals("")) {
                displayLore.remove(generalIndex - 1);
                displayLore.remove(generalIndex-1);
            } else {
                displayLore.remove(generalIndex);
            }

            if (data.isCarbonCopy()) {
                displayLore.set(displayLore.indexOf(data.getCarbonCopyLine()), ItemTools.enableItalicUsage(MessageTools.parseText("&r<italic><#2BD5D5>Carbon Copy")));
            }

            displayMeta.lore(displayLore.stream().map(ItemTools::enableItalicUsage).collect(Collectors.toList()));

            itemDisplay.setItemMeta(displayMeta);

            shopMenu.setItem(13, itemDisplay);

            if (item.getMaxStackSize() == 64 || item.getMaxStackSize() == 16) {
                shopMenu.setItem(29, newPriceBuyItem(data.getBuyOrSell(), item, data.getPrice(), generalIndex, 1, data.getCarbonCopyLine()));
                shopMenu.setItem(30, newPriceBuyItem(data.getBuyOrSell(), item, data.getPrice(), generalIndex, item.getMaxStackSize() / (item.getMaxStackSize() == 64 ? 8 : 4), data.getCarbonCopyLine()));
                shopMenu.setItem(31, newPriceBuyItem(data.getBuyOrSell(), item, data.getPrice(), generalIndex,item.getMaxStackSize()/2, data.getCarbonCopyLine()));
                shopMenu.setItem(32, newPriceBuyItem(data.getBuyOrSell(), item, data.getPrice(), generalIndex,item.getMaxStackSize(), data.getCarbonCopyLine()));
            } else {
                shopMenu.setItem(29, newPriceBuyItem(data.getBuyOrSell(), item, data.getPrice(), generalIndex, 1, data.getCarbonCopyLine()));
            }
            if (data.getBuyOrSell().equalsIgnoreCase("Buy")) {
                shopMenu.setItem(33, NAME_TAG_ITEM);
            } else {
                //set lore for item
                shopMenu.setItem(33, newPriceBuyItem(data.getBuyOrSell(), EMERALD_BLOCK_ITEM, data.getPrice(), generalIndex, getNumberOfItems(e.getWhoClicked().getOpenInventory().getBottomInventory(), noCarbonCopyLore(itemDisplay, data), data.isCarbonCopy()), data.getCarbonCopyLine()).asQuantity(1));
            }

            e.getWhoClicked().openInventory(shopMenu);
            openShopMenus.put(e.getWhoClicked().getUniqueId(), shopMenu);

        }
    }


    private static ItemStack newPriceBuyItem(String buyOrSell, ItemStack item, int currentPrice, int loreIndex, int quantity, Component carbonCopyLine) {
        int newPrice = currentPrice * quantity;
        try {
            item = item.asQuantity(quantity);
        } catch (Exception ex) {
            item = item.asOne();
        }

        List<Component> lore = item.lore();
        if (lore == null) {
            lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.add(ItemTools.enableItalicUsage(MessageTools.parseText("&r<#2BD5D5>" + buyOrSell + ": <aqua>" + newPrice + " <dark_aqua><bold>❖")));
            item.lore(lore);
            return item;
        }

        lore.set(loreIndex, ItemTools.enableItalicUsage(MessageTools.parseText("&r<#2BD5D5>" + buyOrSell + ": <aqua>" + newPrice + " <dark_aqua><bold>❖")));
        if (carbonCopyLine != null) {
            lore.set(lore.indexOf(carbonCopyLine), ItemTools.enableItalicUsage(MessageTools.parseText("&r<italic><#2BD5D5>Carbon Copy")));
        }
        item.lore(lore);


        ItemMeta meta = item.getItemMeta();
        Component itemName = meta.displayName();

        Component newName = ItemTools.enableItalicUsage(MessageTools.parseText("<reset><gray>" + quantity + "x</gray></reset> <name>", Placeholder.component("name", itemName)));
        meta.displayName(newName);

        item.setItemMeta(meta);
        return item;
    }



    private void tradeMenuAction(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType().equals(Material.AIR) || item.equals(BLACK_GLASS_ITEM)) {
            return;
        }
        ItemStack itemToParse = new ItemStack(item);
        boolean customAmount = false;

        if (item.getType().equals(Material.EMERALD_BLOCK) || item.equals(NAME_TAG_ITEM)) {
            ItemMeta itemMeta = itemToParse.getItemMeta();
            if (itemMeta != null){
                String plainName = PlainTextComponentSerializer.plainText().serialize(itemMeta.displayName());
                if (plainName.contains("Sell All") || plainName.contains("Custom Amount")) {
                    if (e.getSlot() > 0) {
                        ItemStack itemBefore = e.getInventory().getItem(e.getSlot() - 1);
                        if (itemBefore.getType().equals(Material.BLACK_STAINED_GLASS_PANE)) {
                            itemBefore = e.getInventory().getItem(e.getSlot() - 4);
                        }
                        if (itemBefore != null) {
                            itemToParse = new ItemStack(itemBefore);
                            itemToParse = itemBefore;
                            customAmount = true;
                        }
                    }
                }
            }
        }

        SellData data = getSellData(e.getWhoClicked(), itemToParse);

        //not a sellable item!
        if (data == null) {
            return;
        }

        itemToParse = noCarbonCopyLore(e.getInventory().getItem(13), data);

        int numberOfItems = customAmount ? Integer.MAX_VALUE : item.getAmount();

        //clone is only used to help get the amount of items
        Material material = itemToParse.getType();
        ItemStack itemToAdd = data.isCarbonCopy() ? itemToParse : new ItemStack(material, numberOfItems);

        //normal trade
        if (data.getBuyOrSell().equalsIgnoreCase("Buy")) {
            if (customAmount) {
                //buy custom amount
                customBuy(e.getClickedInventory(), e.getWhoClicked(), itemToAdd, data.getPrice(), data.isCarbonCopy());
                return;
            }
            buyItems(e.getWhoClicked(), itemToAdd, data.getPrice(), data.getItem().getAmount(), data.isCarbonCopy());
            return;
        }


        Inventory inv = e.getWhoClicked().getInventory();

        sellItems((OfflinePlayer) e.getWhoClicked(), inv, itemToParse, numberOfItems, data.getPrice(), data.isCarbonCopy());

        e.setCurrentItem(new ItemStack(e.getInventory().getItem(29)));
        openBuySellMenu(e);

    }

    private static ItemStack noCarbonCopyLore(ItemStack itemtoParse, SellData data) {
        ItemStack result = new ItemStack(itemtoParse);
        ItemMeta topItemMeta = result.getItemMeta();
        if (topItemMeta != null) {
            String unParsedName = MiniMessage.miniMessage().serialize(topItemMeta.displayName());

            ItemMeta parseMeta = result.getItemMeta();
            parseMeta.displayName(ItemTools.enableItalicUsage(MessageTools.parseText(unParsedName)));
            result.setItemMeta(parseMeta);
        }
        //remove price/carbon copy lore from item
        if (data.isCarbonCopy()) {
            List<Component> loreNoCopy = result.lore();

            loreNoCopy = loreNoCopy.stream()
                    .filter(line -> !PlainTextComponentSerializer.plainText().serialize(line).toUpperCase().contains("CARBON COPY")).collect(Collectors.toList());

            result.lore(loreNoCopy);
        }

        return result;
    }

    private void customBuy(Inventory currentInv, HumanEntity buyer, ItemStack item, int pricePerUnit, boolean isCarbonCopy) {
        new AnvilGUI.Builder()
                .onComplete((player, text) -> {
                    openShopMenus.put(buyer.getUniqueId(), currentInv);
                    int quanity = 0;
                    try {
                        quanity = Integer.parseInt(text);
                        if (quanity < 1) {
                            throw new NumberFormatException();
                        }
                    } catch (NumberFormatException e) {
                        buyer.sendMessage(MessageTools.parseFromPath(config, "Invalid Number"));
                        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> customBuy(currentInv, buyer, item, pricePerUnit, isCarbonCopy), 5L);
                        return AnvilGUI.Response.close();
                    }

                    Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                        buyer.openInventory(currentInv);
                        openShopMenus.put(buyer.getUniqueId(), currentInv);
                    }, 5L);

                    buyItems(buyer, item, pricePerUnit, quanity, isCarbonCopy);
                    return AnvilGUI.Response.close();
                })
                .title("Buy Custom Amount")
                .plugin(Main.getPlugin())
                .itemLeft(item.asQuantity(1))
                .open((Player) buyer);
        return;
    }

    private void buyItems(HumanEntity buyer, ItemStack item, int pricePerUnit, int quantity, boolean isCarbonCopy) {
        if (canFitItem(buyer.getInventory(), item, quantity)) {
            if (Economy.removeMoney((OfflinePlayer) buyer, pricePerUnit* quantity)) {
                for (int i = 0; i < Math.ceil((double)quantity/(double)item.getMaxStackSize()); i++) {
                    if (quantity < 0) {
                        break;
                    }
                    if (quantity > item.getMaxStackSize()) {
                        buyer.getInventory().addItem(item.asQuantity(item.getMaxStackSize()));
                        continue;
                    }

                    buyer.getInventory().addItem(item.asQuantity(quantity));
                }
                return;
            }
            buyer.sendMessage(MessageTools.parseFromPath(config, "Cant Afford Message"));
            return;
        }
        buyer.sendMessage(MessageTools.parseFromPath(config, "No Space"));
        return;
    }

    private static boolean sellItems(OfflinePlayer player, Inventory inv, ItemStack item, int amount, int price, boolean carbonCopy) {

        //count everything to make sure how many can be sold, ignored if max int size because it would sell all of them no matter what
        if (amount != Integer.MAX_VALUE) {
            int totalItems = getNumberOfItems(inv, item, carbonCopy);
            if (totalItems < amount) {
                return true;
            }
        }

        ItemStack itemCopy = new ItemStack(item);
        ItemMeta itemCopyMeta = itemCopy.getItemMeta();
        if (itemCopyMeta != null) {
            itemCopyMeta.displayName(Component.text(""));
            if (!carbonCopy) {
                itemCopyMeta.lore(null);
            }
            itemCopy.setItemMeta(itemCopyMeta);
        }

        ItemStack[] slots = inv.getContents();
        HashMap<Integer, ItemStack> mappedItems = new HashMap<>();
        for (int i = 0; i < slots.length ; i++) {
            ItemStack slot = slots[i];
            if ((slot != null) && (slot.getType() != Material.AIR)) {
                ItemStack slotCopy = new ItemStack(slot);
                ItemMeta slotMeta = slotCopy.getItemMeta();
                if (slotMeta != null) {
                    slotMeta.displayName(Component.text(""));
                    if (!carbonCopy) {
                        slotMeta.lore(null);
                    }
                    slotCopy.setItemMeta(slotMeta);
                }
                if (slotCopy.isSimilar(itemCopy)) {
                    mappedItems.put(i, slot);
                }
            }
        }
        int totalIncome = 0;
        for (Map.Entry<Integer, ItemStack> entrySlots : mappedItems.entrySet()) {
            if (entrySlots.getValue().getAmount() <= amount) {
                inv.setItem(entrySlots.getKey().intValue(), new ItemStack(Material.AIR));
                amount -= entrySlots.getValue().getAmount();
                totalIncome += price*entrySlots.getValue().getAmount();
            } else {
                ItemStack invItem = inv.getItem(entrySlots.getKey());
                invItem.setAmount(invItem.getAmount() - amount);
                totalIncome += price*amount;
                break;
            }
        }
        if (totalIncome > 0) {
            Economy.addMoney(player, totalIncome);
        }
        return true;
    }

    private static SellData getSellData(HumanEntity player, ItemStack item) {
        List<Component> lore = item.getItemMeta().lore();
        if (lore == null) {
            return null;
        }

        String buyOrSell = null;
        String costLine = null;
        boolean carbonCopy = false;
        Component carbonCopyLine = null;
        Component buySellLine = null;

        for (Component line : lore) {
            String lineText = PlainTextComponentSerializer.plainText().serialize(line);
            if (lineText.toUpperCase().contains("BUY:")) {
                buyOrSell = "Buy";
                costLine = lineText.toUpperCase();
                buySellLine = line;
            } else if (lineText.toUpperCase().contains("SELL:")) {
                buyOrSell = "Sell";
                costLine = lineText.toUpperCase();
                buySellLine = line;
            } else if (lineText.toUpperCase().contains("CARBON COPY")) {
                carbonCopy = true;
                carbonCopyLine = line;
            }
        }
        if (costLine == null) {
            return null;
        }
        costLine = costLine.replaceAll("(BUY: )|(SELL: )", "");
        int buySellPrice;
        try {
            buySellPrice = Integer.parseInt(costLine.substring(0, costLine.indexOf(" ")))/item.getAmount();
        } catch (NumberFormatException ex) {
            player.sendMessage(MessageTools.parseFromPath(config, "Trade Error"));
            ex.printStackTrace();
            return null;
        }

        return new SellData(item, buyOrSell, buySellPrice, buySellLine, carbonCopy, carbonCopyLine);
    }

    private static int getNumberOfItems(Inventory inventory, ItemStack item, boolean carbonCopy) {
        int totalItems = 0;
        ItemStack itemCopy = item.clone();
        ItemMeta cloneMeta = itemCopy.getItemMeta();
        if (cloneMeta != null) {
            cloneMeta.displayName(Component.text(""));
            if (!carbonCopy) {
                cloneMeta.lore(null);
            }
            itemCopy.setItemMeta(cloneMeta);
        }


        ItemStack[] slots = inventory.getContents();
        //total all matching items
        for (ItemStack slot : slots) {
            if (slot == null || !slot.getType().equals(item.getType())) {
                continue;
            }
            ItemStack slotCopy = new ItemStack(slot);
            ItemMeta slotMeta = slotCopy.getItemMeta();
            if (slotMeta != null) {
                slotMeta.displayName(Component.text(""));
                if (!carbonCopy) {
                    slotMeta.lore(null);
                }
                slotCopy.setItemMeta(slotMeta);
            }

            if (slotCopy.isSimilar(itemCopy)) {
                totalItems += slot.getAmount();
            }
        }

        return totalItems;
    }

    public static boolean canFitItem(Inventory inv, ItemStack item, int quanity) {
        List<ItemStack> slots = Arrays.stream(Arrays.copyOfRange(inv.getContents(), 0, 36)).filter(a -> a == null || a.getAmount() != a.getMaxStackSize()).collect(Collectors.toList());
        ItemMeta itemMeta = item.getItemMeta();
        int totalRoom = 0;

        for (ItemStack slot : slots) {
            if (slot == null) {
                totalRoom += 64;
                continue;
            }
            if (slot.getItemMeta().equals(itemMeta)) {
                totalRoom += slot.getMaxStackSize() - slot.getAmount();
            }
        }
        return totalRoom >= quanity;
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

class SellData {
    private ItemStack item;
    private String buyOrSell;
    private int buySellPrice;
    private boolean carbonCopy;
    private Component buySellLine;
    private Component carbonCopyLine;

    public SellData(ItemStack item, String buyOrSell, int buySellPrice, Component buySellLine, boolean carbonCopy, Component carbonCopyLine) {
        this.item = item;
        this.buyOrSell = buyOrSell;
        this.buySellPrice = buySellPrice;
        this.buySellLine = buySellLine;
        this.carbonCopy = carbonCopy;
        this.carbonCopyLine = carbonCopyLine;
    }

    public String getBuyOrSell() {
        return buyOrSell;
    }

    public int getPrice() {
        return buySellPrice;
    }

    public boolean isCarbonCopy() {
        return carbonCopy;
    }

    public Component getCarbonCopyLine() {
        return carbonCopyLine;
    }

    public Component getBuySellLine() {
        return buySellLine;
    }

    public ItemStack getItem() {
        return item;
    }
}
