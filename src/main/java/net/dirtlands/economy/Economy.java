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
     * @param amount the amount of tokens and diamonds given
     */
    public static void give(HumanEntity player, Currency amount) {
        if (!ItemTools.canFitItem(player.getInventory(), amount.getDiamondItem())) {
            if (!ItemTools.canFitItem(player.getInventory(), amount.getTokenItem())) {
                ItemTools.dropItemForOnlyPlayer(player, amount.itemsAsArray());
                return;
            }
            ItemTools.dropItemForOnlyPlayer(player, amount.getDiamondItem());
            player.getInventory().addItem(amount.getTokenItem());
            return;

        }
        player.getInventory().addItem(Currency.DIAMOND_ITEM.asQuantity(amount.getDiamonds()));
        player.getInventory().addItem(Currency.TOKEN_ITEM.asQuantity(amount.getTokens()));
    }

    /**
     * Take money from a player
     * @param player player to take money from
     * @param amount the amount of tokens and diamonds taken
     * @return whether the transaction was successful or not
     */
    public static boolean take(HumanEntity player, Currency amount) {
        if (!ItemTools.takeItems(player.getInventory(), Currency.DIAMOND_ITEM, amount.getDiamonds())) {
            return false;
        }
        return ItemTools.takeItems(player.getInventory(), Currency.TOKEN_ITEM, amount.getTokens());
    }

}
