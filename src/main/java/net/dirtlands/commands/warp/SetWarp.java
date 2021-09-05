package net.dirtlands.commands.warp;

import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.files.Warps;
import net.dirtlands.tools.ConfigTools;
import net.dirtlands.tools.LocationTools;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SetWarp extends PluginCommand {

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
        if (args.length > 0){
            Location loc = player.getLocation();

            Warps.get().set("Warps."+ args[0] + ".Coords", LocationTools.locationToString(loc));
            Warps.save();
            Warps.reload();

            player.sendMessage(ConfigTools.parseFromPath("Warp Created", Template.of("Name", args[0])));

        }
    }

}
