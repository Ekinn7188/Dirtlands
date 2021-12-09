package net.dirtlands.commands.essentials;

import dirtlands.db.Tables;
import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.database.DatabaseTools;
import net.dirtlands.tabscoreboard.TabMenu;
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.jooq.DSLContext;

import javax.annotation.Nullable;
import java.util.UUID;

public class Nickname extends PluginCommand {
    private static ConfigSetup config = Main.getPlugin().config();
    static DSLContext dslContext = Main.getPlugin().getDslContext();

    @Override
    public String getName() {
        return "nickname";
    }

    @Override
    protected Permission getPermissionType() {
        return Permission.NICKNAME;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length > 0){
            if (args[0].equals("reset")){
                player.displayName(MessageTools.parseText(player.getName()));
                changeNickname(null, player.getUniqueId());
                player.sendMessage(MessageTools.parseFromPath(config, "Nickname Change", Template.template("name", player.getName())));
                return;
            }
            if (PlainTextComponentSerializer.plainText().serialize(MessageTools.parseText(args[0])).equals("")) {
                player.sendMessage(MessageTools.parseFromPath(config, "Invalid Nickname"));
                return;
            }

            String nickname = String.join(" ", args);
            player.displayName(MessageTools.parseText(nickname));
            changeNickname(nickname, player.getUniqueId());
            player.sendMessage(MessageTools.parseFromPath(config,"Nickname Change", Template.template("name", MessageTools.parseText(nickname))));
        }
    }

    private void changeNickname(@Nullable String name, UUID uuid) {
        int id = DatabaseTools.getUserID(uuid);
        if (id != -1) {
            dslContext.update(Tables.USERS).set(Tables.USERS.USERNICKNAME, name).where(Tables.USERS.USERID.eq(id)).execute();
        } else {
            DatabaseTools.addUser(uuid);
            dslContext.update(Tables.USERS).set(Tables.USERS.USERNICKNAME, name).where(Tables.USERS.USERID.eq(id)).execute();
        }
        TabMenu.updateTab();
    }
}
