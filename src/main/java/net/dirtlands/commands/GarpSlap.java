package net.dirtlands.commands;

import org.bukkit.entity.Player;

public class GarpSlap extends PluginCommand {
    @Override
    public String getName() {
        return "garp slap";
    }

    @Override
    public void execute(Player player, String[] args) {
        player.chat("/warp shop");
    }
}
