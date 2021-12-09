package net.dirtlands.commands.essentials.warp;

import dirtlands.db.Tables;
import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.database.DatabaseTools;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.command.CommandSender;
import org.jooq.DSLContext;

public class DeleteWarp extends PluginCommand {

    private static ConfigSetup config = Main.getPlugin().config();
    DSLContext dslContext = Main.getPlugin().getDslContext();

    @Override
    public String getName() {
        return "delwarp";
    }

    @Override
    protected Permission getPermissionType() {
        return Permission.DELETEWARP;
    }

    @Override
    public boolean isRequiresPlayer() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 0){

            var warpID = DatabaseTools.firstInt(dslContext.select(Tables.WARPS.WARPID).from(Tables.WARPS)
                    .where(Tables.WARPS.WARPNAME.equalIgnoreCase(args[0])).fetchAny());

            if (warpID == -1){
                sender.sendMessage(MessageTools.parseFromPath(config, "Warp Doesnt Exist", Template.template("name", args[0])));
                return;
            }

            dslContext.delete(Tables.WARPS).where(Tables.WARPS.WARPID.eq(warpID)).execute();
            sender.sendMessage(MessageTools.parseFromPath(config,"Warp Deleted", Template.template("name", args[0])));
        }
    }

}
