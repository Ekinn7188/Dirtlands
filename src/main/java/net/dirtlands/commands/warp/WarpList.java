package net.dirtlands.commands.warp;

import net.dirtlands.commands.PluginCommand;
import net.dirtlands.files.Warps;
import net.dirtlands.tools.ConfigTools;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class WarpList extends PluginCommand {

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

        ConfigurationSection warpsFromConfig = Warps.get().getConfigurationSection("Warps");
        if (warpsFromConfig != null){
            List<String> warps = new ArrayList<>(warpsFromConfig.getKeys(false));
            String commaSeperatedWarps = String.join(", ", warps);
            sender.sendMessage(ConfigTools.parseFromPath("Warp List", Template.of("Warps", commaSeperatedWarps)));
        } else {
            sender.sendMessage(ConfigTools.parseFromPath("No Warps"));
        }
    }

}
