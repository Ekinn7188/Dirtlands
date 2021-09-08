package net.dirtlands.tabscoreboard;

import net.dirtlands.Main;
import net.dirtlands.tools.MessageTools;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class TabMenu {

    public static void updateTabLoop() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), TabMenu::updateTab, 0L, 600L);
    }

    public static void updateTab(){
        for (Player player : Bukkit.getOnlinePlayers()){
            player.sendPlayerListHeaderAndFooter(generateHeaderAndFooter("Header"), generateHeaderAndFooter("Footer"));

            User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());
            assert user != null;
            String prefix = user.getCachedData().getMetaData().getPrefix();

            //add a space after the prefix if there isn't one already
            if (prefix != null && prefix.toCharArray()[prefix.length()-1] != ' ') {
                prefix += " ";
            }

            String nameAsPlainText = PlainTextComponentSerializer.plainText().serialize(player.displayName());

            if (Component.text(nameAsPlainText).equals(player.displayName())){
                player.playerListName((prefix == null ? Component.empty() : MessageTools.parseText(prefix))
                        .append(MessageTools.parseText("<#893900>" + nameAsPlainText)).append(Component.text("  ")));
            } else {
                player.playerListName((prefix == null ? Component.empty() : MessageTools.parseText(prefix))
                        .append(player.displayName()).append(Component.text("  ")));
            }

            //add prefix if one exists, then append the player name with some spaces for the network bars

        }
    }

    private static Component generateHeaderAndFooter(String headerOrFooter){
        @SuppressWarnings("unchecked")
        ArrayList<String> text = (ArrayList<String>) Main.getPlugin().config().get().getList("Tablist " + headerOrFooter);
        Component withNewLine = Component.empty();
        if (text != null){
            StringBuilder builder = new StringBuilder();
            for (String line : text) {
                builder.append(line).append("\n");
            }
            return MessageTools.parseText(builder.toString(), Template.of("OnlinePlayers", String.valueOf(Bukkit.getOnlinePlayers().size())));
        }
        return Component.empty();
    }
}
