package net.dirtlands.commands.warp;

import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.PluginCommand;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class WarpList extends PluginCommand {
    private static ConfigSetup config = Main.getPlugin().config();

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
            sender.sendMessage(MessageTools.parseFromPath(config, "Warp List", Template.template("Warps", commaSeperatedWarps)));
        } else {
            sender.sendMessage(MessageTools.parseFromPath(config, "No Warps"));
        }
    }

}
