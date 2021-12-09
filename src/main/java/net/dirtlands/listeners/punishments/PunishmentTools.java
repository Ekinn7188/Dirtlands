package net.dirtlands.listeners.punishments;

import dirtlands.db.Tables;
import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.database.DatabaseTools;
import net.kyori.adventure.text.minimessage.Template;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

public class PunishmentTools {
    static Pattern anvilTimePattern = Pattern.compile("([0-9]+)d\\s([0-9]+)h\\s([0-9]+)m", Pattern.CASE_INSENSITIVE);
    static ConfigSetup config = Main.getPlugin().config();
    static DSLContext dslContext = Main.getPlugin().getDslContext();

    protected static void timeMenu(Punishment punishment, OfflinePlayer punished, HumanEntity punisher, String reason) {

        punisher.sendMessage(MessageTools.parseFromPath(config, "Punishment Time"));

        new AnvilGUI.Builder()
                .onComplete((player, text) -> {
                    Matcher matcher = anvilTimePattern.matcher(text);
                    if (matcher.find()) {
                        try {
                            LocalDateTime currentTime = LocalDateTime.now();
                            System.out.println(matcher.group(1));
                            LocalDateTime endTime = currentTime.plus(parseInt(matcher.group(1)), ChronoUnit.DAYS);
                            endTime = endTime.plus(parseInt(matcher.group(2)), ChronoUnit.HOURS);
                            endTime = endTime.plus(parseInt(matcher.group(3)), ChronoUnit.MINUTES);
                            addPunishmentToDB(punishment, punisher.getUniqueId().toString(), punished, currentTime, endTime, reason);
                            return AnvilGUI.Response.close();
                        } catch (NumberFormatException exception) {
                            player.sendMessage(MessageTools.parseFromPath(config, "Punishment Time Invalid"));
                            return AnvilGUI.Response.close();
                        }
                    } else {
                        if (text.equalsIgnoreCase("permanent")) {
                            addPunishmentToDB(punishment, punisher.getUniqueId().toString(), punished, LocalDateTime.now(), null, reason);
                            return AnvilGUI.Response.close();
                        }

                        player.sendMessage(MessageTools.parseFromPath(config, "Punishment Time Invalid"));
                        return AnvilGUI.Response.close();
                    }
                })
                .text("0d 0h 0m")
                .title(punishment.getPunishment() + " Time Editor")
                .plugin(Main.getPlugin())
                .open((Player) punisher);
    }

    protected static void customPunishment(Punishment punishment, OfflinePlayer punished, HumanEntity punisher) {
        new AnvilGUI.Builder()
                .onComplete((player, text) -> {
                    timeMenu(punishment, punished, punisher, text);
                    return AnvilGUI.Response.close();
                })
                .text("Reason")
                .title(punishment.getPunishment() + " Reason Editor")
                .plugin(Main.getPlugin())
                .open((Player) punisher);
    }

    public static void addPunishmentToDB(Punishment punishment, String bannerID, OfflinePlayer punished, LocalDateTime currentTime, LocalDateTime endTime, String reason) {
        String punishedName = punished.getName();
        if (punishedName == null) {
            return;
        }

        //kick player if ban
        if (punishment.equals(Punishment.BAN)) {
            if (punished.isOnline()) {
                Player player = punished.getPlayer();

                if (player != null) {
                    if (endTime == null) {
                        if (reason.equals("")) {
                            player.kick(PreventJoin.permBanNoReasonMessage());
                        }
                        else {
                            player.kick(PreventJoin.permBanMessage(reason));
                        }
                    } else {
                        if (reason.equals("")) {
                            player.kick(PreventJoin.tempBanNoReasonMessage(endTime));
                        } else {
                            player.kick(PreventJoin.tempBanReasonMessage(endTime, reason));
                        }
                    }
                }
            }
        }

        int userId = DatabaseTools.getUserID(punished.getUniqueId());
        int punishedId = DatabaseTools.getUserID(bannerID);

        dslContext.insertInto(Tables.PUNISHMENTS,
                        Tables.PUNISHMENTS.USERID, Tables.PUNISHMENTS.PUNISHERID,
                        Tables.PUNISHMENTS.PUNISHMENTTYPE, Tables.PUNISHMENTS.PUNISHMENTREASON,
                        Tables.PUNISHMENTS.PUNISHMENTSTART, Tables.PUNISHMENTS.PUNISHMENTEND)
                .values(userId, (punishedId == -1 ? null : punishedId), punishment.getPunishment(), reason, currentTime, endTime).execute();

        System.out.println(dslContext.selectFrom(Tables.PUNISHMENTS).fetch());

        if (punishment.equals(Punishment.BAN)) {
            if (reason.equals("")) {
                Bukkit.broadcast(MessageTools.parseFromPath(config, "Ban No Reason Broadcast",
                        Template.template("player", punishedName), Template.template("time", getPunishmentEndString(endTime))));
                return;
            }
            Bukkit.broadcast(MessageTools.parseFromPath(config, "Ban Broadcast",
                    Template.template("player", punishedName), Template.template("reason", reason), Template.template("time", getPunishmentEndString(endTime))));
        } else if (punishment.equals(Punishment.MUTE)) {
            if (reason.equals("")) {
                Bukkit.broadcast(MessageTools.parseFromPath(config, "Mute Successful No Reason",
                        Template.template("player", punishedName), Template.template("time", getPunishmentEndString(endTime))));
                return;
            }
            Bukkit.broadcast(MessageTools.parseFromPath(config, "Mute Successful",
                    Template.template("player", punishedName), Template.template("reason", reason), Template.template("time", getPunishmentEndString(endTime))));
        }
    }

    /**
     * @param endTime the time the ban ends
     * @return the string for the punishment's end time in the form 0d 0h 0m
     */
    public static String getPunishmentEndString(LocalDateTime endTime) {
        var banEndDifferenceRecord = dslContext.select(DSL.localDateTimeDiff(endTime, LocalDateTime.now())).fetchAny();

        if (banEndDifferenceRecord == null) {
            return "";
        }
        var banEndDifference = banEndDifferenceRecord.value1();

        if (banEndDifference == null) {
            return "permanent";
        }

        return banEndDifference.getDays() + "d " + banEndDifference.getHours() + "h " + banEndDifference.getMinutes() + "m";
    }

    public static boolean checkMuted(Player player) {
        //get a record that has a currently-running mute punishment
        var record = dslContext.select(Tables.PUNISHMENTS.USERID, Tables.PUNISHMENTS.PUNISHMENTREASON, Tables.PUNISHMENTS.PUNISHMENTEND)
                .from(Tables.PUNISHMENTS)
                .where(Tables.PUNISHMENTS.USERID.eq(DatabaseTools.getUserID(player.getUniqueId()))
                        .and(Tables.PUNISHMENTS.PUNISHMENTTYPE.eq(Punishment.MUTE.getPunishment())))
                .orderBy(Tables.PUNISHMENTS.PUNISHMENTEND.desc()).fetchAny();

        //if there is no record, the player is not muted
        if (record == null) {
            return false;
        }

        if (record.get(Tables.PUNISHMENTS.PUNISHMENTEND).isAfter(LocalDateTime.now())) {
            if (record.get(Tables.PUNISHMENTS.PUNISHMENTREASON).isEmpty()) {
                //permanent mute no reason
                if (record.get(Tables.PUNISHMENTS.PUNISHMENTEND) == null) {
                    player.sendMessage(MessageTools.parseFromPath(config, "Permanent Mute No Reason"));
                    return true;
                }
                //mute no reason
                player.sendMessage(MessageTools.parseFromPath(config, "Muted No Reason",
                        Template.template("time", getPunishmentEndString(record.get(Tables.PUNISHMENTS.PUNISHMENTEND)))));
                return true;
            }

            //perm with reason
            if (record.get(Tables.PUNISHMENTS.PUNISHMENTEND) == null) {
                player.sendMessage(MessageTools.parseFromPath(config, "Permanent Mute With Reason",
                        Template.template("reason", record.get(Tables.PUNISHMENTS.PUNISHMENTREASON))));
                return true;
            }

            //muted with reason
            player.sendMessage(MessageTools.parseFromPath(config, "Muted With Reason",
                    Template.template("reason", record.get(Tables.PUNISHMENTS.PUNISHMENTREASON)),
                    Template.template("time", getPunishmentEndString(record.get(Tables.PUNISHMENTS.PUNISHMENTEND)))));
            return true;
        }
        return false;
    }

}
