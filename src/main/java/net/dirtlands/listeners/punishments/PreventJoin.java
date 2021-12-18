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
import org.jooq.impl.DSL;

import java.time.LocalDateTime;
import java.util.Objects;

public class PreventJoin implements Listener {

    static DSLContext dslContext = Main.getPlugin().getDslContext();
    static ConfigSetup config = Main.getPlugin().config();

    @EventHandler
    public void onBanJoin(AsyncPlayerPreLoginEvent e) {

        int userId = DatabaseTools.getUserID(e.getUniqueId());

        var ipBanRecord =
                dslContext.select(Tables.PUNISHMENTS.IPADDRESS, Tables.PUNISHMENTS.PUNISHMENTREASON).from(Tables.PUNISHMENTS)
                        .where(DSL.condition(dslContext.select(Tables.PUNISHMENTS.PUNISHMENTEND)
                                        .from(Tables.PUNISHMENTS)
                                        .where(Tables.PUNISHMENTS.PUNISHMENTTYPE.eq(Punishment.IP_BAN.getPunishment()))
                                        .fetch(Tables.PUNISHMENTS.PUNISHMENTEND)
                                        .stream().filter(Objects::nonNull).anyMatch(time -> time.isAfter(LocalDateTime.now())))
                                .or(Tables.PUNISHMENTS.PUNISHMENTEND.isNull())
                                .and(Tables.PUNISHMENTS.PUNISHMENTTYPE.equalIgnoreCase(Punishment.IP_BAN.getPunishment())))
                        .orderBy(Tables.PUNISHMENTS.PUNISHMENTEND.desc().nullsFirst()).limit(1).fetchOne();

        if (ipBanRecord != null) {

            var address = e.getAddress().getHostAddress();

            if (address.equals(ipBanRecord.get(Tables.PUNISHMENTS.IPADDRESS))) {
                String reason = ipBanRecord.get(Tables.PUNISHMENTS.PUNISHMENTREASON);
                if (reason == null) {
                    e.kickMessage(permBanNoReasonMessage());
                    e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, e.kickMessage());
                } else {
                    e.kickMessage(permBanMessage(reason));
                    e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, e.kickMessage());
                }

            }

            return;
        }



        //get if the time the ban ended. if null, permanent
        var banEndRecord = dslContext.select(Tables.PUNISHMENTS.PUNISHMENTEND, Tables.PUNISHMENTS.PUNISHMENTREASON)
                .from(Tables.PUNISHMENTS)
                .where(DSL.condition(dslContext.select(Tables.PUNISHMENTS.PUNISHMENTEND)
                            .from(Tables.PUNISHMENTS)
                            .where(Tables.PUNISHMENTS.PUNISHMENTTYPE.equalIgnoreCase(Punishment.BAN.getPunishment())
                                    .and(Tables.PUNISHMENTS.USERID.eq(DatabaseTools.getUserID(e.getUniqueId()))))
                            .fetch(Tables.PUNISHMENTS.PUNISHMENTEND)
                            .stream().filter(Objects::nonNull).anyMatch(time -> time.isAfter(LocalDateTime.now()))))
                        .or(Tables.PUNISHMENTS.PUNISHMENTEND.isNull())
                        .and(Tables.PUNISHMENTS.PUNISHMENTTYPE.equalIgnoreCase(Punishment.BAN.getPunishment()))
                        .and(Tables.PUNISHMENTS.USERID.eq(DatabaseTools.getUserID(e.getUniqueId())))
                .orderBy(Tables.PUNISHMENTS.PUNISHMENTEND.desc().nullsFirst()).limit(1).fetchOne();


        if (banEndRecord == null) {
            return;
        }
        //perm banned
        if(banEndRecord.value1() == null) {
            if (banEndRecord.value2() == null) {
                e.kickMessage(permBanNoReasonMessage());
            } else {
                e.kickMessage(permBanMessage(banEndRecord.value2()));
            }
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, e.kickMessage());
            return;

        }

        if (banEndRecord.value2() == null) { //no reason, use other message
            e.kickMessage(tempBanNoReasonMessage(banEndRecord.value1()));
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, e.kickMessage());
            return;
        }

        //if the ban time is passed, let the player join
        if (banEndRecord.value1().isBefore(LocalDateTime.now())) {
            return;
        }


        //send message with reason
        e.kickMessage(tempBanReasonMessage(banEndRecord.value1(), banEndRecord.value2()));
        e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, e.kickMessage());
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
