package net.dirtlands.commands.admin;

import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.tools.ConfigTools;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class Broadcast extends PluginCommand {
    @Override
    public String getName() {
        return "broadcast";
    }

    @Override
    protected Permission getPermissionType() {
        return Permission.BROADCAST;
    }

    @Override
    public boolean isRequiresPlayer() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 0) {
            Bukkit.broadcast(ConfigTools.parseFromPath("Broadcast Prefix").append(MiniMessage.get().parse(String.join(" ", args))));
        }
    }
}