package net.dirtlands.commands.warp;

import net.dirtlands.commands.PluginCommand;
import net.dirtlands.files.Warps;
import net.dirtlands.tools.ConfigTools;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Set;

public class DeleteHome extends PluginCommand {

    @Override
    public String getName() {
        return "delhome";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length > 0){
            Location loc = player.getLocation();

            Set<String> uuids = Objects.requireNonNull(Warps.get().getConfigurationSection("Homes")).getKeys(false);

            if (!uuids.contains(player.getUniqueId().toString())){
                player.sendMessage(ConfigTools.parseFromPath("Home Doesn't Exist", Template.of("Name", args[0])));
                return;
            }

            Set<String> homes = Objects.requireNonNull(Warps.get().getConfigurationSection("Homes." + player.getUniqueId())).getKeys(false);

            if (homes.contains(args[0])){
                Warps.get().set("Homes."+ player.getUniqueId() + "." + args[0], null);
                Warps.save();
                Warps.reload();

                player.sendMessage(ConfigTools.parseFromPath("Home Deleted", Template.of("Name", args[0])));
            } else {
                player.sendMessage(ConfigTools.parseFromPath("Home Doesn't Exist", Template.of("Name", args[0])));
            }
        } else {
            Warps.get().set("Homes."+ player.getUniqueId() + ".home", null);
            Warps.save();
            Warps.reload();
            player.sendMessage(ConfigTools.parseFromPath("Home Deleted", Template.of("Name", "home")));
        }
    }
}
