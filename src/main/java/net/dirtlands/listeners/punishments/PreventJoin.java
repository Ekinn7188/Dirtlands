package net.dirtlands.listeners.punishments;

import dirtlands.db.Tables;
import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.database.DatabaseTools;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.jooq.DSLContext;
import org.jooq.Record1;

import java.time.LocalDateTime;

public class PreventJoin implements Listener {

    static DSLContext dslContext = Main.getPlugin().getDslContext();
    static ConfigSetup config = Main.getPlugin().config();

    @EventHandler
    public void onBanJoin(AsyncPlayerPreLoginEvent e) {

        int userId = DatabaseTools.getUserID(e.getUniqueId());

        //Check if the user is banned
        String punishmentType = DatabaseTools.firstString(dslContext.select(Tables.PUNISHMENTS.PUNISHMENTTYPE).from(Tables.PUNISHMENTS)
                .where(Tables.PUNISHMENTS.USERID.eq(userId)
                .and(Tables.PUNISHMENTS.PUNISHMENTTYPE.equalIgnoreCase(Punishment.BAN.getPunishment()))).fetchAny());

        if (punishmentType == null) {
            return; //not banned
        }

        //get if the time the ban ended. if null, permanent
        var banEndRecord = dslContext.select(Tables.PUNISHMENTS.PUNISHMENTEND).from(Tables.PUNISHMENTS)
                .where(Tables.PUNISHMENTS.USERID.eq(userId))
                .orderBy(Tables.PUNISHMENTS.PUNISHMENTEND.desc().nullsFirst()).limit(1).fetch();


        for (Record1<LocalDateTime> banEnd : banEndRecord) {
            if (banEnd == null) {
                continue;
            }

            //get the player's ban reason
            String reason = DatabaseTools.firstString(dslContext.select(Tables.PUNISHMENTS.PUNISHMENTREASON)
                    .from(Tables.PUNISHMENTS).where(Tables.PUNISHMENTS.USERID.eq(userId)
                            .and(Tables.PUNISHMENTS.PUNISHMENTTYPE.equalIgnoreCase(Punishment.BAN.getPunishment()))).fetchAny());

            LocalDateTime banEndTime = banEnd.value1();

            //perm banned
            if(banEndTime == null) {
                if (reason == null) {
                    e.kickMessage(permBanNoReasonMessage());
                } else {
                    e.kickMessage(permBanMessage(reason));
                }
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, e.kickMessage());
                return;

            }

            if (reason == null) { //no reason, use other message
                e.kickMessage(tempBanNoReasonMessage(banEndTime));
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, e.kickMessage());
                return;
            }

            //if the ban time is passed, let the player join
            if (banEndTime.isBefore(LocalDateTime.now())) {
                return;
            }


            //send message with reason
            e.kickMessage(tempBanReasonMessage(banEndTime, reason));
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, e.kickMessage());
            return;
        }
    }

    /**
     * @return the kick message for a permanent ban, with a reason
     */
    public static Component permBanNoReasonMessage() {
        String discordLink = MessageTools.getString(config, "Discord Link");
        return MessageTools.parseFromPath(config, "Punishment Header").append(Component.newline())
                .append(MessageTools.parseFromPath(config, "Perm Ban Message", Template.template("discord",
                        Component.text(discordLink).clickEvent(ClickEvent.openUrl(discordLink)))));
    }

    /**
     * @return the kick message for a permanent ban
     */
    public static Component permBanMessage(String reason) {
        String discordLink = MessageTools.getString(config, "Discord Link");
        return MessageTools.parseFromPath(config, "Punishment Header").append(Component.newline())
                .append(MessageTools.parseFromPath(config, "Perm Ban With Reason", Template.template("discord",
                        Component.text(discordLink).clickEvent(ClickEvent.openUrl(discordLink))),
                        Template.template("reason", MessageTools.parseText(reason))));
    }

    /**
     * @return the kick message for a temp ban without a reason
     */
    public static Component tempBanNoReasonMessage(LocalDateTime banEnd) {
        String discordLink = MessageTools.getString(config, "Discord Link");

        return MessageTools.parseFromPath(config, "Punishment Header").append(Component.newline())
                .append(MessageTools.parseFromPath(config, "Ban Message", Template.template("time", PunishmentTools.getPunishmentEndString(banEnd)),
                        Template.template("discord", Component.text(discordLink).clickEvent(ClickEvent.openUrl(discordLink)))));
    }

    /**
     * @return the kick message for a temp ban with a reason
     */
    public static Component tempBanReasonMessage(LocalDateTime banEnd, String reason) {
        String discordLink = MessageTools.getString(config, "Discord Link");

        return MessageTools.parseFromPath(config, "Punishment Header").append(Component.newline())
                .append(MessageTools.parseFromPath(config, "Ban With Reason", Template.template("reason", reason),
                        Template.template("time", PunishmentTools.getPunishmentEndString(banEnd)),
                        Template.template("discord", Component.text(discordLink).clickEvent(ClickEvent.openUrl(discordLink)))));
    }




}
