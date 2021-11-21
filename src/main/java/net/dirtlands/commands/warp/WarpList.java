package net.dirtlands.commands.warp;

import dirtlands.db.Tables;
import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.database.DatabaseTools;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jooq.DSLContext;

public class WarpList extends PluginCommand {
    private static ConfigSetup config = Main.getPlugin().config();
    DSLContext dslContext = Main.getPlugin().getDslContext();

    @Override
    public String getName() {
        return "warps";
    }

    @Override
    public boolean isRequiresPlayer() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        var warps = dslContext.select(Tables.WARPS.WARPNAME).from(Tables.WARPS).fetch().getValues(Tables.WARPS.WARPNAME);

        if (warps.size() > 0){
            StringBuilder commaSeperatedWarps = new StringBuilder(String.join(", ", warps));

            if (sender instanceof Player player) {
                commaSeperatedWarps = new StringBuilder();
                for (int i = 0; warps.size() > i; i++) {
                    if (warps.get(i).equalsIgnoreCase("spawn")) {
                        continue;
                    }

                    var permission = DatabaseTools.firstString(dslContext.select(Tables.WARPS.WARPPERMISSION).from(Tables.WARPS)
                            .where(Tables.WARPS.WARPNAME.equalIgnoreCase(warps.get(i))).fetchAny());

                    if (permission == null || player.hasPermission(permission)) {
                        commaSeperatedWarps.append(warps.get(i));
                        if (i != warps.size() - 1) {
                            if (warps.get(i++).equals("spawn")) {
                                continue;
                            }
                            commaSeperatedWarps.append(", ");
                        }
                    }
                }
            }


            sender.sendMessage(MessageTools.parseFromPath(config, "Warp List", Template.template("warps", commaSeperatedWarps.toString())));
        } else {
            sender.sendMessage(MessageTools.parseFromPath(config, "No Warps"));
        }
    }

}
