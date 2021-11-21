package net.dirtlands.commands.tab;

import dirtlands.db.Tables;
import net.dirtlands.Main;
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
        var warps = Main.getPlugin().getDslContext().select(Tables.WARPS.WARPNAME, Tables.WARPS.WARPPERMISSION).from(Tables.HOMES)
                .fetch().intoMap(Tables.WARPS.WARPNAME, Tables.WARPS.WARPPERMISSION);

        List<String> options = new ArrayList<>();

        warps.forEach((key, value) -> {
            if (player.hasPermission(value)) {
                options.add(key);
            }
        });

        if (options.size() == 0) {
            return null;
        }
        return options;

    }
}
