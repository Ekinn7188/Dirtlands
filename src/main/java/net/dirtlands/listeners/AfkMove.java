package net.dirtlands.listeners;

import dirtlands.db.Tables;
import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.AFK;
import net.dirtlands.database.DatabaseTools;
import net.dirtlands.tabscoreboard.TabMenu;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Objects;

public class AfkMove implements Listener {

    ConfigSetup config = Main.getPlugin().config();

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        if (AFK.isAFK.containsKey(e.getPlayer().getUniqueId()) && AFK.isAFK.get(player.getUniqueId())) {
            String nickname = Main.getPlugin().getDslContext().select(Tables.USERS.USERNICKNAME)
                    .from(Tables.USERS).where(Tables.USERS.USERID.eq(DatabaseTools.getUserID(player.getUniqueId())))
                    .fetch().getValue(0, Tables.USERS.USERNICKNAME);
            player.displayName(MessageTools.parseText(Objects.requireNonNullElseGet(nickname, player::getName)));

            Bukkit.broadcast(MessageTools.parseFromPath(config, "Player Not AFK", Template.template("player", player.displayName())));
            AFK.isAFK.put(player.getUniqueId(), false);

            TabMenu.updateTab();
        }
    }
}
