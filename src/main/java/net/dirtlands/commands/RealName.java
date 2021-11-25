package net.dirtlands.commands;

import dirtlands.db.Tables;
import jeeper.utils.MessageTools;
import net.dirtlands.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.*;

public class RealName extends ChatColor {

    @Override
    public String getName() {
        return "realname";
    }

    @Override
    public boolean isRequiresPlayer() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        //nickname without any special symbols like <green> or &b
        String nickname = PlainTextComponentSerializer.plainText().serialize(MessageTools.parseText(String.join(" ", args)));

        Map<String, String> results = Main.getPlugin().getDslContext().select(Tables.USERS.USERNICKNAME, Tables.USERS.USERUUID)
                .from(Tables.USERS).fetch().intoMap(Tables.USERS.USERUUID, Tables.USERS.USERNICKNAME);

        System.out.println(results);

        if (results.size() == 0) {
            sender.sendMessage(MessageTools.parseText("&bThere are no players with the nickname <nickname>",
                    Template.template("nickname", nickname)));
            return;
        }


        results.entrySet().iterator().forEachRemaining((map) -> {
            String value = map.getValue();
            String key = map.getKey();

            if (value == null || !PlainTextComponentSerializer.plainText().serialize(MessageTools.parseText(value)).equals(nickname)) {
                results.remove(key);
            }
        });

/*        results.forEach((key, value) -> {
            if (value == null || !PlainTextComponentSerializer.plainText().serialize(MessageTools.parseText(value)).equals(nickname)) {
                results.remove(key);
            }
        });*/

        List<Component> names = new ArrayList<>();
        List<String> uuids = results.keySet().stream().toList();

        System.out.println(uuids.get(0));

        uuids.forEach(uuid -> names.add(Objects.requireNonNull(Bukkit.getPlayer(UUID.fromString(uuid))).name()));

        if (results.size() > 1) {
            Component seperatedNames = Component.empty();
            for (int i = 0; i < names.size(); i++) {
                seperatedNames = seperatedNames.append(names.get(i));
                if (i != names.size()-1) {
                    seperatedNames = seperatedNames.append(Component.text(", "));
                }
            }

            sender.sendMessage(MessageTools.parseText("&bPlayers with the nickname <nickname>: &3<players>",
                    Template.template("nickname", nickname),
                    Template.template("players", seperatedNames)));
            return;
        }

        sender.sendMessage(MessageTools.parseText("&bPlayer with the nickname <nickname>: &3<player>",
                Template.template("nickname", nickname),
                Template.template("player", names.get(0))));




    }
}
