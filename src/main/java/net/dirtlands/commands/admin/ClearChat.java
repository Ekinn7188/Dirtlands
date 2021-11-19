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

public class ClearChat extends PluginCommand {
    private static ConfigSetup config = Main.getPlugin().config();

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
            Bukkit.broadcast(MessageTools.parseFromPath(config, "Chat Cleared By Message", Template.template("Player", ((Player) sender).displayName())));
        } else {
            Bukkit.broadcast(MessageTools.parseFromPath(config, "Chat Cleared By Message", Template.template("Player", "console")));
        }
    }
}
