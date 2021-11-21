package net.dirtlands.listeners;

import dirtlands.db.Tables;
import io.papermc.paper.event.player.AsyncChatEvent;
import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.admin.MuteChat;
import net.dirtlands.database.DatabaseTools;
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
import org.jooq.DSLContext;

public class Chat implements Listener {

    DSLContext dslContext = Main.getPlugin().getDslContext();
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


        String chatcolor = DatabaseTools.firstString(dslContext.select(Tables.USERS.CHATCOLOR).from(Tables.USERS).where(Tables.USERS.USERID.eq(DatabaseTools.getUserID(e.getPlayer().getUniqueId()))).fetchAny());
        String messageStyle = MessageTools.getString(config, "Chat Style");
        Player player = e.getPlayer();
        User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());


        assert user != null;
        String prefix = user.getCachedData().getMetaData().getPrefix();
        String suffix = user.getCachedData().getMetaData().getSuffix();

        String messageString = PlainTextComponentSerializer.plainText().serialize(e.message());
        Component message = MessageTools.parseText(messageString);
        if (player.hasPermission("dirtlands.chat.color") && chatcolor != null ){
            message = MessageTools.parseText(chatcolor + messageString);
        }

        Component replacedText = MessageTools.parseFromPath(config, "Chat Style", Template.template("prefix", MessageTools.parseText(prefix == null ? "" : prefix + " ")),
                Template.template("player", e.getPlayer().displayName()), Template.template("suffix", MessageTools.parseText(suffix == null ? "" : " " + suffix)),
                Template.template("message", message));

        for (String url : MessageTools.fetchURLs(messageString)) {
            replacedText = replacedText.clickEvent(ClickEvent.openUrl(url));
        }

        Bukkit.broadcast(replacedText);
    }

}
