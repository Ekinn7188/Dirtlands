package net.dirtlands.commands.warp;

import net.dirtlands.commands.PluginCommand;
import net.dirtlands.files.Warps;
import net.dirtlands.tools.ConfigTools;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HomeList extends PluginCommand {

    @Override
    public String getName() {
        return "homes";
    }

    @Override
    public void execute(Player player, String[] args) {
        super.execute(player, args);
        ConfigurationSection homesFromConfig = Warps.get().getConfigurationSection("Homes." + player.getUniqueId());
        if (homesFromConfig != null){
            List<String> homes = new ArrayList<>(homesFromConfig.getKeys(false));
            String commaSeperatedHomes = String.join(", ", homes);
            player.sendMessage(ConfigTools.parseFromPath("Home List", Template.of("Homes", commaSeperatedHomes)));
        } else {
            player.sendMessage(ConfigTools.parseFromPath("No Homes"));
        }
    }

}
