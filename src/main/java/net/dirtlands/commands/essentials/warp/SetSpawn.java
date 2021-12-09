package net.dirtlands.commands.essentials.warp;

import dirtlands.db.Tables;
import jeeper.utils.LocationParser;
import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.database.DatabaseTools;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jooq.DSLContext;

public class SetSpawn extends PluginCommand {
    private static ConfigSetup config = Main.getPlugin().config();
    DSLContext dslContext = Main.getPlugin().getDslContext();

    @Override
    public String getName() {
        return "setspawn";
    }

    @Override
    protected Permission getPermissionType() {
        return Permission.SETSPAWN;
    }

    @Override
    public void execute(Player player, String[] args) {
        Location loc = player.getLocation();

        var spawn = DatabaseTools.firstString(dslContext.select(Tables.WARPS.WARPLOCATION).from(Tables.WARPS)
                .where(Tables.WARPS.WARPNAME.equalIgnoreCase("spawn")).fetchAny());

        if (spawn == null) {
            dslContext.insertInto(Tables.WARPS, Tables.WARPS.WARPNAME, Tables.WARPS.WARPLOCATION).values("spawn", LocationParser.locationToString(loc)).execute();
            player.sendMessage(MessageTools.parseFromPath(config, "Spawn Set"));
            return;
        }

        dslContext.update(Tables.WARPS).set(Tables.WARPS.WARPNAME, "spawn").set(Tables.WARPS.WARPLOCATION, LocationParser.locationToString(loc))
                .where(Tables.WARPS.WARPNAME.equalIgnoreCase("spawn")).execute();
        player.sendMessage(MessageTools.parseFromPath(config, "Spawn Set"));

    }
}
