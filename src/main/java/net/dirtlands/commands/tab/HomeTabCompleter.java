package net.dirtlands.commands.tab;

import net.dirtlands.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class HomeTabCompleter extends PluginTabCompleter {

    @Override
    public List<String> getNames() {
        return List.of("home", "sethome", "delhome");
    }

    @Override
    public List<String> tabCompleter(Player player, @NotNull String[] args) {

        ConfigurationSection homesFromConfig = Main.getPlugin().warps().get().getConfigurationSection("Homes." + player.getUniqueId());
        if (homesFromConfig != null){
            return new ArrayList<>(homesFromConfig.getKeys(false));
        }

        return null;

    }

}
