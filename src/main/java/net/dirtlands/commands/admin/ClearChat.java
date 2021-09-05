package net.dirtlands.commands.admin;

import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.tools.ConfigTools;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClearChat extends PluginCommand {

    @Override
    public String getName() {
        return "clearchat";
    }

    @Override
    protected Permission getPermissionType() {
        return Permission.CLEARCHAT;
    }

    @Override
    public boolean isRequiresPlayer() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        for(int i = 0; i < 300; i++){
            Bukkit.broadcast(Component.text(""));
        }
        if (sender instanceof Player){
            Bukkit.broadcast(ConfigTools.parseFromPath("Chat Cleared By Message", Template.of("Player", ((Player) sender).displayName())));
        } else {
            Bukkit.broadcast(ConfigTools.parseFromPath("Chat Cleared By Message", Template.of("Player", "console")));
        }
    }
}
