package net.dirtlands.listeners;

import dirtlands.db.Tables;
import io.papermc.paper.event.player.AsyncChatEvent;
import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.essentials.admin.MuteChat;
import net.dirtlands.database.DatabaseTools;
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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Chat implements Listener {

    DSLContext dslContext = Main.getPlugin().getDslContext();
    private static ConfigSetup config = Main.getPlugin().config();
    private static final ArrayList<Pattern> blockedPatterns = getBlockedRegex(Objects.requireNonNull(config.get().getStringList("Word Filter")));
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
        for (Pattern pattern : blockedPatterns){
            Matcher matcher = pattern.matcher(messageString);
            if (matcher.find()){
                e.setCancelled(true);
                String blockedMessage = matcher.group();
                if (matcher.group().charAt(0) == ' ') {
                    blockedMessage = blockedMessage.substring(1);
                }

                e.getPlayer().sendMessage(MessageTools.parseFromPath(config, "Chat Blocked", Template.template("word", blockedMessage)));
                return;
            }
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

    public static ArrayList<Pattern> getBlockedRegex(List<String> BlockedWords) {
        ArrayList<Pattern> patterns = new ArrayList<>();

        for (String word : BlockedWords) {
            StringBuilder patternBuilder = new StringBuilder("(^|\\s)");

            char[] blockedChar = word.toCharArray();
            boolean lastCharDouble = false;
            for (int i = 0; i < blockedChar.length; i++){
                String searchChar = switch (String.valueOf(blockedChar[i])) {
                    case "a" -> "(a|4)";
                    case "b" -> "(b|8)";
                    case "e" -> "(e|3)";
                    case "g" -> "(g|9)";
                    case "i" -> "(i|1)";
                    case "l" -> "(l|1)";
                    case "o" -> "(o|0)";
                    case "s" -> "(s|5)";
                    case "t" -> "(t|7)";
                    default -> String.valueOf(blockedChar[i]);
                };
                if (searchChar.equals(" ")) {
                    continue;
                }
                if (i != blockedChar.length-1 && blockedChar[i] == blockedChar[i+1]) {
                    patternBuilder.append("((").append(searchChar).append(")(").append(searchChar).append("|\\s|\\.|_|-)+?)");
                    lastCharDouble = true;
                    continue;
                }
                if (lastCharDouble) {
                    lastCharDouble = false;
                    continue;
                }
                if (i == blockedChar.length-1) {
                    patternBuilder.append("(").append(searchChar).append(")+?");
                    continue;
                }
                patternBuilder.append("(").append(searchChar).append(")+?").append("(\\s|\\.|_|-)*?");
            }
            patternBuilder.append("(\\s|$)");
            patterns.add(Pattern.compile(patternBuilder.toString(), Pattern.CASE_INSENSITIVE));
        }
        return patterns;
    }

    /**
     * return the first blocked word if detected, otherwise null
     */
    public static String isBlocked(String message) {
        for (Pattern pattern : blockedPatterns) {
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                String blockedMessage = matcher.group();
                if (matcher.group().charAt(0) == ' ') {
                    blockedMessage = blockedMessage.substring(1);
                }
                return blockedMessage;
            }
        }
        return null;
    }

}
