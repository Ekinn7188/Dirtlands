package net.dirtlands.commands.essentials.admin;

import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.tabscoreboard.TabMenu;
import org.bukkit.command.CommandSender;

public class Dirtlands extends PluginCommand {
    private static ConfigSetup config = Main.getPlugin().config();
    @Override
    public String getName() {
        return "dirtlands";
    }

    @Override
    public boolean isRequiresPlayer() {
        return false;
    }

    @Override
    protected Permission getPermissionType() {
        return Permission.DIRTLANDS;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                Main.getPlugin().config().reload();
                Main.getPlugin().npcInventory().reload();
                TabMenu.updateTab();

                sender.sendMessage(MessageTools.parseFromPath(config, "Dirtlands Reloaded"));
            }
        }
    }
}
