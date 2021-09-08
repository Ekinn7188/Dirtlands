package net.dirtlands.listeners;

import net.dirtlands.Main;
import net.dirtlands.tabscoreboard.TabMenu;
import net.dirtlands.tools.MessageTools;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;

public class JoinAndLeave implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        //update tab menu
        TabMenu.updateTab();
        //update player display name (it doesn't save when the server restarts!)
        String nickname = Main.getPlugin().playerdata().get().getString(e.getPlayer().getUniqueId() + ".nickname");
        if (nickname != null){
            e.getPlayer().displayName(MessageTools.parseText(nickname));
        }
        if (!e.getPlayer().hasPlayedBefore()){
            long uniqueJoins = new File(Bukkit.getWorlds().get(0).getWorldFolder() + File.separator + "playerdata").length();

            e.joinMessage(MessageTools.parseFromPath("First Join Message", Template.of("Player", e.getPlayer().displayName()),
                    Template.of("Number", String.valueOf(uniqueJoins))));
        } else {
            e.joinMessage(MessageTools.parseFromPath("Join Message", Template.of("Player", e.getPlayer().displayName())));
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        e.quitMessage(MessageTools.parseFromPath("Leave Message", Template.of("Player", e.getPlayer().displayName())));
    }

}
