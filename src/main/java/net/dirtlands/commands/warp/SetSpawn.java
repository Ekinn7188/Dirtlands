package net.dirtlands.commands.warp;

import jeeper.utils.LocationParser;
import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.files.Warps;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SetSpawn extends PluginCommand {
    Warps warps = Main.getPlugin().warps();
    private static ConfigSetup config = Main.getPlugin().config();

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

        warps.get().set("Spawn.Coords", LocationParser.locationToString(loc));
        warps.save();
        warps.reload();

        player.sendMessage(MessageTools.parseFromPath(config, "Spawn Set"));
    }
}
