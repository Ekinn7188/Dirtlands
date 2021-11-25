package net.dirtlands.commands.warp;

import dirtlands.db.Tables;
import jeeper.utils.LocationParser;
import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;

public class SetWarp extends PluginCommand {

    private static ConfigSetup config = Main.getPlugin().config();
    DSLContext dslContext = Main.getPlugin().getDslContext();

    @Override
    public String getName() {
        return "setwarp";
    }

    @Override
    protected Permission getPermissionType() {
        return Permission.SETWARP;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 1) {
            Location loc = player.getLocation();
            try {
                dslContext.insertInto(Tables.WARPS).columns(Tables.WARPS.WARPNAME, Tables.WARPS.WARPLOCATION)
                        .values(args[0], LocationParser.roundedLocationToString(loc)).execute();
            } catch (DataAccessException e) {
                //if the value already exists, then just update
                dslContext.update(Tables.WARPS).set(Tables.WARPS.WARPLOCATION, LocationParser.roundedLocationToString(loc))
                        .where(Tables.WARPS.WARPNAME.equalIgnoreCase(args[0])).execute();
            }

            player.sendMessage(MessageTools.parseFromPath(config, "Warp Created", Template.template("name", args[0])));
            return;
        }
        if (args.length == 2) {
            Location loc = player.getLocation();
            try {
                dslContext.insertInto(Tables.WARPS).columns(Tables.WARPS.WARPNAME, Tables.WARPS.WARPLOCATION, Tables.WARPS.WARPPERMISSION)
                        .values(args[0], LocationParser.roundedLocationToString(loc), args[1]).execute();
            } catch (DataAccessException e) {
                //if the value already exists, then just update
                dslContext.update(Tables.WARPS).set(Tables.WARPS.WARPLOCATION, LocationParser.roundedLocationToString(loc))
                        .set(Tables.WARPS.WARPPERMISSION, args[1])
                        .where(Tables.WARPS.WARPNAME.equalIgnoreCase(args[0])).execute();
            }

            player.sendMessage(MessageTools.parseFromPath(config, "Warp Created", Template.template("name", args[0])));
            return;
        }
        player.sendMessage(MessageTools.parseText("<red>Correct usage: /setwarp {name} {permission (optional)}"));
    }

}
