package net.dirtlands.commands.admin;

import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.files.Config;
import net.dirtlands.files.Npcs;
import net.dirtlands.files.Warps;
import net.dirtlands.tools.ConfigTools;
import org.bukkit.command.CommandSender;

public class Dirtlands extends PluginCommand {
    @Override
    public String getName() {
        return "dirtlands";
    }

    @Override
    public boolean isRequiresPlayer() {
        return false;
    }

    @Override
    protected Permission getPermissionType() {
        return Permission.DIRTLANDS;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                Config.reload();
                Warps.reload();
                Npcs.reload();
                sender.sendMessage(ConfigTools.parseFromPath("Dirtlands Reloaded"));
            }
        }
    }
}
