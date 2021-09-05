package net.dirtlands.commands.tab;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DirtlandsTabCompleter extends PluginTabCompleter {

    @Override
    public List<String> getNames() {
        return List.of("dirtlands");
    }

    @Override
    public List<String> tabCompleter(Player player, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        completions.add("reload");

        return completions;
    }
}
