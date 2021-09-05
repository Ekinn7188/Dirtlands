package net.dirtlands.commands.tab;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NicknameTabCompleter extends PluginTabCompleter {

    @Override
    public List<String> getNames() {
        return List.of("nickname");
    }

    @Override
    public List<String> tabCompleter(Player player, @NotNull String[] args) {
        List<String> options = new ArrayList<>();

        options.add("reset");

        return options;
    }
}
