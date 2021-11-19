package net.dirtlands.commands.warp;

import jeeper.utils.LocationParser;
import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.files.Warps;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SetWarp extends PluginCommand {
    Warps warps = Main.getPlugin().warps();
    private static ConfigSetup config = Main.getPlugin().config();

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

            warps.get().set("Warps."+ args[0] + ".Coords", LocationParser.locationToString(loc));
            warps.save();
            warps.reload();

            player.sendMessage(MessageTools.parseFromPath(config, "Warp Created", Template.template("Name", args[0])));

        }
    }

}
