package net.dirtlands.commands;

import net.dirtlands.Main;
import net.dirtlands.files.Playerdata;
import net.dirtlands.tools.MessageTools;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class ChatColor extends PluginCommand {
    Playerdata playerdata = Main.getPlugin().playerdata();

    @Override
    public String getName() {
        return "chatcolor";
    }

    @Override
    protected Permission getPermissionType() {
        return Permission.CHATCOLOR;
    }

    @Override
    public void execute(Player player, String[] args) {

        if (args.length > 0) {

            if (!MessageTools.parseText(args[0]).equals(Component.text(args[0]))) {
                playerdata.get().set(player.getUniqueId() + ".chatcolor", args[0]);
                playerdata.save();
                playerdata.reload();
                player.sendMessage(MessageTools.parseFromPath("Chat Color Set"));

            } else {
                player.sendMessage(MessageTools.parseFromPath("Invalid Chat Color"));
            }
            return;
        }

        player.sendMessage(MessageTools.parseText("&cCorrect Usage: /chatcolor <color code>"));

    }
}
