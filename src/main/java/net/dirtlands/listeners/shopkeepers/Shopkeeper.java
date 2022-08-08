package net.dirtlands.listeners.shopkeepers;

import dirtlands.db.Tables;
import jeeper.utils.MessageTools;
import jeeper.utils.config.Config;
import net.citizensnpcs.api.CitizensAPI;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.database.ItemSerialization;
import net.dirtlands.economy.Currency;
import net.dirtlands.economy.Economy;
import net.dirtlands.listeners.shopkeepers.custom.shops.HorseShop;
import net.dirtlands.tools.Durability;
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
                if (item.getAmount() == 1) {
                    shopMenu.setItem(29, newPriceBuyItem(data.getBuyOrSell(), item, data.getPrice(), generalIndex, 1, data.getCarbonCopyLine()));
                    shopMenu.setItem(30, newPriceBuyItem(data.getBuyOrSell(), item, data.getPrice(), generalIndex, item.getMaxStackSize() / (item.getMaxStackSize() == 64 ? 8 : 4), data.getCarbonCopyLine()));
                    shopMenu.setItem(31, newPriceBuyItem(data.getBuyOrSell(), item, data.getPrice(), generalIndex,item.getMaxStackSize()/2, data.getCarbonCopyLine()));
                    shopMenu.setItem(32, newPriceBuyItem(data.getBuyOrSell(), item, data.getPrice(), generalIndex,item.getMaxStackSize(), data.getCarbonCopyLine()));
                }
                else {
                    if (item.getAmount() * 4 <= item.getMaxStackSize()) {
                        shopMenu.setItem(29, newPriceBuyItem(data.getBuyOrSell(), item, data.getPrice(), generalIndex, item.getAmount(), data.getCarbonCopyLine()));
                        shopMenu.setItem(30, newPriceBuyItem(data.getBuyOrSell(), item, data.getPrice(), generalIndex, item.getAmount()*2, data.getCarbonCopyLine()));
                        shopMenu.setItem(31, newPriceBuyItem(data.getBuyOrSell(), item, data.getPrice(), generalIndex,item.getAmount()*3, data.getCarbonCopyLine()));
                        shopMenu.setItem(32, newPriceBuyItem(data.getBuyOrSell(), item, data.getPrice(), generalIndex,item.getAmount()*4, data.getCarbonCopyLine()));
                    }
                    else if (item.getAmount() * 3 <= item.getMaxStackSize()) {
                        shopMenu.setItem(29, newPriceBuyItem(data.getBuyOrSell(), item, data.getPrice(), generalIndex, item.getAmount(), data.getCarbonCopyLine()));
                        shopMenu.setItem(30, newPriceBuyItem(data.getBuyOrSell(), item, data.getPrice(), generalIndex, item.getAmount()*2, data.getCarbonCopyLine()));
                        shopMenu.setItem(31, newPriceBuyItem(data.getBuyOrSell(), item, data.getPrice(), generalIndex,item.getAmount()*3, data.getCarbonCopyLine()));
                    }
                    else if (item.getAmount() * 2 <= item.getMaxStackSize()) {
                        shopMenu.setItem(29, newPriceBuyItem(data.getBuyOrSell(), item, data.getPrice(), generalIndex, item.getAmount(), data.getCarbonCopyLine()));
                        shopMenu.setItem(31, newPriceBuyItem(data.getBuyOrSell(), item, data.getPrice(), generalIndex, item.getAmount()*2, data.getCarbonCopyLine()));
                    }
                    else  {
                        shopMenu.setItem(29, newPriceBuyItem(data.getBuyOrSell(), item, data.getPrice(), generalIndex, 1, data.getCarbonCopyLine()));
                    }
                }
            } else {
                shopMenu.setItem(29, newPriceBuyItem(data.getBuyOrSell(), item, data.getPrice(), generalIndex, 1, data.getCarbonCopyLine()));
            }
            if (data.getBuyOrSell().equalsIgnoreCase("Buy")) {
                shopMenu.setItem(33, NAME_TAG_ITEM);
            } else {
                //set lore for item
                shopMenu.setItem(33, newPriceBuyItem(data.getBuyOrSell(), EMERALD_BLOCK_ITEM, data.getPrice(), generalIndex, getNumberOfItems(e.getWhoClicked().getOpenInventory().getBottomInventory(), noCarbonCopyLore(itemDisplay, data), data.isCarbonCopy())/item.getAmount(), data.getCarbonCopyLine()).asQuantity(1));
            }

            e.getWhoClicked().openInventory(shopMenu);
            openShopMenus.put(e.getWhoClicked().getUniqueId(), shopMenu);

        }
    }


    private static ItemStack newPriceBuyItem(String buyOrSell, ItemStack item, Currency currentPrice, int loreIndex, int quantity, Component carbonCopyLine) {
        Currency newPrice = currentPrice.multiply(quantity/item.getAmount());
        try {
            item = item.asQuantity(quantity);
        } catch (Exception ex) {
            item = item.asOne();
        }

        List<Component> lore = item.lore();
        if (lore == null) {
            lore = new ArrayList<>();
            lore.add(Component.empty());
            if (newPrice.getTokens() <= 0) {
                lore.add(ItemTools.enableItalicUsage(MessageTools.parseText("<#2BD5D5>" + buyOrSell + ": <dark_aqua><white>"
                        + Currency.EXPENSIVE_TOKEN_CHARACTER + "</white> x "+ newPrice.getExpensiveTokens())));
            }
            else if (newPrice.getExpensiveTokens() <= 0) {
                lore.add(ItemTools.enableItalicUsage(MessageTools.parseText("<#2BD5D5>" + buyOrSell + ": <gold><white>"
                        + Currency.TOKEN_CHARACTER + "</white> x " + newPrice.getTokens())));
            }
            else {
                lore.add(ItemTools.enableItalicUsage(MessageTools.parseText("<#2BD5D5>" + buyOrSell + ": <dark_aqua><white>"
                        + Currency.EXPENSIVE_TOKEN_CHARACTER + "</white> x "+ newPrice.getExpensiveTokens()
                        + " <gold><white>" + Currency.TOKEN_CHARACTER + "</white> x " + newPrice.getTokens() )));
            }
            item.lore(lore);
            return item;
        }

        if (newPrice.getTokens() <= 0) {
            lore.set(loreIndex, ItemTools.enableItalicUsage(MessageTools.parseText("<#2BD5D5>" + buyOrSell + ": <dark_aqua><white>"
                    + Currency.EXPENSIVE_TOKEN_CHARACTER + "</white> x "+ newPrice.getExpensiveTokens())));
        }
        else if (newPrice.getExpensiveTokens() <= 0) {
            lore.set(loreIndex, ItemTools.enableItalicUsage(MessageTools.parseText("<#2BD5D5>" + buyOrSell + ": <gold><white>"
                    + Currency.TOKEN_CHARACTER + "</white> x " + newPrice.getTokens())));
        }
        else {
            lore.set(loreIndex, ItemTools.enableItalicUsage(MessageTools.parseText("<#2BD5D5>" + buyOrSell + ": <dark_aqua><white>"
                    + Currency.EXPENSIVE_TOKEN_CHARACTER + "</white> x "+ newPrice.getExpensiveTokens()
                    + " <gold><white>" + Currency.TOKEN_CHARACTER + "</white> x " + newPrice.getTokens() )));
        }

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
                    if (e.getSlot() > 3) {
                        ItemStack itemBefore = null;
                        for (int i = 4; i >= 0; i--) {
                            itemBefore = e.getInventory().getItem(e.getSlot() - i);
                            if (!itemBefore.getType().equals(Material.BLACK_STAINED_GLASS_PANE)) {
                                break;
                            }
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

        ItemStack firstBuyOption = e.getInventory().getItem(29);
        if (itemToParse.equals(new ItemStack(item))) {
            if (firstBuyOption == null || firstBuyOption.equals(Editor.generateGuiBackground())) {
                itemToParse = noCarbonCopyLore(e.getInventory().getItem(13), data);
            } else {
                itemToParse = noCarbonCopyLore(e.getInventory().getItem(13).asQuantity(item.getAmount()), data);
            }
        }

        //normal trade
        if (data.getBuyOrSell().equalsIgnoreCase("Buy")) {
            Material material = itemToParse.getType();
            //Removes most metadata in item if not a copy
            ItemStack itemToAdd = data.isCarbonCopy() ? itemToParse : new ItemStack(material, itemToParse.getAmount());

            if (!data.isCarbonCopy()) {
                int durability, maxDurability;

                durability = Durability.getDurability(itemToParse);
                maxDurability = Durability.getMaxDurability(itemToParse);

                if (durability != -1 && maxDurability != -1){
                    Durability.setCustomDurability(itemToAdd, durability, maxDurability);
                }
            }


            if (customAmount) {
                //buy custom amount
                customBuy(e.getClickedInventory(), e.getWhoClicked(), itemToAdd, data.getPrice(), data.isCarbonCopy());
                return;
            }
            buyItems(e.getWhoClicked(), itemToAdd, data.getPrice(), data.getItem().getAmount(), data.isCarbonCopy());
            return;
        }


        Inventory inv = e.getWhoClicked().getInventory();

        int numberOfItems = customAmount ? Integer.MAX_VALUE : item.getAmount();
        sellItems(e.getWhoClicked(), inv, itemToParse, numberOfItems, data.getPrice(), data.isCarbonCopy());

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

    private void customBuy(Inventory currentInv, HumanEntity buyer, ItemStack item, Currency price, boolean isCarbonCopy) {
        new AnvilGUI.Builder()
                .onComplete((player, text) -> {
                    openShopMenus.put(buyer.getUniqueId(), currentInv);
                    int quantity = 0;
                    try {
                        quantity = Integer.parseInt(text);
                        if (quantity < 1 || quantity % item.getAmount() != 0) {
                            throw new NumberFormatException();
                        }
                    } catch (NumberFormatException e) {
                        buyer.sendMessage(MessageTools.parseFromPath(config, "Invalid Number"));
                        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> customBuy(currentInv, buyer, item, price, isCarbonCopy), 5L);
                        return AnvilGUI.Response.close();
                    }

                    Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                        buyer.openInventory(currentInv);
                        openShopMenus.put(buyer.getUniqueId(), currentInv);
                    }, 5L);

                    int factor = quantity/item.getAmount();
                    buyItems(buyer, item, price.multiply(factor), quantity, isCarbonCopy);
                    return AnvilGUI.Response.close();
                })
                .title("Buy Custom Amount")
                .plugin(Main.getPlugin())
                .itemLeft(item.asQuantity(1))
                .open((Player) buyer);
        return;
    }

    private void buyItems(HumanEntity buyer, ItemStack item, Currency price, int quantity, boolean isCarbonCopy) {
        if (ItemTools.canFitItem(buyer.getInventory(), item, quantity)) {
            if (Economy.take(buyer, price)) {
                int quantityCopy = quantity;
                for (int i = 0; i < Math.ceil((double) quantityCopy / (double) item.getMaxStackSize()); i++) {
                    if (quantity < 0) {
                        break;
                    }
                    if (quantity > item.getMaxStackSize()) {
                        buyer.getInventory().addItem(item.asQuantity(item.getMaxStackSize()));
                        quantity -= item.getMaxStackSize();
                        continue;
                    }

                    buyer.getInventory().addItem(item.asQuantity(quantity));
                    quantity = 0;
                }
                return;
            }
            buyer.sendMessage(MessageTools.parseFromPath(config, "Cant Afford Message"));
            return;
        }
        buyer.sendMessage(MessageTools.parseFromPath(config, "No Space"));
        return;
    }

    private static boolean sellItems(HumanEntity player, Inventory inv, ItemStack item, int amount, Currency price, boolean carbonCopy) {

        int totalItems = getNumberOfItems(inv, item, carbonCopy);
        if (amount != Integer.MAX_VALUE && totalItems < amount) {
            return false;
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

        int remainder = totalItems % item.getAmount();
        int exchangeAmount = amount == Integer.MAX_VALUE ? totalItems-remainder : amount;
        if (ItemTools.takeItems(player.getInventory(), item, exchangeAmount)) {
            if (price.asTokens() > 0) {
                if (amount == Integer.MAX_VALUE) {
                    Economy.give(player, price.multiply(exchangeAmount/item.getAmount()));
                } else {
                    Economy.give(player, price);
                }
            }
            return true;
        }
        return false;

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
        boolean expensiveTokens = false;
        boolean tokens = false;

        if (costLine.contains(Currency.EXPENSIVE_TOKEN_CHARACTER + "")) {
            expensiveTokens = true;
        }
        if (costLine.contains(Currency.TOKEN_CHARACTER + "")) {
            tokens = true;
        }

        costLine = costLine.replaceAll("(BUY: )|(SELL: )", "").replace(Currency.TOKEN_CHARACTER +  " X ", ".");
        if (costLine.contains(Currency.EXPENSIVE_TOKEN_CHARACTER + "")) {
            if (!tokens) {
                costLine = costLine + ".";
            }
            costLine = costLine.replaceAll("("+Currency.EXPENSIVE_TOKEN_CHARACTER+")|(X)|(\s)", "");
        } else {
            costLine = "0" + costLine.trim();
        }

        Currency buySellPrice;

        // splitCost[0] == Expensive Tokens, splitCost[1] == Tokens
        String[] splitCost = costLine.trim().split("\\.");
        try {
            Double.parseDouble(costLine);
            if (splitCost.length == 1 || splitCost[0].equals("") || splitCost[1].equals("")) {
                if (expensiveTokens) {
                    buySellPrice = new Currency(Integer.parseInt(splitCost[0]), 0);
                }
                else if (tokens) {
                    buySellPrice = new Currency(0, Integer.parseInt(splitCost[0]));
                }
                else {
                    throw new NumberFormatException("Currency isn't tokens nor expensiveTokens");
                }
            } else {
                int expensiveTokenCost = Integer.parseInt(splitCost[0]);
                int tokenCost = Integer.parseInt(splitCost[1]);
                buySellPrice = new Currency(expensiveTokenCost, tokenCost);
            }
        } catch (NumberFormatException ex) {
            player.sendMessage(MessageTools.parseFromPath(config, "Trade Error"));
            ex.printStackTrace();
            return null;
        }

        buySellPrice.convertTokensToExpensiveTokens();
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
    private Currency buySellPrice;
    private boolean carbonCopy;
    private Component buySellLine;
    private Component carbonCopyLine;

    public SellData(ItemStack item, String buyOrSell, Currency buySellPrice, Component buySellLine, boolean carbonCopy, Component carbonCopyLine) {
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

    public Currency getPrice() {
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
