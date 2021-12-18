package net.dirtlands.commands.tab;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GarpSlapTabCompleter extends PluginTabCompleter {


    @Override
    public List<String> getNames() {
        return List.of("garp");
    }

    @Override
    public List<String> tabCompleter(Player player, @NotNull String[] args) {
        return List.of("slap");
    }
}
