package net.dirtlands.commands.essentials.warp;

import dirtlands.db.Tables;
import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.database.DatabaseTools;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jooq.DSLContext;

public class DeleteHome extends PluginCommand {

    private static ConfigSetup config = Main.getPlugin().config();
    DSLContext dslContext = Main.getPlugin().getDslContext();

    @Override
    public String getName() {
        return "delhome";
    }

    @Override
    public void execute(Player player, String[] args) {

        int userID = DatabaseTools.getUserID(player.getUniqueId());

        System.out.println(dslContext.selectFrom(Tables.HOMES).fetch());

        if (args.length > 0){
            Location loc = player.getLocation();

            var home = DatabaseTools.firstString(dslContext.select(Tables.HOMES.HOMENAME).from(Tables.HOMES)
                    .where(Tables.HOMES.USERID.eq(userID).and(Tables.HOMES.HOMENAME.equalIgnoreCase(args[0]))).fetchAny());

            if (home == null){
                player.sendMessage(MessageTools.parseFromPath(config, "Home Doesn't Exist", Template.template("name", home)));
                return;
            }
            dslContext.delete(Tables.HOMES).where(Tables.HOMES.USERID.eq(userID).and(Tables.HOMES.HOMENAME.equalIgnoreCase(home))).execute();
            player.sendMessage(MessageTools.parseFromPath(config,"Home Deleted", Template.template("name", home)));
        } else {
            dslContext.delete(Tables.HOMES).where(Tables.HOMES.USERID.eq(userID).and(Tables.HOMES.HOMENAME.equalIgnoreCase("home"))).execute();
            player.sendMessage(MessageTools.parseFromPath(config,"Home Deleted", Template.template("name", "home")));
        }

        System.out.println(dslContext.selectFrom(Tables.HOMES).fetch());
    }
}
