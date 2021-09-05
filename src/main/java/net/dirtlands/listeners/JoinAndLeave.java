package net.dirtlands.listeners;

import net.dirtlands.Main;
import net.dirtlands.tools.ConfigTools;
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
        if (!e.getPlayer().hasPlayedBefore()){
            long uniqueJoins = new File(Bukkit.getWorlds().get(0).getWorldFolder() + File.separator + "playerdata").length();

            e.joinMessage(ConfigTools.parseFromPath("First Join Message", Template.of("Player", e.getPlayer().displayName()),
                    Template.of("Number", String.valueOf(uniqueJoins))));
        } else {
            e.joinMessage(ConfigTools.parseFromPath("Join Message", Template.of("Player", e.getPlayer().displayName())));
        }

        Main main = Main.getPlugin();
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        e.quitMessage(ConfigTools.parseFromPath("Leave Message", Template.of("Player", e.getPlayer().displayName())));

        Main main = Main.getPlugin();
    }

}
