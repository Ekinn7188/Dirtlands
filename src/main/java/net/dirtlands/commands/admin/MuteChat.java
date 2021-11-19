package net.dirtlands.commands.admin;

import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MuteChat extends PluginCommand {

    public static boolean chatMuted = false;
    private static ConfigSetup config = Main.getPlugin().config();

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
            Bukkit.broadcast(MessageTools.parseFromPath(config,"Chat Muted By Message", Template.template("Player", commandSender)));
        } else {
            Bukkit.broadcast(MessageTools.parseFromPath(config, "Chat Unmuted By Message", Template.template("Player", commandSender)));
        }
    }
}
