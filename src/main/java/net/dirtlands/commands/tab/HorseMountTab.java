package net.dirtlands.commands.tab;

import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class HorseMountTab extends PluginTabCompleter {
    @Override
    public List<String> getNames() {
        return List.of("horsemount");
    }

    @Override
    public List<String> tabCompleter(Player player, @NotNull String[] args) {
        if (args.length == 3) {
            return Arrays.stream(Horse.Color.values()).map(Enum::toString).toList();
        }
        else if (args.length == 4) {
            return Arrays.stream(Horse.Style.values()).map(Enum::toString).toList();
        }
        return List.of();
    }
}
