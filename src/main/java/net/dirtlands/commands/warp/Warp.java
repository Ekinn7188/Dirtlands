package net.dirtlands.commands.warp;

import net.dirtlands.Main;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.tools.Countdown;
import net.dirtlands.tools.MessageTools;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.entity.Player;

import java.util.Set;

public class Warp extends PluginCommand {

    @Override
    public String getName() {
        return "warp";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length > 0){
            //noinspection ConstantConditions
            Set<String> warpNames = Main.getPlugin().warps().get().getConfigurationSection("Warps").getKeys(false);
            if (warpNames.contains(args[0])){
                Countdown.startCountdown(player, "Warps." + args[0] + ".Coords", args[0], Main.getPlugin());
            } else{
                player.sendMessage(MessageTools.parseFromPath("Warp Doesn't Exist", Template.of("Name", args[0])));
            }
        }
    }


}
