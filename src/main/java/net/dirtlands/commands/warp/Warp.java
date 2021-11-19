package net.dirtlands.commands.warp;

import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.tools.Countdown;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.entity.Player;

import java.util.Set;

public class Warp extends PluginCommand {
    private static ConfigSetup config = Main.getPlugin().config();

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
                player.sendMessage(MessageTools.parseFromPath(config, "Warp Doesn't Exist", Template.template("Name", args[0])));
            }
        }
    }


}
