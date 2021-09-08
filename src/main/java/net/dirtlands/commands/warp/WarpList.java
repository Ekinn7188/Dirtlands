package net.dirtlands.commands.warp;

import net.dirtlands.Main;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.tools.MessageTools;
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

        ConfigurationSection warpsFromConfig = Main.getPlugin().warps().get().getConfigurationSection("Warps");
        if (warpsFromConfig != null){
            List<String> warps = new ArrayList<>(warpsFromConfig.getKeys(false));
            String commaSeperatedWarps = String.join(", ", warps);
            sender.sendMessage(MessageTools.parseFromPath("Warp List", Template.of("Warps", commaSeperatedWarps)));
        } else {
            sender.sendMessage(MessageTools.parseFromPath("No Warps"));
        }
    }

}
