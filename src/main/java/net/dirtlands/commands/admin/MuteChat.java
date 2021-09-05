package net.dirtlands.commands.admin;

import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.tools.ConfigTools;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MuteChat extends PluginCommand {

    public static boolean chatMuted = false;

    @Override
    public String getName() {
        return "mutechat";
    }

    @Override
    protected Permission getPermissionType() {
        return Permission.MUTECHAT;
    }

    @Override
    public boolean isRequiresPlayer() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        chatMuted = !chatMuted;
        Component commandSender = (sender instanceof Player) ? ((Player) sender).displayName() : Component.text("console");

        if (chatMuted){
            Bukkit.broadcast(ConfigTools.parseFromPath("Chat Muted By Message", Template.of("Player", commandSender)));
        } else {
            Bukkit.broadcast(ConfigTools.parseFromPath("Chat Unmuted By Message", Template.of("Player", commandSender)));
        }
    }
}
