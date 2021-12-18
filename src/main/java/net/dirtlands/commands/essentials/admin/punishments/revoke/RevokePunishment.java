package net.dirtlands.commands.essentials.admin.punishments.revoke;

import dirtlands.db.Tables;
import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.database.DatabaseTools;
import net.dirtlands.listeners.punishments.Punishment;
import net.dirtlands.log.LogColor;
import net.dirtlands.tools.UUIDTools;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

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
     * @return true if playerName is a real player, false if it's not
     */
    public static boolean revoke(Punishment punishment, CommandSender sender, String playerName) {
        String uuid = UUIDTools.getUuid(playerName);

        if (uuid == null) {
            sender.sendMessage(MessageTools.parseFromPath(config, "Player Doesnt Exist", Template.template("player", playerName)));
            return false;
        }

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
            return true;
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
        return true;
    }
}
