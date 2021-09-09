package net.dirtlands.economy;

import net.dirtlands.Main;
import net.dirtlands.files.Playerdata;
import net.dirtlands.tools.MessageTools;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.entity.Player;

public class Economy {

    public static Playerdata playerdata = Main.getPlugin().playerdata();

    /**
     *
     * Give money to a player.
     *
     * @param player player to give money to
     * @param amount how much money to give
     */
    public static void addMoney(Player player, int amount) {

        int balance = playerdata.get().getInt(player.getUniqueId() + ".balance");
        balance += amount;
        playerdata.get().set(player.getUniqueId() + ".balance", balance);
        playerdata.save();
        playerdata.reload();
        player.sendActionBar(MessageTools.parseFromPath("Money Gained Actionbar",
                Template.of("money", String.valueOf(amount)), Template.of("balance", String.valueOf(balance))));
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
        int balance = playerdata.get().getInt(player.getUniqueId() + ".balance");
        if (balance - amount < 0) {
            return false;
        }
        balance -= amount;
        playerdata.get().set(player.getUniqueId() + ".balance", balance);
        playerdata.save();
        playerdata.reload();
        player.sendActionBar(MessageTools.parseFromPath("Money Lost Actionbar",
                Template.of("money", String.valueOf(amount)), Template.of("balance", String.valueOf(balance))));
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
        int balance = playerdata.get().getInt(player.getUniqueId() + ".balance");
        balance -= amount;
        playerdata.get().set(player.getUniqueId() + ".balance", balance);
        playerdata.save();
        playerdata.reload();
        player.sendActionBar(MessageTools.parseFromPath("Money Lost Actionbar",
                Template.of("money", String.valueOf(amount)), Template.of("balance", String.valueOf(balance))));
    }

    /**
     *
     * Sets a player's balance.
     *
     * @param player player to set money for
     * @param amount how much money to set
     */
    public static void setBalance(Player player, int amount) {
        playerdata.get().set(player.getUniqueId() + ".balance", amount);
        playerdata.save();
        playerdata.reload();

        player.sendActionBar(MessageTools.parseFromPath("Money Set Actionbar",
                Template.of("money", String.valueOf(amount))));
    }

    /**
     *
     * Gets a player's balance.
     *
     * @param player player to remove money from
     */
    public static int getBalance(Player player) {
        return playerdata.get().getInt(player.getUniqueId() + ".balance");
    }


}
