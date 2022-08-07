package net.dirtlands.listeners.shopkeepers.custom.shops;

import dirtlands.db.Tables;
import jeeper.utils.MessageTools;
import net.dirtlands.Main;
import net.dirtlands.database.DatabaseTools;
import net.dirtlands.database.ItemSerialization;
import net.dirtlands.economy.Currency;
import net.dirtlands.listeners.shopkeepers.Shopkeeper;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jooq.DSLContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Banker implements Listener {

    private static final Map<UUID, Inventory> openShopMenus = Shopkeeper.openShopMenus;
    private static final Map<UUID, Inventory> openStorageMenu = new HashMap<>();
    private static final DSLContext dslContext = Main.getPlugin().getDslContext();

    @EventHandler
    public void onBankerItemClick(InventoryClickEvent e) {
        if (!openShopMenus.containsKey(e.getWhoClicked().getUniqueId()) &&
                !openStorageMenu.containsKey(e.getWhoClicked().getUniqueId())) {
            return;
        }
        ItemStack item = e.getCurrentItem();
        if (item == null) {
            return;
        }
        ItemMeta meta = item.getItemMeta();

        NamespacedKey bankerItemKey = new NamespacedKey(Main.getPlugin(), "BankerItem");
        String bankerData = meta.getPersistentDataContainer().get(bankerItemKey, PersistentDataType.STRING);


        if (bankerData == null || bankerData.equals("")) {
            if (!item.asOne().equals(Currency.TOKEN_ITEM) && !item.asOne().equals(Currency.EXPENSIVE_TOKEN_ITEM)) {
                if (openStorageMenu.containsKey(e.getWhoClicked().getUniqueId())) {
                    e.setCancelled(true);
                }
            }
            return;
        }

        switch (bankerData) {
            case "Storage" -> openStorageMenu(e);
        }
    }

    private void openStorageMenu(InventoryClickEvent e) {
        Inventory inv = Bukkit.createInventory(e.getWhoClicked(), 27,
                MessageTools.parseText("<#856f2d>Deposit/Withdrawal Money"));

        var bankInventory = dslContext.select(Tables.BANK.INVENTORYBASE64).from(Tables.BANK)
                .where(Tables.BANK.USERID.eq(DatabaseTools.getUserID(e.getWhoClicked().getUniqueId()))).fetchAny();

        if (bankInventory == null) {
            e.getWhoClicked().openInventory(inv);
            openStorageMenu.put(e.getWhoClicked().getUniqueId(), inv);
            return;
        }
        String bankBase64 = bankInventory.get(Tables.BANK.INVENTORYBASE64);
        if (bankBase64 == null) {
            return;
        }

        try {
            inv.setContents(ItemSerialization.fromBase64(
                    bankInventory.get(Tables.BANK.INVENTORYBASE64)).getInventory().getContents()
            );
        } catch (IOException exception) {
            return;
        }

        e.getWhoClicked().openInventory(inv);
        openStorageMenu.put(e.getWhoClicked().getUniqueId(), inv);
    }

    @EventHandler
    public void playerCloseInventory(InventoryCloseEvent e) {
        if (openStorageMenu.containsKey(e.getPlayer().getUniqueId())) {
            openStorageMenu.remove(e.getPlayer().getUniqueId());

            int userID = DatabaseTools.getUserID(e.getPlayer().getUniqueId());

            var bankRecord = dslContext.select().from(Tables.BANK).where(
                    Tables.BANK.USERID.eq(userID)
            ).fetchAny();

            if (bankRecord == null) {
                dslContext.insertInto(Tables.BANK).columns(Tables.BANK.USERID, Tables.BANK.INVENTORYBASE64)
                        .values(
                                userID,
                                ItemSerialization.toBase64(new ItemSerialization(e.getInventory(), e.getView().title()))
                        ).execute();
                return;
            }

            dslContext.update(Tables.BANK)
                    .set(Tables.BANK.INVENTORYBASE64,
                            ItemSerialization.toBase64(new ItemSerialization(e.getInventory(), e.getView().title()))
                    ).where(Tables.BANK.USERID.eq(userID)).execute();
        }
    }



}
