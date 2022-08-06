package net.dirtlands.listeners.shopkeepers.custom.shops;

import jeeper.utils.MessageTools;
import net.dirtlands.Main;
import net.dirtlands.economy.Currency;
import net.dirtlands.economy.Economy;
import net.dirtlands.listeners.shopkeepers.Editor;
import net.dirtlands.listeners.shopkeepers.Shopkeeper;
import net.dirtlands.tools.ItemTools;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class HorseShop implements Listener {

    private static final Map<UUID, Inventory> openShopMenus = Shopkeeper.openShopMenus;
    private static final Map<UUID, String> colorChosen = new HashMap<>();
    private static final Map<UUID, Integer> horseCost = new HashMap<>();
    private static final int JUMP_UPGRADE_COST = 1;
    private static final int SPEED_UPGRADE_COST = 1;
    static ItemStack BOOTS_ITEM;
    static {
        BOOTS_ITEM = ItemTools.createGuiItem(Material.GOLDEN_BOOTS,
                MessageTools.parseText("<green>Upgrade Jump Height"), 1,
                MessageTools.parseText("<#2BD5D5>Cost: " + JUMP_UPGRADE_COST + " <dark_aqua><bold>❖"));
        ItemMeta meta = BOOTS_ITEM.getItemMeta() == null ? Bukkit.getItemFactory().getItemMeta(BOOTS_ITEM.getType()) : BOOTS_ITEM.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        BOOTS_ITEM.setItemMeta(meta);
        BOOTS_ITEM = makeHorseShopItem(BOOTS_ITEM, "JumpUpgrade");
    }
    static ItemStack FEATHER_ITEM = makeHorseShopItem(ItemTools.createGuiItem(Material.FEATHER,
            MessageTools.parseText("<aqua>Upgrade Movement Speed"),1,
            MessageTools.parseText("<#2BD5D5>Cost: " + SPEED_UPGRADE_COST + " <dark_aqua><bold>❖")), "SpeedUpgrade");

    @EventHandler
    public void onHorseShopItemClick(InventoryClickEvent e) {
        if (!openShopMenus.containsKey(e.getWhoClicked().getUniqueId())) {
            return;
        }
        ItemStack item = e.getCurrentItem();
        if (item == null) {
            return;
        }
        ItemMeta meta = item.getItemMeta();

        NamespacedKey horseShopKey = new NamespacedKey(Main.getPlugin(), "HorseShop");
        String horseData = meta.getPersistentDataContainer().get(horseShopKey, PersistentDataType.STRING);


        if (horseData == null || horseData.equals("")) {
            if (item.getType().equals(Material.SADDLE)) {
                List<Component> lore = meta.lore();
                if (lore == null) {
                    return;
                }
                if (lore.size() == 4 &&
                        (e.getView().title().equals(MessageTools.parseText("<#856f2d>Place your mount in the slot"))) ||
                         e.getView().title().equals(MessageTools.parseText("<#856f2d>Upgrade Your Horse"))) {
                    horseData = "SaddleItem";
                } else {
                    return;
                }
            } else {
                return;
            }
        }

        switch (horseData) {
            case "Create" -> {
                List<Component> lore = e.getCurrentItem().lore();

                if (lore == null) {
                    return;
                }

                for (Component line : lore) {
                    String plain = PlainTextComponentSerializer.plainText().serialize(line);

                    if (!plain.toUpperCase().contains("BUY: ")) {
                        continue;
                    }

                    plain = plain.toUpperCase().replace("BUY: ", "").replace(" ❖", "");

                    int cost = Integer.parseInt(plain);
                    horseCost.put(e.getWhoClicked().getUniqueId(), cost);
                    break;
                }

                openNewHorseMenu(e);
            }
            case "Upgrade" -> openUpgradeHorseMenu(e);
            case "Color" -> {
                Component name = meta.displayName();
                if (name == null) {
                    return;
                }
                String color = PlainTextComponentSerializer.plainText().serialize(name);

                colorChosen.put(e.getWhoClicked().getUniqueId(), color);

                openHorseStyleMenu(e);
            }
            case "Style" -> confirmPurchaseMenu(e);
            case "SaddleItem" -> updateUpgradeMenu(e);
            case "SpeedUpgrade" -> buyUpgrades(e, "SpeedUpgrade");
            case "JumpUpgrade" -> buyUpgrades(e, "JumpUpgrade");
        }
    }

    @EventHandler
    public void onHorseInventoryClose(InventoryCloseEvent e) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getPlugin(),
                () -> {
                    if (!openShopMenus.containsKey(e.getPlayer().getUniqueId())) {
                        colorChosen.remove(e.getPlayer().getUniqueId());
                        horseCost.remove(e.getPlayer().getUniqueId());
                    }
                }, 20L);
    }

    private static ItemStack makeHorseShopItem(@NotNull ItemStack item, String tag) {
        NamespacedKey horseShopKey = new NamespacedKey(Main.getPlugin(), "HorseShop");
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(horseShopKey, PersistentDataType.STRING, tag);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isHorseShopItem(@NotNull ItemStack item) {
        NamespacedKey horseShopKey = new NamespacedKey(Main.getPlugin(), "HorseShop");
        ItemMeta meta = item.getItemMeta();
        String value = meta.getPersistentDataContainer().get(horseShopKey, PersistentDataType.STRING);
        return value != null;
    }

    private String getName(ItemStack item) {
        if (item == null) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        Component name = meta.displayName();
        if (name == null) {
            return null;
        }
        return PlainTextComponentSerializer.plainText().serialize(name);
    }

    /**
     * Not an event!
     */
    private void confirmPurchaseMenu(InventoryClickEvent e) {
        String color = colorChosen.get(e.getWhoClicked().getUniqueId());


        String style = getName(e.getCurrentItem());


        ItemStack saddle = new ItemStack(Material.SADDLE, 1);

        List<Component> lore = new ArrayList<>();

        lore.add(MessageTools.parseText("<!italic><#856f2d>Speed: <#b8a567>1"));
        lore.add(MessageTools.parseText("<!italic><#856f2d>Jump: <#b8a567>1"));
        lore.add(MessageTools.parseText("<!italic><#856f2d>Color: <#b8a567>" + color));
        lore.add(MessageTools.parseText("<!italic><#856f2d>Style: <#b8a567>" + style));
        Integer cost = horseCost.get(e.getWhoClicked().getUniqueId());
        if (cost == null) {
            return;
        }
        lore.add(MessageTools.parseText("<!italic><#2BD5D5>Buy: <aqua>" + cost + " <dark_aqua><bold>❖"));
        lore.add(MessageTools.parseText("<italic><#2BD5D5>Carbon Copy"));

        ItemMeta meta = saddle.getItemMeta();
        meta.displayName(MessageTools.parseText("<!italic><#7c3e12><bold>Horse Mount"));
        meta.lore(lore);

        saddle.setItemMeta(meta);

        e.setCurrentItem(saddle); // Open the buy menu for the saddle
        Shopkeeper.openBuySellMenu(e);
    }


    /**
     * Not an event!
     */
    private void openHorseStyleMenu(InventoryClickEvent e) {
        Inventory inv = Bukkit.createInventory(e.getWhoClicked(), 27,
                MessageTools.parseText("<#856f2d>Choose A Horse Style"));

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, Editor.makeHotbarItem(Editor.generateGuiBackground()));
        }



        inv.setItem(11, makeHorseShopItem(ItemTools.createGuiItem(Material.BARRIER,
                MessageTools.parseText("<dark_red>None"),1,
                MessageTools.parseText("<red>No Markings")), "Style"));

        ItemStack boots = makeHorseShopItem(ItemTools.createGuiItem(Material.LEATHER_BOOTS,
                MessageTools.parseText("<#F8F0E3>White"),1,
                MessageTools.parseText("<white>White socks or stripes")), "Style");

        LeatherArmorMeta meta = boots.hasItemMeta() ? (LeatherArmorMeta) boots.getItemMeta() :
                (LeatherArmorMeta) Bukkit.getItemFactory().getItemMeta(boots.getType());

        meta.setColor(Color.WHITE);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        boots.setItemMeta(meta);

        inv.setItem(12, boots);


        inv.setItem(13, makeHorseShopItem(ItemTools.createGuiItem(Material.WHITE_DYE,
                MessageTools.parseText("<#F8F0E3>Whitefield"),1,
                MessageTools.parseText("<white>Milky splotches")), "Style"));
        inv.setItem(14, makeHorseShopItem(ItemTools.createGuiItem(Material.OXEYE_DAISY,
                MessageTools.parseText("<#F8F0E3>White Dots"),1,
                MessageTools.parseText("<white>Round white dots")), "Style"));
        inv.setItem(15, makeHorseShopItem(ItemTools.createGuiItem(Material.WITHER_ROSE,
                MessageTools.parseText("<#22262a>Black Dots"),1,
                MessageTools.parseText("<#2E3238>Small black dots")), "Style"));

        e.getWhoClicked().openInventory(inv);
        openShopMenus.put(e.getWhoClicked().getUniqueId(), inv);
    }

    /**
     * Not an event!
     */
    private void openNewHorseMenu(InventoryClickEvent e) {
        Inventory inv = Bukkit.createInventory(e.getWhoClicked(), 27,
                MessageTools.parseText("<#856f2d>Choose A Horse Color"));

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, Editor.makeHotbarItem(Editor.generateGuiBackground()));
        }

        inv.setItem(10, makeHorseShopItem(ItemTools.createGuiItem(Material.WHITE_CONCRETE,
                MessageTools.parseText("<white>White"),1), "Color"));
        inv.setItem(11, makeHorseShopItem(ItemTools.createGuiItem(Material.TERRACOTTA,
                MessageTools.parseText("<#9d5021>Creamy"),1), "Color"));
        inv.setItem(12, makeHorseShopItem(ItemTools.createGuiItem(Material.BROWN_WOOL,
                MessageTools.parseText("<#613c1f>Chestnut"),1), "Color"));
        inv.setItem(13, makeHorseShopItem(ItemTools.createGuiItem(Material.BROWN_CONCRETE,
                MessageTools.parseText("<#4d3224>Brown"),1), "Color"));
        inv.setItem(14, makeHorseShopItem(ItemTools.createGuiItem(Material.BROWN_TERRACOTTA,
                MessageTools.parseText("<#3a2413>Dark Brown"),1), "Color"));
        inv.setItem(15, makeHorseShopItem(ItemTools.createGuiItem(Material.GRAY_CONCRETE,
                MessageTools.parseText("<#35393c>Gray"),1), "Color"));
        inv.setItem(16, makeHorseShopItem(ItemTools.createGuiItem(Material.BLACK_CONCRETE,
                MessageTools.parseText("<#1F2226>Black"),1), "Color"));

        e.getWhoClicked().openInventory(inv);

        openShopMenus.put(e.getWhoClicked().getUniqueId(), inv);
    }

    /**
     * Not an event!
     */
    private void openUpgradeHorseMenu(InventoryClickEvent e) {
        Inventory inv = Bukkit.createInventory(e.getWhoClicked(), 27,
                MessageTools.parseText("<#856f2d>Place your mount in the slot"));

        for (int i = 0; i < inv.getSize(); i++) {
            if (i == 13) {
                continue;
            }
            inv.setItem(i, Editor.makeHotbarItem(Editor.generateGuiBackground()));
        }

        e.getWhoClicked().openInventory(inv);

        openShopMenus.put(e.getWhoClicked().getUniqueId(), inv);
    }

    /**
     * Not an event!
     */
    private void updateUpgradeMenu(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        if (item == null) {
            return;
        }

        Inventory inv;

        if (e.getClickedInventory() instanceof PlayerInventory) {
            inv = Bukkit.createInventory(e.getWhoClicked(), e.getInventory().getType(),
                    MessageTools.parseText("<#856f2d>Upgrade Your Horse"));
            inv.setContents(e.getInventory().getContents());

            ItemStack saddle = inv.getItem(13);
            if (saddle != null) {
                if (saddle.getType().equals(Material.SADDLE)) {
                    e.getWhoClicked().getInventory().setItem(e.getSlot(), saddle);
                }
            }

            inv.setItem(11, FEATHER_ITEM);

            inv.setItem(13, item);

            inv.setItem(15, BOOTS_ITEM);

            if (saddle == null) {
                e.getWhoClicked().getInventory().setItem(e.getSlot(), new ItemStack(Material.AIR));
            }
        } else {

            inv = Bukkit.createInventory(e.getWhoClicked(), e.getInventory().getType(),
                    MessageTools.parseText("<#856f2d>Place your mount in the slot"));
            inv.setContents(e.getInventory().getContents());

            inv.setItem(11, Editor.generateGuiBackground());
            inv.setItem(13, null);
            inv.setItem(15, Editor.generateGuiBackground());
            returnSaddle(e.getWhoClicked(), item);
        }

        e.getWhoClicked().openInventory(inv);
        openShopMenus.put(e.getWhoClicked().getUniqueId(), inv);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (e.getReason().equals(InventoryCloseEvent.Reason.OPEN_NEW)) {
            return;
        }
        if (!openShopMenus.containsKey(e.getPlayer().getUniqueId()) &&
                !e.getView().title().equals(MessageTools.parseText("<#856f2d>Upgrade Your Horse"))) {
            return;
        }
        Inventory inv = e.getInventory();
        ItemStack saddle;
        try {
            saddle = inv.getItem(13);
        } catch (IndexOutOfBoundsException exception) {
            return;
        }
        if (saddle == null) {
            return;
        }
        if (inv.getSize() == 27 && saddle.getType().equals(Material.SADDLE)) {
            returnSaddle(e.getPlayer(), saddle);
        }
    }
    
    private void returnSaddle(HumanEntity player, ItemStack item) {
        if (ItemTools.canFitItem(player.getInventory(), item, item.getAmount())) {
            player.getInventory().addItem(item);
        } else {
            ItemTools.dropItemForOnlyPlayer(player, item);
        }
    }

    /**
     * Not an event!
     */
    private void buyUpgrades(InventoryClickEvent e, String upgradeName) {
        boolean speedUpgrade = upgradeName.equals("SpeedUpgrade");

        int cost = speedUpgrade ? SPEED_UPGRADE_COST : JUMP_UPGRADE_COST;

        ItemStack saddle = e.getInventory().getItem(13);

        if (saddle == null) {
            return;
        }

        List<Component> lore = saddle.lore();

        if (lore == null) {
            return;
        }
        if (lore.size() != 4) {
            return;
        }

        // speed 0, jump 1
        Component levelText = speedUpgrade ? lore.get(0) : lore.get(1);

        int level = Integer.parseInt(PlainTextComponentSerializer.plainText().serialize(levelText)
                .replaceAll("(Speed:\\s)|(Jump:\\s)", ""));

        if (level >= 3) {
            e.getWhoClicked().sendMessage(MessageTools.parseFromPath(Main.getPlugin().config(), "Horse Max Upgrade"));
            return;
        }

        // Runs if player can't afford
        if (!Economy.take(e.getWhoClicked(), new Currency(cost, 0))) {
            e.getWhoClicked().sendMessage(MessageTools.parseFromPath(Main.getPlugin().config(), "Cant Afford Message"));
            return;
        }

        level++;

        if (speedUpgrade) {
            lore.set(0, MessageTools.parseText("<!italic><#856f2d>Speed: <#b8a567>" + level));
        } else {
            lore.set(1, MessageTools.parseText("<!italic><#856f2d>Jump: <#b8a567>" + level));
        }
        e.getInventory().setItem(speedUpgrade ? 11 : 15, ItemTools.createGuiItem(Material.LIME_STAINED_GLASS_PANE,
                MessageTools.parseText("<green>Purchase Successful!"),1));

        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getPlugin(), () -> {
            if (speedUpgrade) {
                e.getInventory().setItem(11, FEATHER_ITEM);
            } else {
                e.getInventory().setItem(15, BOOTS_ITEM);
            }
        }, 5L);

        saddle.lore(lore);

        e.getInventory().setItem(13, saddle);
    }
}
