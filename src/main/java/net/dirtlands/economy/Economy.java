package net.dirtlands.economy;

import dirtlands.db.Tables;
import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.database.DatabaseTools;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.entity.Player;
import org.jooq.DSLContext;

public class Economy {
    private static ConfigSetup config = Main.getPlugin().config();
    static DSLContext dslContext = Main.getPlugin().getDslContext();

    /**
     *
     * Give money to a player.
     *
     * @param player player to give money to
     * @param amount how much money to give
     */
    public static void addMoney(Player player, int amount) {
        addPlayerToTableIfDoesntExist(player);

        int balance = dslContext.select(Tables.ECONOMY.BALANCE)
                .from(Tables.ECONOMY).where(Tables.ECONOMY.USERID.eq(DatabaseTools.getUserID(player.getUniqueId()))).fetch().getValue(0, Tables.ECONOMY.BALANCE)
                + amount;

        dslContext.update(Tables.ECONOMY).set(Tables.ECONOMY.BALANCE, balance).execute();

        player.sendActionBar(MessageTools.parseFromPath(config, "Money Gained Actionbar",
                Template.template("money", String.valueOf(amount)), Template.template("balance", String.valueOf(balance))));
    }

    /**
     *
     * If the player's balance goes below 0, money won't be taken.
     * Returns <b>true</b> if successful.
     *
     * @param player player to remove money from
     * @param amount how much money to remove
     */
    public static boolean removeMoney(Player player, int amount) {
        addPlayerToTableIfDoesntExist(player);

        int balance = dslContext.select(Tables.ECONOMY.BALANCE)
                .from(Tables.ECONOMY).where(Tables.ECONOMY.USERID.eq(DatabaseTools.getUserID(player.getUniqueId()))).fetch().getValue(0, Tables.ECONOMY.BALANCE)
                - amount;

        if (balance < 0) {
            return false;
        }

        dslContext.update(Tables.ECONOMY).set(Tables.ECONOMY.BALANCE, balance).execute();
        player.sendActionBar(MessageTools.parseFromPath(config, "Money Lost Actionbar",
                Template.template("money", String.valueOf(amount)), Template.template("balance", String.valueOf(balance))));
        return true;
    }

    /**
     *
     * Remove money from a player, ignoring the 0 limit of <b>removeMoney()</b>.
     *
     * @param player player to remove money from
     * @param amount how much money to remove
     */
    public static void forceRemoveMoney(Player player, int amount) {
        addPlayerToTableIfDoesntExist(player);

        int balance = dslContext.select(Tables.ECONOMY.BALANCE)
                .from(Tables.ECONOMY).where(Tables.ECONOMY.USERID.eq(DatabaseTools.getUserID(player.getUniqueId()))).fetch().getValue(0, Tables.ECONOMY.BALANCE)
                - amount;

        dslContext.update(Tables.ECONOMY).set(Tables.ECONOMY.BALANCE, balance).execute();
        player.sendActionBar(MessageTools.parseFromPath(config, "Money Lost Actionbar",
                Template.template("money", String.valueOf(amount)), Template.template("balance", String.valueOf(balance))));
    }

    /**
     *
     * Sets a player's balance.
     *
     * @param player player to set money for
     * @param amount how much money to set
     */
    public static void setBalance(Player player, int amount) {
        addPlayerToTableIfDoesntExist(player);

        dslContext.update(Tables.ECONOMY).set(Tables.ECONOMY.BALANCE, amount).execute();

        player.sendActionBar(MessageTools.parseFromPath(config,"Money Set Actionbar",
                Template.template("money", String.valueOf(amount))));
    }

    /**
     *
     * Gets a player's balance.
     *
     * @param player player to remove money from
     */
    public static int getBalance(Player player) {
        addPlayerToTableIfDoesntExist(player);

        return dslContext.select(Tables.ECONOMY.BALANCE)
                .from(Tables.ECONOMY).where(Tables.ECONOMY.USERID.eq(DatabaseTools.getUserID(player.getUniqueId()))).fetch().getValue(0, Tables.ECONOMY.BALANCE);
    }

    public static void addPlayerToTable(Player player) {
        dslContext.insertInto(Tables.ECONOMY, Tables.ECONOMY.USERID, Tables.ECONOMY.BALANCE).values(DatabaseTools.getUserID(player.getUniqueId()), 0).execute();
    }

    private static void addPlayerToTableIfDoesntExist(Player player) {
        if(dslContext.select(Tables.ECONOMY.USERID).from(Tables.ECONOMY)
                .where(Tables.ECONOMY.USERID.eq(DatabaseTools.getUserID(player.getUniqueId())))
                .fetch().getValues(Tables.ECONOMY.USERID).size() > 0) {
            return;
        }

        addPlayerToTable(player);
    }


}
