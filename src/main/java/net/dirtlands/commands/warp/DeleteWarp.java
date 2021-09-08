package net.dirtlands.commands.warp;

import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.files.Warps;
import net.dirtlands.tools.MessageTools;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.command.CommandSender;

import java.util.Objects;
import java.util.Set;

public class DeleteWarp extends PluginCommand {
    Warps warps = Main.getPlugin().warps();

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
                sender.sendMessage(MessageTools.parseFromPath("Warp Doesn't Exist", Template.of("Name", args[0])));
                return;
            }

            warps.get().set("Warps."+ args[0], null);
            warps.save();
            warps.reload();

            sender.sendMessage(MessageTools.parseFromPath("Warp Deleted", Template.of("Name", args[0])));

        }
    }

}
