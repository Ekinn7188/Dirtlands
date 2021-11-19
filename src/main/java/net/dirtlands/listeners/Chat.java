package net.dirtlands.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.admin.MuteChat;
import net.dirtlands.files.Playerdata;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Chat implements Listener {

    Playerdata playerdata = Main.getPlugin().playerdata();
    private static ConfigSetup config = Main.getPlugin().config();

    @EventHandler
    public void onChat(AsyncChatEvent e){
        e.setCancelled(true);

        if (MuteChat.chatMuted){
            if (!e.getPlayer().hasPermission(Permission.BYPASSCHAT.getName())){
                Bukkit.broadcast(MessageTools.parseFromPath(config, "Chat Is Muted"));
                return;
            }
        }


        String chatcolor = playerdata.get().getString(e.getPlayer().getUniqueId() + ".chatcolor");
        String messageStyle = MessageTools.getString(config, "Chat Style");
        Player player = e.getPlayer();
        User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());


        assert user != null;
        String prefix = user.getCachedData().getMetaData().getPrefix();
        String suffix = user.getCachedData().getMetaData().getSuffix();

        String messageString = PlainTextComponentSerializer.plainText().serialize(e.message());
        Component message = MessageTools.parseText(messageString);
        if (player.hasPermission("dirtlands.chat.color")){
            if (chatcolor == null){
                message = MessageTools.parseText(messageString);
            }
            else {
                message = MessageTools.parseText(chatcolor + messageString);
            }

        }

        Component replacedText = MessageTools.parseFromPath(config, "Chat Style", Template.template("Prefix", MessageTools.parseText(prefix == null ? "" : prefix + " ")),
                Template.template("Player", e.getPlayer().displayName()), Template.template("Suffix", MessageTools.parseText(suffix == null ? "" : " " + suffix)),
                Template.template("Message", message));

        for (String url : MessageTools.fetchURLs(messageString)) {
            replacedText = replacedText.clickEvent(ClickEvent.openUrl(url));
        }


        Bukkit.broadcast(replacedText);
    }

}
