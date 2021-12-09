package net.dirtlands.commands.essentials;

import dirtlands.db.Tables;
import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jooq.DSLContext;

public class ChatColor extends PluginCommand {
    DSLContext dslContext = Main.getPlugin().getDslContext();
    private static ConfigSetup config = Main.getPlugin().config();

    @Override
    public String getName() {
        return "chatcolor";
    }

    @Override
    protected Permission getPermissionType() {
        return Permission.CHATCOLOR;
    }

    @Override
    public void execute(Player player, String[] args) {

        if (args.length > 0) {

            if (!MessageTools.parseText(args[0]).equals(Component.text(args[0]))) {
                dslContext.update(Tables.USERS).set(Tables.USERS.CHATCOLOR, args[0]).execute();
                player.sendMessage(MessageTools.parseFromPath(config, "Chat Color Set"));

            } else {
                player.sendMessage(MessageTools.parseFromPath(config, "Invalid Chat Color"));
            }
            return;
        }

        player.sendMessage(MessageTools.parseText("&cCorrect Usage: /chatcolor <color code>"));

    }
}
