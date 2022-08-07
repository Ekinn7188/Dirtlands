package net.dirtlands.economy;

import jeeper.utils.config.Config;
import net.dirtlands.Main;
import net.dirtlands.tools.ItemTools;
import org.bukkit.entity.HumanEntity;

public class Economy {
    private static final Config config = Main.getPlugin().config();

    /**
     * Give money to a player
     * @param player player to pay
     * @param amount the amount of tokens and expensive tokens given
     */
    public static void give(HumanEntity player, Currency amount) {
        if (!ItemTools.canFitItem(player.getInventory(), amount.getExpensiveTokenItem())) {
            if (!ItemTools.canFitItem(player.getInventory(), amount.getTokenItem())) {
                ItemTools.dropItemForOnlyPlayer(player, amount.itemsAsArray());
                return;
            }
            ItemTools.dropItemForOnlyPlayer(player, amount.getExpensiveTokenItem());
            player.getInventory().addItem(amount.getTokenItem());
            return;

        }
        player.getInventory().addItem(Currency.EXPENSIVE_TOKEN_ITEM.asQuantity(amount.getExpensiveTokens()));
        player.getInventory().addItem(Currency.TOKEN_ITEM.asQuantity(amount.getTokens()));
    }

    /**
     * Take money from a player
     * @param player player to take money from
     * @param amount the amount of tokens and expensive tokens taken
     * @return whether the transaction was successful or not
     */
    public static boolean take(HumanEntity player, Currency amount) {
        Currency copy = amount.copy();
        copy.convertExpensiveTokensToTokens();

        Currency balance = new Currency(player.getInventory());

        int tokens = ItemTools.countItems(player.getInventory(), Currency.TOKEN_ITEM);
        int expensiveTokens = ItemTools.countItems(player.getInventory(), Currency.EXPENSIVE_TOKEN_ITEM);

        if (ItemTools.takeItems(player.getInventory(), Currency.TOKEN_ITEM, copy.getTokens())) {
            return true;
        }

        if (ItemTools.countItems(player.getInventory(), Currency.EXPENSIVE_TOKEN_ITEM) >= amount.getExpensiveTokens()
                && ItemTools.countItems(player.getInventory(), Currency.TOKEN_ITEM) >= amount.getTokens()) {
            //Check if both items can be taken before they are actually taken
            ItemTools.takeItems(player.getInventory(), Currency.EXPENSIVE_TOKEN_ITEM, amount.getExpensiveTokens());
            ItemTools.takeItems(player.getInventory(), Currency.TOKEN_ITEM, amount.getTokens());
            return true;
        }

        // Make change for a purchase
        if (ItemTools.canFitItem(player.getInventory(), Currency.TOKEN_ITEM, 64-amount.getTokens())) {
            if (ItemTools.takeItems(player.getInventory(), Currency.EXPENSIVE_TOKEN_ITEM, amount.getExpensiveTokens()+1)) {
                player.getInventory().addItem(Currency.TOKEN_ITEM.asQuantity(64 - amount.getTokens()));
                return true;
            }
        }

        return false;
    }

}
