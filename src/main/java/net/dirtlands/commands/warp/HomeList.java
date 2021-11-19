package net.dirtlands.commands.warp;

import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.PluginCommand;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HomeList extends PluginCommand {
    private static ConfigSetup config = Main.getPlugin().config();

    @Override
    public String getName() {
        return "homes";
    }

    @Override
    public void execute(Player player, String[] args) {
        super.execute(player, args);
        ConfigurationSection homesFromConfig = Main.getPlugin().warps().get().getConfigurationSection("Homes." + player.getUniqueId());
        if (homesFromConfig != null){
            List<String> homes = new ArrayList<>(homesFromConfig.getKeys(false));
            String commaSeperatedHomes = String.join(", ", homes);
            player.sendMessage(MessageTools.parseFromPath(config, "Home List", Template.template("Homes", commaSeperatedHomes)));
        } else {
            player.sendMessage(MessageTools.parseFromPath(config, "No Homes"));
        }
    }

}
