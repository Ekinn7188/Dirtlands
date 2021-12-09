package net.dirtlands.commands.essentials.warp;

import dirtlands.db.Tables;
import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.database.DatabaseTools;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.entity.Player;
import org.jooq.DSLContext;

public class HomeList extends PluginCommand {
    private static ConfigSetup config = Main.getPlugin().config();
    DSLContext dslContext = Main.getPlugin().getDslContext();
    @Override
    public String getName() {
        return "homes";
    }

    @Override
    public void execute(Player player, String[] args) {

        int userID = DatabaseTools.getUserID(player.getUniqueId());
        var homes = dslContext.select(Tables.HOMES.HOMENAME).from(Tables.HOMES)
                .where(Tables.HOMES.USERID.eq(userID)).fetch().getValues(Tables.HOMES.HOMENAME);

        if (homes.size() > 0){
            String commaSeperatedHomes = String.join(", ", homes);
            player.sendMessage(MessageTools.parseFromPath(config, "Home List", Template.template("homes", commaSeperatedHomes)));
        } else {
            player.sendMessage(MessageTools.parseFromPath(config, "No Homes"));
        }
    }

}
