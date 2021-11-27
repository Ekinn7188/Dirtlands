package net.dirtlands.commands.admin;

import jeeper.utils.MessageTools;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Sudo extends PluginCommand {
    @Override
    public String getName() {
        return "sudo";
    }

    @Override
    protected Permission getPermissionType() {
        return Permission.SUDO;
    }

    @Override
    public boolean isRequiresPlayer() {
       return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            sender.sendMessage(MessageTools.parseText("&cUsage: /sudo {player} {command}"));
            return;
        }
        Player player = Main.getPlugin().getServer().getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage("Player not found");
            return;
        }

        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, newArgs.length);

        player.chat(String.join(" ", newArgs));
    }
}
