package net.dirtlands.commands.admin;

import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
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
                Main.getPlugin().config().reload();
                Main.getPlugin().warps().reload();
                Main.getPlugin().npcInventory().reload();
                sender.sendMessage(ConfigTools.parseFromPath("Dirtlands Reloaded"));
            }
        }
    }
}
