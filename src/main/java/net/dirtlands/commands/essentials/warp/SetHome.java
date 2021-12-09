package net.dirtlands.commands.essentials.warp;

import dirtlands.db.Tables;
import jeeper.utils.LocationParser;
import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.database.DatabaseTools;
import net.dirtlands.tools.NumberAfterPermission;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jooq.DSLContext;

public class SetHome extends PluginCommand {

    private static ConfigSetup config = Main.getPlugin().config();
    DSLContext dslContext = Main.getPlugin().getDslContext();

    @Override
    public String getName() {
        return "sethome";
    }

    @Override
    protected Permission getPermissionType() {
        return Permission.SETHOME;
    }

    @Override
    public void execute(Player player, String[] args) {
        Location loc = player.getLocation();
        int largestSetHomeSize = NumberAfterPermission.get(player, "dirtlands.sethome.");

        if (largestSetHomeSize == -1) {
            largestSetHomeSize = 0;
        }

        int userID = DatabaseTools.getUserID(player.getUniqueId());

        var homesForUser = dslContext.select(Tables.HOMES.HOMENAME).from(Tables.HOMES).where(Tables.HOMES.USERID.eq(userID));

        String homeName = "home";

        if (args.length != 0) {
            homeName = args[0];
        }

        for (var record : homesForUser) {
            if (record.value1().equals(args[0])) {
                dslContext.update(Tables.HOMES).set(Tables.HOMES.HOMELOCATION, LocationParser.roundedLocationToString(loc))
                        .where(Tables.HOMES.USERID.eq(userID).and(Tables.HOMES.HOMENAME.eq(record.value1()))).execute();
                player.sendMessage(MessageTools.parseFromPath(config, "Home Created", Template.template("name", homeName)));
                return;
            }
        }

        if (homesForUser.execute() < largestSetHomeSize) {
            dslContext.insertInto(Tables.HOMES).columns(Tables.HOMES.USERID, Tables.HOMES.HOMENAME, Tables.HOMES.HOMELOCATION)
                    .values(userID, homeName, LocationParser.roundedLocationToString(loc)).execute();
            player.sendMessage(MessageTools.parseFromPath(config, "Home Created", Template.template("name", homeName)));
            return;
        }
        player.sendMessage(MessageTools.parseFromPath(config, "Too Many Homes", Template.template("number", String.valueOf(largestSetHomeSize))));
    }
}
