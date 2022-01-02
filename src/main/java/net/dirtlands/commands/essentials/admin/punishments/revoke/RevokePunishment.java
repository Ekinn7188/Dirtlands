package net.dirtlands.commands.essentials.admin.punishments.revoke;

import dirtlands.db.Tables;
import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.database.DatabaseTools;
import net.dirtlands.listeners.punishments.Punishment;
import net.dirtlands.log.LogColor;
import net.dirtlands.tools.UUIDTools;
import net.dv8tion.jda.api.EmbedBuilder;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.Objects;

public class RevokePunishment {

    static DSLContext dslContext = Main.getPlugin().getDslContext();
    static ConfigSetup config = Main.getPlugin().config();

    /**
     * Revokes a punishment
     * @param punishment The punishment to revoke
     * @param sender The sender of the command
     * @param playerName The arguments of the command (should be the player name)
     */
    public static void revoke(Punishment punishment, CommandSender sender, String playerName) {
        OfflinePlayer player = UUIDTools.checkNameAndUUID(sender, playerName);
        if (player == null) {
            sender.sendMessage(MessageTools.parseFromPath(config, "Player Doesnt Exist", Template.template("player", playerName)));
            return;
        }

        String uuid = player.getUniqueId().toString();



        var punishmentCondition =
                DSL.condition(dslContext.select(Tables.PUNISHMENTS.PUNISHMENTEND)
                                .from(Tables.PUNISHMENTS)
                                .where(Tables.PUNISHMENTS.PUNISHMENTTYPE.equalIgnoreCase(punishment.getPunishment())
                                        .and(Tables.PUNISHMENTS.USERID.eq(DatabaseTools.getUserID(uuid))))
                                .fetch(Tables.PUNISHMENTS.PUNISHMENTEND)
                                .stream().filter(Objects::nonNull).anyMatch(time -> time.isAfter(LocalDateTime.now())))
                        .or(Tables.PUNISHMENTS.PUNISHMENTEND.isNull())
                        .and(Tables.PUNISHMENTS.PUNISHMENTTYPE.equalIgnoreCase(punishment.getPunishment()))
                        .and(Tables.PUNISHMENTS.USERID.eq(DatabaseTools.getUserID(uuid)));

        var punishRecord = dslContext.selectFrom(Tables.PUNISHMENTS)
                .where(punishmentCondition).fetch();

        if (punishRecord.size() == 0) {
            if (punishment.equals(Punishment.BAN)) {
                sender.sendMessage(MessageTools.parseFromPath(config, "Unban Not Banned", Template.template("player", playerName)));
            } else if (punishment.equals(Punishment.MUTE)) {
                sender.sendMessage(MessageTools.parseFromPath(config, "Unmute Not Muted", Template.template("player", playerName)));
            } else if (punishment.equals(Punishment.IP_BAN)) {
                sender.sendMessage(MessageTools.parseFromPath(config, "Player Not IP Banned", Template.template("player", playerName)));
            }
            return;
        }

        Bukkit.getLogger().warning(LogColor.RED+(sender.getName().equals("CONSOLE") ? "Console" : sender.getName()) + " has revoked " + playerName + "'s " + punishment.getPunishment() + " punishment"+LogColor.RESET);

        punishRecord.forEach(p -> {
            p.setPunishmentend(LocalDateTime.now());
            dslContext.executeUpdate(p, punishmentCondition);//UPDATE __ SET __ WHERE __
        });

        if (punishment.equals(Punishment.BAN)) {
            sender.sendMessage(MessageTools.parseFromPath(config, "Player Unbanned", Template.template("player", playerName)));
        } else if (punishment.equals(Punishment.IP_BAN)) {
            sender.sendMessage(MessageTools.parseFromPath(config, "Player Un IP Banned", Template.template("player", playerName)));
        }else {
            sender.sendMessage(MessageTools.parseFromPath(config, "Player Unmuted", Template.template("player", playerName)));
        }

        //a switch to get all string values of punishments
        String punishmentString = switch(punishment) {
            case IP_BAN -> "Unbanned Ip";
            case BAN -> "Unbanned";
            case MUTE -> "Unmuted";
            case KICK -> "Kick Revoked";
            case WARN -> "Warn Revoked";
        };



        Main.getPlugin().getJda().getGuilds().forEach(guild -> {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle(punishmentString);
            embedBuilder.addField("Player", player.getName(), true);
            embedBuilder.addField("Revoked By", sender instanceof Player ? sender.getName() : "Console", true);
            embedBuilder.setColor(Color.CYAN);
            embedBuilder.setThumbnail("https://minotar.net/helm/" + player.getName() + "/64");
            try {
                Objects.requireNonNull(guild.getTextChannelById(921676351696687175L)).sendMessage(" ").setEmbeds(embedBuilder.build()).queue();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        });
    }
}
