package net.dirtlands.commands.essentials.warp;

import dirtlands.db.Tables;
import jeeper.utils.MessageTools;
import net.dirtlands.Main;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.database.DatabaseTools;
import net.dirtlands.tools.Countdown;
import org.bukkit.entity.Player;
import org.jooq.DSLContext;

public class Spawn extends PluginCommand {
    DSLContext dslContext = Main.getPlugin().getDslContext();

    @Override
    public String getName() {
        return "spawn";
    }

    @Override
    public void execute(Player player, String[] args) {

        var spawn = DatabaseTools.firstString(dslContext.select(Tables.WARPS.WARPLOCATION).from(Tables.WARPS)
                .where(Tables.WARPS.WARPNAME.equalIgnoreCase("spawn")).fetchAny());

        if (spawn == null){
            player.sendMessage(MessageTools.parseText("<red>Spawn doesn't exist yet! Make sure to set it with <dark_red>/setspawn</dark_red>!</red>"));
            return;
        }
        Countdown.startCountdown(player, spawn, "spawn", Main.getPlugin());
    }

}
