package net.dirtlands.commands.warp;

import net.dirtlands.Main;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.files.Warps;
import net.dirtlands.tools.ConfigTools;
import net.dirtlands.tools.Countdown;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.entity.Player;

import java.util.Set;

public class Home extends PluginCommand {

    @Override
    public String getName() {
        return "home";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length > 0){

            //noinspection ConstantConditions,DuplicatedCode
            Set<String> uuids = Warps.get().getConfigurationSection("Homes").getKeys(false);

            if (!uuids.contains(player.getUniqueId().toString())){
                player.sendMessage(ConfigTools.parseFromPath("Home Doesn't Exist", Template.of("Name", args[0])));
                return;
            }

            //noinspection ConstantConditions
            Set<String> homes = Warps.get().getConfigurationSection("Homes." + player.getUniqueId()).getKeys(false);

            if (homes.size() > 0){
                if (homes.contains(args[0])){
                    Countdown.startCountdown(player, "Homes." + player.getUniqueId() + "." + args[0], args[0], Main.getPlugin());
                } else {
                    player.sendMessage(ConfigTools.parseFromPath("Home Doesn't Exist", Template.of("Name", args[0])));
                }
            } else {
                player.sendMessage(ConfigTools.parseFromPath("Home Doesn't Exist", Template.of("Name", args[0])));
            }
        } else {
            if (Warps.get().getString("Homes." + player.getUniqueId() + ".home") != null){
                Countdown.startCountdown(player, "Homes." + player.getUniqueId() + ".home", "home", Main.getPlugin());
            } else {
                player.sendMessage(ConfigTools.parseFromPath("Home Doesn't Exist", Template.of("Name", "home")));
            }
        }
    }

}