package net.dirtlands.commands.warp;

import jeeper.utils.MessageTools;
import net.dirtlands.Main;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.tools.Countdown;
import org.bukkit.entity.Player;

import java.util.Objects;

public class Spawn extends PluginCommand {

    @Override
    public String getName() {
        return "spawn";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (Objects.equals(Main.getPlugin().warps().get().getString("Spawn.Coords"), "")){
            player.sendMessage(MessageTools.parseText("<red>Spawn doesn't exist yet! Make sure to set it with <dark_red>/setspawn</dark_red>!</red>"));
            return;
        }
        Countdown.startCountdown(player, "Spawn.Coords", "Spawn", Main.getPlugin());
    }

}
