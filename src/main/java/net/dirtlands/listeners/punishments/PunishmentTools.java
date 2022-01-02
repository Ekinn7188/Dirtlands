package net.dirtlands.listeners.punishments;

import dirtlands.db.Tables;
import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.database.DatabaseTools;
import net.dirtlands.log.LogColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.kyori.adventure.text.minimessage.Template;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;
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
                            LocalDateTime endTime = currentTime.plus(parseInt(matcher.group(1)), ChronoUnit.DAYS);
                            endTime = endTime.plus(parseInt(matcher.group(2)), ChronoUnit.HOURS);
                            endTime = endTime.plus(parseInt(matcher.group(3)), ChronoUnit.MINUTES);
                            addPunishmentToDB(player, punishment, punisher.getUniqueId().toString(), "", punished, currentTime, endTime, reason);
                            return AnvilGUI.Response.close();
                        } catch (NumberFormatException exception) {
                            player.sendMessage(MessageTools.parseFromPath(config, "Punishment Time Invalid"));
                            return AnvilGUI.Response.close();
                        }
                    } else {
                        if (text.equalsIgnoreCase("permanent")) {
                            addPunishmentToDB(player, punishment, punisher.getUniqueId().toString(), "", punished, LocalDateTime.now(), null, reason);
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
                    if (punishment.equals(Punishment.WARN)) {
                        addPunishmentToDB(player, punishment, punisher.getUniqueId().toString(), "", punished, LocalDateTime.now(), null, text);
                    }
                    else {
                        timeMenu(punishment, punished, punisher, text);
                    }
                    return AnvilGUI.Response.close();
                })
                .text("Reason")
                .title(punishment.getPunishment() + " Reason Editor")
                .plugin(Main.getPlugin())
                .open((Player) punisher);
    }

    public static void addPunishmentToDB(CommandSender sender, Punishment punishment, String punisherUUID, String punishedIP, OfflinePlayer punished, LocalDateTime currentTime, LocalDateTime endTime, String reason) {
        String punishedName = punished.getName();
        if (punishedName == null) {
            return;
        }

        if (punishment.equals(Punishment.IP_BAN)) {
            if (reason == null) {
                Bukkit.broadcast(MessageTools.parseFromPath(config, "IP Ban No Reason Broadcast",
                        Template.template("player", punishedName), Template.template("time", getPunishmentEndString(endTime))));
            } else {
                Bukkit.broadcast(MessageTools.parseFromPath(config, "IP Ban Broadcast",
                        Template.template("player", punishedName), Template.template("reason", reason), Template.template("time", getPunishmentEndString(endTime))));
            }
        }if (punishment.equals(Punishment.BAN)) {
            if (reason == null) {
                Bukkit.broadcast(MessageTools.parseFromPath(config, "Ban No Reason Broadcast",
                        Template.template("player", punishedName), Template.template("time", getPunishmentEndString(endTime))));
            } else {
                Bukkit.broadcast(MessageTools.parseFromPath(config, "Ban Broadcast",
                        Template.template("player", punishedName), Template.template("reason", reason), Template.template("time", getPunishmentEndString(endTime))));
            }
        } else if (punishment.equals(Punishment.MUTE)) {
            if (reason.equals("")) {
                sender.sendMessage(MessageTools.parseFromPath(config, "Mute Successful No Reason",
                        Template.template("player", punishedName), Template.template("time", getPunishmentEndString(endTime))));
            } else {
                sender.sendMessage(MessageTools.parseFromPath(config, "Mute Successful",
                        Template.template("player", punishedName), Template.template("reason", reason), Template.template("time", getPunishmentEndString(endTime))));
            }
        }


        int punishedID = DatabaseTools.getUserID(punished.getUniqueId());
        int punisherID = DatabaseTools.getUserID(punisherUUID);

        if (!punishment.equals(Punishment.IP_BAN)) {
            dslContext.insertInto(Tables.PUNISHMENTS,
                            Tables.PUNISHMENTS.USERID, Tables.PUNISHMENTS.PUNISHERID,
                            Tables.PUNISHMENTS.PUNISHMENTTYPE, Tables.PUNISHMENTS.PUNISHMENTREASON,
                            Tables.PUNISHMENTS.PUNISHMENTSTART, Tables.PUNISHMENTS.PUNISHMENTEND)
                    .values(punishedID, (punisherID == -1 ? null : punisherID), punishment.getPunishment(), reason, currentTime, endTime).execute();
        } else {
            dslContext.insertInto(Tables.PUNISHMENTS,
                            Tables.PUNISHMENTS.USERID, Tables.PUNISHMENTS.PUNISHERID, Tables.PUNISHMENTS.IPADDRESS,
                            Tables.PUNISHMENTS.PUNISHMENTTYPE, Tables.PUNISHMENTS.PUNISHMENTREASON,
                            Tables.PUNISHMENTS.PUNISHMENTSTART, Tables.PUNISHMENTS.PUNISHMENTEND)
                    .values(punishedID, (punisherID == -1 ? null : punisherID), punishedIP, punishment.getPunishment(), reason, currentTime, endTime).execute();

        }

        String pastTensePunishment = switch(punishment) {
            case BAN -> "banned";
            case MUTE -> "muted";
            case WARN -> "warned";
            case KICK -> "kicked";
            case IP_BAN -> "ip banned";
        };
        Color punishmentColor = switch(punishment) {
            case IP_BAN -> Color.DARK_GRAY;
            case BAN -> Color.RED;
            case MUTE -> new Color(255, 140, 0);
            case KICK -> Color.ORANGE;
            case WARN -> Color.GREEN;
        };

        String punisherName = (punisherID == -1 ? "Console" : Bukkit.getOfflinePlayer(UUID.fromString(punisherUUID)).getName());
        String ipString = (punishment.equals(Punishment.IP_BAN) ? " (" + punishedIP + ")" : "");
        if (reason == null) {
            Bukkit.getLogger().warning(LogColor.RED + punisherName + " has " + pastTensePunishment + " " + punishedName + ipString + LogColor.RESET);
        } else {
            Bukkit.getLogger().warning(LogColor.RED + punisherName + " has " + pastTensePunishment + " " + punishedName + " for " + reason + ipString + LogColor.RESET);
        }

        String currentTimeString = DatabaseTools.localDateTimeToString(currentTime).replaceAll(":\\d\\d\\.\\d\\d\\d", "");
        final String endTimeString;
        if (endTime != null) {
            endTimeString = DatabaseTools.localDateTimeToString(endTime).replaceAll(":\\d\\d\\.\\d\\d\\d", "");
        } else {
            endTimeString = null;
        }

        Main.getPlugin().getJda().getGuilds().forEach(guild -> {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Player " + pastTensePunishment);
            embedBuilder.addField("**Player**", punishedName, true);
            embedBuilder.addField("**Punished By**", punisherName, true);
            embedBuilder.addField("Reason: ", reason == null ? "none" : reason, false);
            if (endTimeString != null) {
                embedBuilder.addField("**Start Time:**", currentTimeString, true);
                embedBuilder.addField("**End Time:**", endTimeString, true);
            }

            embedBuilder.setColor(punishmentColor);
            embedBuilder.setThumbnail("https://minotar.net/helm/" + punishedName + "/64");
            try {
                Objects.requireNonNull(guild.getTextChannelById(921676351696687175L)).sendMessage(" ").setEmbeds(embedBuilder.build()).queue();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

        });



        //kick player if ban
        if (punished.isOnline()) {
            Player player = punished.getPlayer();
            if (player != null) {
                if (punishment.equals(Punishment.IP_BAN)) {
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        var address = online.getAddress();
                        if (address == null) {
                            return;
                        }
                        String ip = online.getAddress().getAddress().getHostAddress();

                        if (ip.equals(punishedIP)) {
                            banKick(endTime, reason, online);
                        }
                    }
                }if (punishment.equals(Punishment.BAN)) {
                    banKick(endTime, reason, player);
                } else if (punishment.equals(Punishment.WARN)) {

                    var condition = Tables.PUNISHMENTS.PUNISHMENTEND.isNull()
                            .and(Tables.PUNISHMENTS.USERID.eq(punishedID).and(Tables.PUNISHMENTS.PUNISHMENTTYPE.eq(Punishment.WARN.getPunishment())));

                    var userWarns = dslContext.select(Tables.PUNISHMENTS.PUNISHMENTEND).from(Tables.PUNISHMENTS)
                            .where(condition).fetch().size();




                    if (userWarns < 3) {
                        if (reason == null) {
                            player.sendMessage(MessageTools.parseFromPath(config, "Warn Message No Reason",
                                    Template.template("player", punishedName),
                                    Template.template("offence", String.valueOf(userWarns))));
                            return;
                        }
                        player.sendMessage(MessageTools.parseFromPath(config, "Warn Message",
                                Template.template("player", punishedName), Template.template("reason", reason),
                                Template.template("offence", String.valueOf(userWarns))));
                    } else {
                        //recursive call to ban
                        addPunishmentToDB(sender, Punishment.BAN, punisherUUID, "", punished, currentTime, currentTime.plus(1, ChronoUnit.WEEKS), "Gaining Three Warnings");

                        dslContext.update(Tables.PUNISHMENTS).set(Tables.PUNISHMENTS.PUNISHMENTEND, LocalDateTime.now())
                                .where(condition).execute();
                    }
                }
            }
        }
    }

    private static void banKick(LocalDateTime endTime, String reason, Player player) {
        if (endTime == null) {
            if (reason == null) {
                player.kick(PreventJoin.permBanNoReasonMessage());
            } else {
                player.kick(PreventJoin.permBanMessage(reason));
            }
        } else {
            if (reason == null) {
                player.kick(PreventJoin.tempBanNoReasonMessage(endTime));
            } else {
                player.kick(PreventJoin.tempBanReasonMessage(endTime, reason));
            }
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
                        .and(Tables.PUNISHMENTS.PUNISHMENTTYPE.eq(Punishment.MUTE.getPunishment()))
                        .and(DSL.condition(dslContext.select(Tables.PUNISHMENTS.PUNISHMENTEND)
                                .from(Tables.PUNISHMENTS)
                                .where(Tables.PUNISHMENTS.PUNISHMENTTYPE.equalIgnoreCase(Punishment.MUTE.getPunishment())
                                        .and(Tables.PUNISHMENTS.USERID.eq(DatabaseTools.getUserID(player.getUniqueId()))))
                                .fetch(Tables.PUNISHMENTS.PUNISHMENTEND)
                                .stream().filter(Objects::nonNull).anyMatch(time -> time.isAfter(LocalDateTime.now())))
                                .or(Tables.PUNISHMENTS.PUNISHMENTEND.isNull())))
                .orderBy(Tables.PUNISHMENTS.PUNISHMENTEND.desc().nullsFirst()).fetchAny();

        //if there is no record, the player is not muted
        if (record == null) {
            return false;
        }

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



}
