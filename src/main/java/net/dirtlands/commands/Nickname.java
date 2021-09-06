package net.dirtlands.commands;

import net.dirtlands.tools.ConfigTools;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.entity.Player;

public class Nickname extends PluginCommand {
    @Override
    public String getName() {
        return "nickname";
    }

    @Override
    protected Permission getPermissionType() {
        return Permission.NICKNAME;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length > 0){
            if (args[0].equals("reset")){
                player.displayName(ConfigTools.parseText(player.getName()));
                player.sendMessage(ConfigTools.parseFromPath("Nickname Change", Template.of("Name", player.getName())));
                return;
            }
            player.displayName(ConfigTools.parseText(args[0]));
            player.sendMessage(ConfigTools.parseFromPath("Nickname Change", Template.of("Name", player.displayName())));
        }
    }
}
