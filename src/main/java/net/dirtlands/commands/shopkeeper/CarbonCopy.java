package net.dirtlands.commands.shopkeeper;

import jeeper.utils.MessageTools;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CarbonCopy extends PluginCommand {
    @Override
    public String getName() {
        return "carboncopy";
    }

    @Override
    protected Permission getPermissionType() {
        return Permission.SHOPKEEPER;
    }

    @Override
    public void execute(Player player, String[] args) {
        ItemStack item = player.getEquipment().getItemInMainHand();

        if (item.getType().equals(Material.AIR)) {
            player.sendMessage(MessageTools.parseFromPath(Main.getPlugin().config(), "Item In Hand"));
            return;
        }
        List<Component> lore = item.lore() == null ? new ArrayList<>() : item.lore();

        assert lore != null;
        lore.add(MessageTools.parseText("<italic><#2BD5D5>Carbon Copy"));

        item.lore(lore);
        player.sendMessage(MessageTools.parseFromPath(Main.getPlugin().config(), "Shop Lore Set"));
    }
}
