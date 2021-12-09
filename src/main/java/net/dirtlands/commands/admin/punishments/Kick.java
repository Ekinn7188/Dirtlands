package net.dirtlands.commands.admin.punishments;

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

import java.util.Arrays;

public class Kick extends PluginCommand {
    ConfigSetup config = Main.getPlugin().config();

    @Override
    public String getName() {
        return "kick";
    }

    @Override
    protected Permission getPermissionType() {
        return Permission.KICK;
    }

    @Override
    public boolean isRequiresPlayer() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(MessageTools.parseText("&cUsage: /kick {player} {reason (optional)}"));
            return;
        }

        Player player = Bukkit.getPlayer(args[0]);

        try{
            assert player != null;
            if (args.length == 1) {
                player.kick(MessageTools.parseFromPath(config, "Punishment Header").append(Component.newline())
                        .append(MessageTools.parseFromPath(config, "Kick Message")));
            } else {
                String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

                player.kick(MessageTools.parseFromPath(config, "Punishment Header").append(Component.newline())
                        .append(MessageTools.parseFromPath(config, "Kick With Reason", Template.template("reason", reason))));
            }
        } catch (AssertionError e) {
            sender.sendMessage(MessageTools.parseFromPath(config, "Player Is Offline", Template.template("player", args[0])));
        }


    }
}
