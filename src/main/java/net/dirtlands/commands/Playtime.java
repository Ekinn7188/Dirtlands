package net.dirtlands.commands;

import jeeper.utils.MessageTools;
import net.dirtlands.Main;
import net.dirtlands.tools.UUIDTools;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Playtime extends PluginCommand {
    @Override
    public String getName() {
        return "playtime";
    }

    @Override
    public boolean isRequiresPlayer() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        OfflinePlayer player = (sender instanceof Player) ? (Player) sender : null;

        if (args.length == 0) {
            if (player == null) {
                sender.sendMessage(MessageTools.parseFromPath(Main.getPlugin().config(), "Correct Usage", Placeholder.parsed("command", "/playtime {player}")));
                return;
            }
        }
        if (args.length >= 1) {
            player = UUIDTools.checkNameAndUUID(sender, args[0]);
            if (player == null) {
                return;
            }
        }

        // Get time played in hours and minutes
        int minutes = player.getStatistic(Statistic.PLAY_ONE_MINUTE)/20/60;
        int hours = minutes/60;
        minutes = minutes%60;

        if (args.length == 0) {
            sender.sendMessage(MessageTools.parseFromPath(Main.getPlugin().config(), "Your Playtime",
                    Placeholder.parsed("hours", String.valueOf(hours)),
                    Placeholder.parsed("minutes", String.valueOf(minutes))));
        }
        else {
            sender.sendMessage(MessageTools.parseFromPath(Main.getPlugin().config(), "Other Playtime",
                    Placeholder.parsed("player", player.getName() == null ? "Unknown" : player.getName()),
                    Placeholder.parsed("hours", String.valueOf(hours)),
                    Placeholder.parsed("minutes", String.valueOf(minutes))));
        }

    }
}
