package net.dirtlands.commands.economy;

import dirtlands.db.Tables;
import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.database.DatabaseTools;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jooq.DSLContext;

import java.util.Objects;
import java.util.UUID;

public class Baltop extends PluginCommand {
    DSLContext dslContext = Main.getPlugin().getDslContext();
    final ConfigSetup config = Main.getPlugin().config();

    @Override
    public String getName() {
        return "baltop";
    }

    @Override
    public boolean isRequiresPlayer() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        int offset = 0;
        int page = 1;

        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
                offset = (page-1)*10;
            } catch (NumberFormatException e) {
                sender.sendMessage(MessageTools.parseText("&cCorrect Usage: /baltop {page}"));
                return;
            }
        }

        var map = dslContext.selectFrom(Tables.ECONOMY).orderBy(Tables.ECONOMY.BALANCE.desc())
                .limit(10).offset(offset)
                .fetch().intoMap(Tables.ECONOMY.USERID, Tables.ECONOMY.BALANCE);

        int pages = (int)Math.ceil(dslContext.fetchCount(Tables.ECONOMY) / 10.0);

        sender.sendMessage(MessageTools.parseFromPath(config, "Baltop Header", Template.template("page", String.valueOf(page)),
                Template.template("pages", String.valueOf(pages))));

        var set = map.entrySet().iterator();

        System.out.println(map);

        int position = (page*10)-9;

        System.out.println(position);
        System.out.println(offset);
        System.out.println(page);
        while (set.hasNext()){
            var entry = set.next();
            int key = entry.getKey();
            int value = entry.getValue();

            OfflinePlayer player;
            try {
                player = Bukkit.getOfflinePlayer(UUID.fromString(Objects.requireNonNull(
                        DatabaseTools.firstString(dslContext.select(Tables.USERS.USERUUID).from(Tables.USERS)
                                .where(Tables.USERS.USERID.eq(key)).fetchAny()))));
            } catch (NullPointerException e) {
                continue;
            }
            if (player.getName()==null) {
                continue;
            }

            sender.sendMessage(MessageTools.parseFromPath(config, "Baltop Names",
                    Template.template("position", String.valueOf(position)), Template.template("name", player.getName()),
                    Template.template("balance", String.format("%,d", value))));

            position++;
        }
    }
}
