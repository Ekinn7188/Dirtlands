package net.dirtlands.commands.economy;

import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.economy.Currency;
import org.bukkit.entity.Player;

public class Token extends PluginCommand {

    @Override
    public String getName() {
        return "token";
    }

    @Override
    protected Permission getPermissionType() {
        return Permission.ECONOMY;
    }

    @Override
    public void execute(Player player, String[] args) {
        player.getInventory().addItem(Currency.TOKEN_ITEM);
    }
}
