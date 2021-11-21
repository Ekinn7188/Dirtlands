package net.dirtlands.commands.warp;

import dirtlands.db.Tables;
import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.database.DatabaseTools;
import net.dirtlands.tools.Countdown;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.entity.Player;
import org.jooq.DSLContext;

public class Warp extends PluginCommand {
    private static ConfigSetup config = Main.getPlugin().config();
    DSLContext dslContext = Main.getPlugin().getDslContext();

    @Override
    public String getName() {
        return "warp";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length > 0){

            String warpLocation = DatabaseTools.firstString(dslContext.select(Tables.WARPS.WARPLOCATION).from(Tables.WARPS)
                    .where(Tables.WARPS.WARPNAME.equalIgnoreCase(args[0])).fetchAny());

            String warpPermission = DatabaseTools.firstString(dslContext.select(Tables.WARPS.WARPPERMISSION).from(Tables.WARPS)
                    .where(Tables.WARPS.WARPNAME.equalIgnoreCase(args[0])).fetchAny());

            if (warpLocation == null || warpPermission == null || !player.hasPermission(warpPermission)) {
                player.sendMessage(MessageTools.parseFromPath(config, "Warp Doesnt Exist", Template.template("name", args[0])));
                return;
            }
            Countdown.startCountdown(player, warpLocation, args[0], Main.getPlugin());
        }
    }


}
