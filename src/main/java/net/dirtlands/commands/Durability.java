package net.dirtlands.commands;

import jeeper.utils.MessageTools;
import net.dirtlands.Main;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Durability extends PluginCommand {
    @Override
    public String getName() {
        return "durability";
    }

    @Override
    protected Permission getPermissionType() {
        return Permission.DURABILITY;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 0 ) {
            player.sendMessage(MessageTools.parseFromPath(Main.getPlugin().config(), "Correct Usage",
                    Placeholder.unparsed("command", "/durability <durability> <maximum durability>")));
        }
        ItemStack item = player.getEquipment().getItemInMainHand();

        if (item.getType().equals(Material.AIR)) {
            player.sendMessage(MessageTools.parseFromPath(Main.getPlugin().config(), "Item In Hand"));
            return;
        }

        if (args.length == 1) {
            int maxDurability = net.dirtlands.tools.Durability.getMaxDurability(item);
            if (maxDurability == -1) {
                player.sendMessage(MessageTools.parseFromPath(Main.getPlugin().config(), "Correct Usage",
                        Placeholder.unparsed("command", "/durability <durability> <maximum durability>")));
                return;
            }
            try {
                int durability = Integer.parseInt(args[0]);
                net.dirtlands.tools.Durability.setCustomDurability(item, durability, maxDurability);
            } catch (NumberFormatException e) {
                player.sendMessage(MessageTools.parseFromPath(Main.getPlugin().config(), "Invalid Number"));
            }

        }
        else {
            try {
                int durability = Integer.parseInt(args[0]);
                int maxDurability = Integer.parseInt(args[1]);
                net.dirtlands.tools.Durability.setCustomDurability(item, durability, maxDurability);
            } catch (NumberFormatException e) {
                player.sendMessage(MessageTools.parseFromPath(Main.getPlugin().config(), "Invalid Number"));
            }
        }
    }
}
