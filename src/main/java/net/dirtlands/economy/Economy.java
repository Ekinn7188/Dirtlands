package net.dirtlands.economy;

import dirtlands.db.Tables;
import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.database.DatabaseTools;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.OfflinePlayer;
import org.jooq.DSLContext;

import java.util.Objects;

public class Economy {
    private static final ConfigSetup config = Main.getPlugin().config();
    static DSLContext dslContext = Main.getPlugin().getDslContext();

    /**
     *
     * Give money to a player.
     *
     * @param player player to give money to
     * @param amount how much money to give
     */
    public static void addMoney(OfflinePlayer player, int amount) {
        addPlayerToTableIfDoesntExist(player);

        int balance = dslContext.select(Tables.ECONOMY.BALANCE)
                .from(Tables.ECONOMY).where(Tables.ECONOMY.USERID.eq(DatabaseTools.getUserID(player.getUniqueId()))).fetch().getValue(0, Tables.ECONOMY.BALANCE)
                + amount;

        updateBalance(player, balance);

        if (player.isOnline()) {
            Objects.requireNonNull(player.getPlayer()).sendActionBar(MessageTools.parseFromPath(config, "Money Gained Actionbar",
                    Template.template("money", String.format("%,d", amount)), Template.template("balance", String.format("%,d", balance))));
        }


    }

    /**
     *
     * If the player's balance goes below 0, money won't be taken.
     *
     * @param player player to remove money from
     * @param amount how much money to remove
     * @return <b>true</b> if successful.
     */
    public static boolean removeMoney(OfflinePlayer player, int amount) {
        addPlayerToTableIfDoesntExist(player);

        int balance = dslContext.select(Tables.ECONOMY.BALANCE)
                .from(Tables.ECONOMY).where(Tables.ECONOMY.USERID.eq(DatabaseTools.getUserID(player.getUniqueId()))).fetch().getValue(0, Tables.ECONOMY.BALANCE)
                - amount;

        if (balance < 0) {
            return false;
        }

        updateBalance(player, balance);

        if (player.isOnline()) {
            Objects.requireNonNull(player.getPlayer()).sendActionBar(MessageTools.parseFromPath(config, "Money Lost Actionbar",
                    Template.template("money", String.format("%,d", amount)), Template.template("balance", String.format("%,d", balance))));
        }

        return true;
    }

    /**
     *
     * Remove money from a player, ignoring the 0 limit of <b>removeMoney()</b>.
     *
     * @param player player to remove money from
     * @param amount how much money to remove
     */
    public static void forceRemoveMoney(OfflinePlayer player, int amount) {
        addPlayerToTableIfDoesntExist(player);

        int balance = dslContext.select(Tables.ECONOMY.BALANCE)
                .from(Tables.ECONOMY).where(Tables.ECONOMY.USERID.eq(DatabaseTools.getUserID(player.getUniqueId()))).fetch().getValue(0, Tables.ECONOMY.BALANCE)
                - amount;

        updateBalance(player, balance);

        if (player.isOnline()) {
            Objects.requireNonNull(player.getPlayer()).sendActionBar(MessageTools.parseFromPath(config, "Money Lost Actionbar",
                Template.template("money", String.format("%,d", amount)), Template.template("balance", String.format("%,d", balance))));
        }
    }

    /**
     *
     * Sets a player's balance.
     *
     * @param player player to set money for
     * @param amount how much money to set
     */
    public static void setBalance(OfflinePlayer player, int amount) {
        addPlayerToTableIfDoesntExist(player);

        updateBalance(player, amount);

        if (player.isOnline()) {
            Objects.requireNonNull(player.getPlayer()).sendActionBar(MessageTools.parseFromPath(config,"Money Set Actionbar",
                    Template.template("money", String.format("%,d", amount))));
        }

    }

    /**
     *
     * Gets a player's balance.
     *
     * @param player the player to get the balance from
     */
    public static int getBalance(OfflinePlayer player) {
        addPlayerToTableIfDoesntExist(player);

        return dslContext.select(Tables.ECONOMY.BALANCE).from(Tables.ECONOMY)
                .where(Tables.ECONOMY.USERID.eq(DatabaseTools.getUserID(player.getUniqueId()))).fetch()
                .getValue(0, Tables.ECONOMY.BALANCE);
    }


    /**
     * Gets a player's balance with commas separating the place values
     * @param player the player to get the balance from
     */
    public static String commaSeperatedBalance(OfflinePlayer player) {
        return String.format("%,d", getBalance(player));
    }

    public static void addPlayerToTable(OfflinePlayer player) {
        dslContext.insertInto(Tables.ECONOMY, Tables.ECONOMY.USERID, Tables.ECONOMY.BALANCE).values(DatabaseTools.getUserID(player.getUniqueId()), 0).execute();
    }

    private static void addPlayerToTableIfDoesntExist(OfflinePlayer player) {
        if(dslContext.select(Tables.ECONOMY.USERID).from(Tables.ECONOMY)
                .where(Tables.ECONOMY.USERID.eq(DatabaseTools.getUserID(player.getUniqueId())))
                .fetch().getValues(Tables.ECONOMY.USERID).size() > 0) {
            return;
        }

        addPlayerToTable(player);
    }

    private static void updateBalance(OfflinePlayer player, int amount) {
        dslContext.update(Tables.ECONOMY).set(Tables.ECONOMY.BALANCE, amount)
                .where(Tables.ECONOMY.USERID.eq(DatabaseTools.getUserID(player.getUniqueId()))).execute();
    }

}
