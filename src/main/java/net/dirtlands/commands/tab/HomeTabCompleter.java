package net.dirtlands.commands.tab;

import dirtlands.db.Tables;
import net.dirtlands.Main;
import net.dirtlands.database.DatabaseTools;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HomeTabCompleter extends PluginTabCompleter {

    @Override
    public List<String> getNames() {
        return List.of("home", "sethome", "delhome");
    }

    @Override
    public List<String> tabCompleter(Player player, @NotNull String[] args) {
        return Main.getPlugin().getDslContext().select(Tables.HOMES.HOMENAME).from(Tables.HOMES)
                .where(Tables.HOMES.USERID.eq(DatabaseTools.getUserID(player.getUniqueId()))).fetch().getValues(Tables.HOMES.HOMENAME);
    }

}
