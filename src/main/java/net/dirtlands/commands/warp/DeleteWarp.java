package net.dirtlands.commands.warp;

import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.files.Warps;
import net.dirtlands.tools.ConfigTools;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.command.CommandSender;

import java.util.Objects;
import java.util.Set;

public class DeleteWarp extends PluginCommand {

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

            Set<String> warpNames = Objects.requireNonNull(Warps.get().getConfigurationSection("Warps")).getKeys(false);

            if (!warpNames.contains(args[0])){
                sender.sendMessage(ConfigTools.parseFromPath("Warp Doesn't Exist", Template.of("Name", args[0])));
                return;
            }

            Warps.get().set("Warps."+ args[0], null);
            Warps.save();
            Warps.reload();

            sender.sendMessage(ConfigTools.parseFromPath("Warp Deleted", Template.of("Name", args[0])));

        }
    }

}
