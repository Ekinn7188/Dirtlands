package net.dirtlands.commands.warp;

import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.files.Warps;
import net.dirtlands.tools.LocationTools;
import net.dirtlands.tools.MessageTools;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SetSpawn extends PluginCommand {
    Warps warps = Main.getPlugin().warps();

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

        warps.get().set("Spawn.Coords", LocationTools.locationToString(loc));
        warps.save();
        warps.reload();

        player.sendMessage(MessageTools.parseFromPath("Spawn Set"));
    }
}
