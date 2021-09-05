package net.dirtlands.commands.tab;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class PluginTabCompleter implements TabCompleter {

    abstract public List<String> getNames();

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {

        List<String> results = tabCompleter((Player) sender, args);

        if (results == null){
            List<String> playerList = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()){
                playerList.add(p.getName());
            }

            return playerList;
        }

        return results;

    }

    public abstract List<String> tabCompleter(Player player, @NotNull String[] args);
}
