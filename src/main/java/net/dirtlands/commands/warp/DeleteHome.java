package net.dirtlands.commands.warp;

import net.dirtlands.Main;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.files.Warps;
import net.dirtlands.tools.MessageTools;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Set;

public class DeleteHome extends PluginCommand {

    Warps warps = Main.getPlugin().warps();

    @Override
    public String getName() {
        return "delhome";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length > 0){
            Location loc = player.getLocation();

            Set<String> uuids = Objects.requireNonNull(warps.get().getConfigurationSection("Homes")).getKeys(false);

            if (!uuids.contains(player.getUniqueId().toString())){
                player.sendMessage(MessageTools.parseFromPath("Home Doesn't Exist", Template.of("Name", args[0])));
                return;
            }

            Set<String> homes = Objects.requireNonNull(warps.get().getConfigurationSection("Homes." + player.getUniqueId())).getKeys(false);

            if (homes.contains(args[0])){
                warps.get().set("Homes."+ player.getUniqueId() + "." + args[0], null);
                warps.save();
                warps.reload();

                player.sendMessage(MessageTools.parseFromPath("Home Deleted", Template.of("Name", args[0])));
            } else {
                player.sendMessage(MessageTools.parseFromPath("Home Doesn't Exist", Template.of("Name", args[0])));
            }
        } else {
            warps.get().set("Homes."+ player.getUniqueId() + ".home", null);
            warps.save();
            warps.reload();
            player.sendMessage(MessageTools.parseFromPath("Home Deleted", Template.of("Name", "home")));
        }
    }
}
