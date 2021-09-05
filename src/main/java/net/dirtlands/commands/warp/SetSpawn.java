package net.dirtlands.commands.warp;

import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.files.Warps;
import net.dirtlands.tools.ConfigTools;
import net.dirtlands.tools.LocationTools;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SetSpawn extends PluginCommand {

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

        Warps.get().set("Spawn.Coords", LocationTools.locationToString(loc));
        Warps.save();
        Warps.reload();

        player.sendMessage(ConfigTools.parseFromPath("Spawn Set"));
    }
}
