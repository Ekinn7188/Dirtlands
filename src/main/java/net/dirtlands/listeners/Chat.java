package net.dirtlands.listeners;

import dirtlands.db.Tables;
import io.papermc.paper.event.player.AsyncChatEvent;
import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.essentials.admin.MuteChat;
import net.dirtlands.database.DatabaseTools;
import net.dirtlands.listeners.filters.Filter;
import net.dirtlands.listeners.punishments.PunishmentTools;
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

import java.util.HashMap;
import java.util.UUID;

public class Chat implements Listener {

    DSLContext dslContext = Main.getPlugin().getDslContext();
    private static ConfigSetup config = Main.getPlugin().config();

    private static HashMap<UUID, HashMap<String, Long>> cooldown = new HashMap<>();

    @EventHandler
    public void onChat(AsyncChatEvent e){
        e.setCancelled(true);
        Player player = e.getPlayer();

        //muted chat
        if (MuteChat.chatMuted){
            if (!e.getPlayer().hasPermission(Permission.BYPASSCHAT.getName())){
                Bukkit.broadcast(MessageTools.parseFromPath(config, "Chat Is Muted"));
                return;
            }
        }

        if (PunishmentTools.checkMuted(player)){
            return;
        }


        //chat cooldown
        String messageString = PlainTextComponentSerializer.plainText().serialize(e.message());

        if (!player.hasPermission(Permission.COOLDOWN.getName())) {
            if (cooldown.containsKey(player.getUniqueId()) && cooldown.get(player.getUniqueId()).containsKey(messageString)) {
                long secondsLeft = ((cooldown.get(player.getUniqueId()).get(messageString) + 2500) - System.currentTimeMillis());
                if (secondsLeft > 0) {
                    player.sendMessage(MessageTools.parseFromPath(config, "Sending Messages Too Fast", Template.template("message", messageString)));
                    return;
                }
            }
            HashMap<String, Long> newCooldown = new HashMap<>();
            newCooldown.put(messageString, System.currentTimeMillis());
            cooldown.put(player.getUniqueId(), newCooldown);
        }


        //check for blocked words
        if (Filter.blockCheck(e.getPlayer(), messageString)) {
            e.setCancelled(true);
            return;
        }

        //if the player has permission to use a chat color and has one set, use it
        Component message = MessageTools.parseText(messageString);
        String chatcolor = DatabaseTools.firstString(dslContext.select(Tables.USERS.CHATCOLOR)
                .from(Tables.USERS).where(Tables.USERS.USERID.eq(DatabaseTools.getUserID(e.getPlayer().getUniqueId()))).fetchAny());
        if (player.hasPermission("dirtlands.chat.color") && chatcolor != null ){
            message = MessageTools.parseText(chatcolor + messageString);
        }

        //get prefix and suffix
        User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());
        assert user != null;
        String prefix = user.getCachedData().getMetaData().getPrefix();
        String suffix = user.getCachedData().getMetaData().getSuffix();

        //compile everything into a message to send
        Component replacedText = MessageTools.parseFromPath(config, "Chat Style", Template.template("prefix", MessageTools.parseText(prefix == null ? "" : prefix + " ")),
                Template.template("player", e.getPlayer().displayName()), Template.template("suffix", MessageTools.parseText(suffix == null ? "" : " " + suffix)),
                Template.template("message", message));


        //get any urls and make them clickable
        for (String url : MessageTools.fetchURLs(messageString)) {
            replacedText = replacedText.clickEvent(ClickEvent.openUrl(url));
        }

        //send the message
        Bukkit.broadcast(replacedText);
    }




}
