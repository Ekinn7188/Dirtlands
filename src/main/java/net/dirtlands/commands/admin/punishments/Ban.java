package net.dirtlands.commands.admin.punishments;

import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.listeners.punishments.Punishment;
import net.dirtlands.listeners.punishments.PunishmentTools;
import net.dirtlands.tools.UUIDTools;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

public class Ban extends PluginCommand {
    ConfigSetup config = Main.getPlugin().config();
    static Pattern anvilTimePattern = Pattern.compile("([0-9])+d([0-9])+h([0-9])+m", Pattern.CASE_INSENSITIVE);

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
                return;
            }
        }

        //ban {player} 0d0h0m {reason}
        if (args.length >= 2) {
            OfflinePlayer punished = UUIDTools.checkNameAndUUID(sender, args[0]);
            if (punished == null) {
                return;
            }
            assert punished.getName() != null; //checked in checkNameAndUUID

            Matcher matcher = anvilTimePattern.matcher(args[1]);
            if (matcher.find()) {
                try {
                    LocalDateTime currentTime = LocalDateTime.now();
                    LocalDateTime endTime = currentTime.plus(parseInt(matcher.group(1)), ChronoUnit.DAYS);
                    endTime = endTime.plus(parseInt(matcher.group(2)), ChronoUnit.HOURS);
                    endTime = endTime.plus(parseInt(matcher.group(3)), ChronoUnit.MINUTES);
                    String[] reason = Arrays.copyOfRange(args, 2, args.length);
                    if (reason.length == 0) {
                        PunishmentTools.addPunishmentToDB(Punishment.BAN, "Console", punished, LocalDateTime.now(), endTime,
                                String.join(" ", ""));
                        return;
                    }
                    PunishmentTools.addPunishmentToDB(Punishment.BAN, "Console", punished, LocalDateTime.now(), endTime,
                            String.join(" ", String.join(" ", reason)));
                } catch (NumberFormatException e) {
                    sender.sendMessage(MessageTools.parseFromPath(config, "Punishment Time Invalid"));
                }
            } else {
                String[] reason = Arrays.copyOfRange(args, 1, args.length);
                PunishmentTools.addPunishmentToDB(Punishment.BAN,"Console", punished, LocalDateTime.now(), null,
                        String.join(" ", String.join(" ", reason)));
            }
        } else {
            sender.sendMessage(MessageTools.parseText("&cUsage: /ban {player} {time | 0h0d0m} {reason}"));
        }




    }
}
