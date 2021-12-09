package net.dirtlands.commands.essentials.admin.punishments.revoke;

import dirtlands.db.Tables;
import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.database.DatabaseTools;
import net.dirtlands.listeners.punishments.Punishment;
import net.dirtlands.tools.UUIDTools;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.command.CommandSender;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class RevokePunishment {

    DSLContext dslContext = Main.getPlugin().getDslContext();
    ConfigSetup config = Main.getPlugin().config();

    public RevokePunishment(Punishment punishment, CommandSender sender, String[] args) {
        String playerName = args[0];
        String uuid = UUIDTools.getUuid(playerName);

        if (uuid == null) {
            sender.sendMessage(MessageTools.parseFromPath(config, "Player Doesnt Exist", Template.template("player", playerName)));
            return;
        }

        var punishRecord = dslContext.selectFrom(Tables.PUNISHMENTS)
                .where(Tables.PUNISHMENTS.PUNISHMENTTYPE.equalIgnoreCase(punishment.getPunishment()).and(Tables.PUNISHMENTS.USERID.eq(DatabaseTools.getUserID(uuid)))).fetch();

        if (punishRecord.size() == 0) {
            if (punishment.equals(Punishment.BAN)) {
                sender.sendMessage(MessageTools.parseFromPath(config, "Unban Not Banned", Template.template("player", playerName)));
            } else if (punishment.equals(Punishment.MUTE)) {
                sender.sendMessage(MessageTools.parseFromPath(config, "Unmute Not Muted", Template.template("player", playerName)));
            }
            return;
        }



        var currentPunishments = punishRecord.stream()
                .filter(p -> p.get(Tables.PUNISHMENTS.PUNISHMENTEND) == null ||
                        p.get(Tables.PUNISHMENTS.PUNISHMENTEND).isAfter(LocalDateTime.now())).collect(Collectors.toList());

        currentPunishments.forEach(p -> {
            p.setPunishmentend(LocalDateTime.now());
            dslContext.executeUpdate(p, DSL.trueCondition());
        });

        if (punishment.equals(Punishment.BAN)) {
            sender.sendMessage(MessageTools.parseFromPath(config, "Player Unbanned", Template.template("player", playerName)));
        } else {
            sender.sendMessage(MessageTools.parseFromPath(config, "Player Unmuted", Template.template("player", playerName)));
        }

        System.out.println(dslContext.selectFrom(Tables.PUNISHMENTS).fetch());
    }
}
