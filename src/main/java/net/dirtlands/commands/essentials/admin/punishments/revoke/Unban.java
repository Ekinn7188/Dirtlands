package net.dirtlands.commands.essentials.admin.punishments.revoke;

import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.listeners.punishments.Punishment;
import org.bukkit.command.CommandSender;
import org.jooq.DSLContext;

public class Unban extends PluginCommand {

    DSLContext dslContext = Main.getPlugin().getDslContext();
    ConfigSetup config = Main.getPlugin().config();

    @Override
    public String getName() {
        return "unban";
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
            sender.sendMessage(MessageTools.parseText("&cUsage: /unban {player}"));
            return;
        }
        new RevokePunishment(Punishment.MUTE, sender, args);
    }
}
