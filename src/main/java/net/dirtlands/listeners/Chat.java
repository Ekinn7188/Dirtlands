package net.dirtlands.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.admin.MuteChat;
import net.dirtlands.tools.ConfigTools;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Chat implements Listener {

    @EventHandler
    public void onChat(AsyncChatEvent e){
        e.setCancelled(true);

        if (MuteChat.chatMuted){
            if (!e.getPlayer().hasPermission(Permission.BYPASSCHAT.getName())){
                Bukkit.broadcast(ConfigTools.parseFromPath("Chat Is Muted"));
                return;
            }
        }

        String messageStyle = ConfigTools.getString("Chat Style");
        Player player = e.getPlayer();
        User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());


        assert user != null;
        String prefix = user.getCachedData().getMetaData().getPrefix();
        String suffix = user.getCachedData().getMetaData().getSuffix();

        Component message = e.message();
        if (player.hasPermission("dirtlands.chat.color")){
            message = ConfigTools.parseText(PlainTextComponentSerializer.plainText().serialize(e.message()));
        }

        Component replacedText = ConfigTools.parseFromPath("Chat Style", Template.of("Prefix", ConfigTools.parseText(prefix == null ? "" : prefix)),
                Template.of("Player", e.getPlayer().displayName()), Template.of("Suffix", ConfigTools.parseText(suffix == null ? "" : suffix)),
                Template.of("Message", message));

        Bukkit.broadcast(replacedText);
    }

}
