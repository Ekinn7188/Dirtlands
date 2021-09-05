package net.dirtlands.commands.warp;

import net.dirtlands.Main;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.files.Warps;
import net.dirtlands.tools.Countdown;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.Objects;

public class Spawn extends PluginCommand {

    @Override
    public String getName() {
        return "spawn";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (Objects.equals(Warps.get().getString("Spawn.Coords"), "")){
            player.sendMessage(MiniMessage.get().parse("<red>Spawn doesn't exist yet! Make sure to set it with <dark_red>/setspawn</dark_red>!</red>"));
            return;
        }
        Countdown.startCountdown(player, "Spawn.Coords", "Spawn", Main.getPlugin());
    }

}
