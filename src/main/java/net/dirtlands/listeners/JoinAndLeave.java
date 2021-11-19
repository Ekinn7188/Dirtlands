package net.dirtlands.listeners;

import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.tabscoreboard.TabMenu;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;

public class JoinAndLeave implements Listener {

    private static ConfigSetup config = Main.getPlugin().config();

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        //update player display name (it doesn't save when the server restarts!)
        String nickname = Main.getPlugin().playerdata().get().getString(e.getPlayer().getUniqueId() + ".nickname");
        if (nickname != null){
            e.getPlayer().displayName(MessageTools.parseText(nickname));
        }
        //update tab menu
        TabMenu.updateTab();
        if (!e.getPlayer().hasPlayedBefore()){
            long uniqueJoins = new File(Bukkit.getWorlds().get(0).getWorldFolder() + File.separator + "playerdata").length();

            e.joinMessage(MessageTools.parseFromPath(config, "First Join Message", Template.template("Player", e.getPlayer().displayName()),
                    Template.template("Number", String.valueOf(uniqueJoins))));
        } else {
            e.joinMessage(MessageTools.parseFromPath(config, "Join Message", Template.template("Player", e.getPlayer().displayName())));
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        e.quitMessage(MessageTools.parseFromPath(config,"Leave Message", Template.template("Player", e.getPlayer().displayName())));
    }

}
