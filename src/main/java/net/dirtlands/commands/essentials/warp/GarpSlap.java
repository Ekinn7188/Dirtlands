package net.dirtlands.commands.essentials.warp;

import net.dirtlands.commands.PluginCommand;
import org.bukkit.entity.Player;

public class GarpSlap extends PluginCommand {
    @Override
    public String getName() {
        return "garp";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 0) {
            return;
        }
        if (args[0].equalsIgnoreCase("slap")) {
            System.out.println("ran");
            player.performCommand("warp shop");
        }
    }
}
