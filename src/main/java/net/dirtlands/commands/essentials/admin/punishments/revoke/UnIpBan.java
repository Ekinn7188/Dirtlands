package net.dirtlands.commands.essentials.admin.punishments.revoke;

import jeeper.utils.MessageTools;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.listeners.punishments.Punishment;
import org.bukkit.command.CommandSender;

public class UnIpBan extends PluginCommand {
    @Override
    public String getName() {
        return "unban-ip";
    }

    @Override
    protected Permission getPermissionType() {
        return Permission.BAN_IP;
    }

    @Override
    public boolean isRequiresPlayer() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(MessageTools.parseText("&cUsage: /unban-ip {player}"));
            return;
        }

        RevokePunishment.revoke(Punishment.IP_BAN, sender, args[0]);
    }
}
