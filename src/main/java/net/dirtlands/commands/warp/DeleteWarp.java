package net.dirtlands.commands.warp;

import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.files.Warps;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.command.CommandSender;

import java.util.Objects;
import java.util.Set;

public class DeleteWarp extends PluginCommand {
    Warps warps = Main.getPlugin().warps();
    private static ConfigSetup config = Main.getPlugin().config();

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

            Set<String> warpNames = Objects.requireNonNull(warps.get().getConfigurationSection("Warps")).getKeys(false);

            if (!warpNames.contains(args[0])){
                sender.sendMessage(MessageTools.parseFromPath(config, "Warp Doesn't Exist", Template.template("Name", args[0])));
                return;
            }

            warps.get().set("Warps."+ args[0], null);
            warps.save();
            warps.reload();

            sender.sendMessage(MessageTools.parseFromPath(config,"Warp Deleted", Template.template("Name", args[0])));

        }
    }

}
