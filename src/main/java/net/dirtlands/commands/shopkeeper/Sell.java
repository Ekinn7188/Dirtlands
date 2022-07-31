package net.dirtlands.commands.shopkeeper;

import jeeper.utils.MessageTools;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

public class Sell extends PluginCommand {
    @Override
    public String getName() {
        return "sell";
    }

    @Override
    protected Permission getPermissionType() {
        return Permission.SHOPKEEPER;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(MessageTools.parseFromPath(Main.getPlugin().config(), "Correct Usage",
                    Placeholder.unparsed("command", "/sell <diamonds> <tokens>")));
        }

        Buy.buySellLoreCommand(player, args, "Sell");
    }
}
