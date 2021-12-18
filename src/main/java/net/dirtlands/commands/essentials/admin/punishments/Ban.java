package net.dirtlands.commands.essentials.admin.punishments;

import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.listeners.punishments.Punishment;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class Ban extends PluginCommand {
    ConfigSetup config = Main.getPlugin().config();


    @Override
    public String getName() {
        return "ban";
    }

    @Override
    protected Permission getPermissionType() {
        return Permission.BAN;
    }

    @Override
    public boolean isRequiresPlayer() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(MessageTools.parseText("&cUsage: /ban {player}"));
            return;
        }

        if (sender instanceof Player p && args.length == 1) {
            try{
                p.openInventory(Objects.requireNonNull(Punishments.getPunishmentsMenu(Punishment.BAN, p, args[0])));
                return;
            } catch (NullPointerException e) {
                //try console command below
            }
        }

        //ban {player} 0d0h0m {reason}
        if (args.length >= 2) {
            Punishments.consolePunishment(Punishment.BAN, sender, args);
        } else {
            sender.sendMessage(MessageTools.parseText("&cUsage: /ban {player} {time | 0d0h0m} {reason}"));
        }




    }


}
