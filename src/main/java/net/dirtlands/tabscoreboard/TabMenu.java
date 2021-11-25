package net.dirtlands.tabscoreboard;

import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TabMenu {
    static ConfigSetup config = Main.getPlugin().config();

    public static void updateTabLoop() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), TabMenu::updateTab, 0L, 600L);
    }

    public static void updateTab(){
        for (Player player : Bukkit.getOnlinePlayers()){
            Component header = generateHeaderAndFooter("Header");
            Component footer = generateHeaderAndFooter("Footer");

            player.sendPlayerListHeaderAndFooter(header, footer);

            User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());
            assert user != null;
            String prefix = user.getCachedData().getMetaData().getPrefix();

            //add a space after the prefix if there isn't one already
            if (prefix != null && prefix.toCharArray()[prefix.length()-1] != ' ') {
                prefix += " ";
            }

            String nameAsPlainText = PlainTextComponentSerializer.plainText().serialize(player.displayName());

            if (Component.text(nameAsPlainText).equals(player.displayName())){
                player.playerListName(MessageTools.parseFromPath(config, "Player Tab Style",
                        Template.template("prefix", prefix == null ? Component.empty() : MessageTools.parseText(prefix)),
                        Template.template("player", nameAsPlainText)));
            } else {
                player.playerListName(MessageTools.parseFromPath(config, "Player Tab Style",
                        Template.template("prefix", prefix == null ? Component.empty() : MessageTools.parseText(prefix)),
                        Template.template("player", player.displayName())));
            }

            //add prefix if one exists, then append the player name with some spaces for the network bars

        }
    }

    private static Component generateHeaderAndFooter(String headerOrFooter){
        ConfigurationSection configurationSection = Main.getPlugin().config().get().getConfigurationSection("Tablist " + headerOrFooter);

        if (configurationSection == null) {
            return Component.empty();
        }

        List<String> lines = configurationSection.getKeys(false).stream().toList();

        List<String> text = new ArrayList<>();

        lines.forEach(s -> text.add(MessageTools.getString(config, "Tablist " + headerOrFooter + "." + s)));

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.size(); i++) {
            builder.append(text.get(i));
            if (i != text.size() - 1) {
                builder.append("\n");
            }
        }

        return MessageTools.parseText(builder.toString(),
                Template.template("onlineplayers", String.valueOf(Bukkit.getOnlinePlayers().size())),
                Template.template("maxplayers", String.valueOf(Bukkit.getServer().getMaxPlayers())));
    }
}
