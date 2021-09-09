package net.dirtlands.commands.tab;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EconomyTabCompleter extends PluginTabCompleter {
    @Override
    public List<String> getNames() {
        return List.of("economy");
    }

    @Override
    public List<String> tabCompleter(Player player, @NotNull String[] args) {
        List<String> completer = new ArrayList<>();
        if (args.length == 1) {
            completer.add("add");
            completer.add("remove");
            completer.add("forceremove");
            completer.add("set");
            completer.add("get");
        } else if (args.length == 3 && !args[0].equals("get")) {
            completer.add("<number>");
        } else {
            return null; //null sends player list tab completer
        }
        return completer;
    }
}
