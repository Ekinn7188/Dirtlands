package net.dirtlands.commands.economy;

import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.economy.Economy;
import net.dirtlands.tools.UUIDTools;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jooq.DSLContext;

import java.util.Objects;

public class Bal extends PluginCommand {
    DSLContext dslContext = Main.getPlugin().getDslContext();
    ConfigSetup config = Main.getPlugin().config();

    @Override
    public String getName() {
        return "bal";
    }

    @Override
    public boolean isRequiresPlayer() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                player.sendMessage(MessageTools.parseFromPath(config, "Balance", Template.template("balance",
                        String.valueOf(Economy.getBalance(player)))));
                return;
            }
            sender.sendMessage(MessageTools.parseText("&cCorrect usage: /bal {player}"));
            return;
        }

        String uuid = UUIDTools.getUuid(args[0]);

        OfflinePlayer player = UUIDTools.checkNameAndUUID(sender, args[0]);
        if (player == null) {
            return;
        }

        if (player.getName() == null) {
            sender.sendMessage(MessageTools.parseFromPath(config,"Player Hasnt Logged In", Template.template("player", args[0])));
            return;
        }

        sender.sendMessage(MessageTools.parseFromPath(config, "Player Balance",
                Template.template("player", Objects.requireNonNull(player.getName())),
                Template.template("balance", Economy.commaSeperatedBalance(player))));
    }
}
