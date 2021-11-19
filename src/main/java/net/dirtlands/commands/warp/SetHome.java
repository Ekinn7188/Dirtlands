package net.dirtlands.commands.warp;

import jeeper.utils.LocationParser;
import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.files.Warps;
import net.dirtlands.tools.NumberAfterPermission;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class SetHome extends PluginCommand {
    Warps warps = Main.getPlugin().warps();
    private static ConfigSetup config = Main.getPlugin().config();

    @Override
    public String getName() {
        return "sethome";
    }

    @Override
    protected Permission getPermissionType() {
        return Permission.SETHOME;
    }

    @Override
    public void execute(Player player, String[] args) {
        Location loc = player.getLocation();
        int largestSetHomeSize = NumberAfterPermission.get(player, "dirtlands.sethome.");

        if (largestSetHomeSize == -1) {
            largestSetHomeSize = 0;
        }


        ConfigurationSection playersInConfigSection = warps.get().getConfigurationSection("Homes");
        if (playersInConfigSection == null || !playersInConfigSection.getKeys(false).contains(player.getUniqueId().toString())) {
            warps.get().set("Homes." + player.getUniqueId(), "");
            warps.save();
            warps.reload();
        }
        ConfigurationSection homes = warps.get().getConfigurationSection("Homes." + player.getUniqueId());
        Set<String> homeNames = new HashSet<>();

        if (homes != null) {
            homeNames = homes.getKeys(false);
        }

        String[] newArgs = new String[1];

        if (args.length == 0) {
            newArgs[0] = "home";
        } else {
            newArgs[0] = args[0];
        }

        if (homeNames.size() < largestSetHomeSize || homeNames.contains(newArgs[0])) {
            warps.get().set("Homes." + player.getUniqueId() + "." + newArgs[0], LocationParser.roundedLocationToString(loc));
            warps.save();
            warps.reload();
            player.sendMessage(MessageTools.parseFromPath(config, "Home Created", Template.template("Name", newArgs[0])));
        } else {
            player.sendMessage(MessageTools.parseFromPath(config, "Too Many Homes", Template.template("Number", String.valueOf(largestSetHomeSize))));
        }
    }
}
