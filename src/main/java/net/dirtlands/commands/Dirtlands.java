package net.dirtlands.commands;

import jeeper.utils.MessageTools;
import net.dirtlands.Main;
import org.bukkit.command.CommandSender;

public class Dirtlands extends PluginCommand {
    @Override
    public String getName() {
        return "dirtlands";
    }

    @Override
    protected Permission getPermissionType() {
        return Permission.DIRTLANDS;
    }

    @Override
    public boolean isRequiresPlayer() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                Main.getPlugin().config().reload();

                sender.sendMessage(MessageTools.parseFromPath(Main.getPlugin().config(), "Dirtlands Reloaded"));
            }
        }
    }
}
