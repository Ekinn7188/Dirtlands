package net.dirtlands.commands.tab;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NpcTabCompleter extends PluginTabCompleter{
    @Override
    public List<String> getNames() {
        return List.of("npc");
    }

    @Override
    public List<String> tabCompleter(Player player, @NotNull String[] args) {
        return List.of("create", "select", "skin");
    }
}
