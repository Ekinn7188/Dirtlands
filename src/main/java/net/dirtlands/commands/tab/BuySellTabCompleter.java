package net.dirtlands.commands.tab;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BuySellTabCompleter extends PluginTabCompleter {

    @Override
    public List<String> getNames() {
        return List.of("buy", "sell");
    }

    @Override
    public List<String> tabCompleter(Player player, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("<diamonds>");
        }
        else if (args.length == 2) {
            return List.of("<tokens>");
        }
        return List.of();
    }
}
