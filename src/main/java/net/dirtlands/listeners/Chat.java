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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Chat implements Listener {

    DSLContext dslContext = Main.getPlugin().getDslContext();
    private static ConfigSetup config = Main.getPlugin().config();
    private static final ArrayList<Pattern> blockedPatterns =
            getBlockedRegex(Objects.requireNonNull(config.get().getStringList("Word Filter")));

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

}
