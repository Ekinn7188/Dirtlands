package net.dirtlands.commands.tab;

import net.dirtlands.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class WarpTabCompleter extends PluginTabCompleter {

    @Override
    public List<String> getNames() {
        return List.of("warp", "setwarp", "delwarp");
    }

    @Override
    public List<String> tabCompleter(Player player, @NotNull String[] args) {
        ConfigurationSection warpsFromConfig = Main.getPlugin().warps().get().getConfigurationSection("Warps");
        if (warpsFromConfig != null){
            return new ArrayList<>(warpsFromConfig.getKeys(false));
        }

        return null;

    }
}
