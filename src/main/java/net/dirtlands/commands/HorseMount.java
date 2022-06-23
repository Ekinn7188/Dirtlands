package net.dirtlands.commands;

import jeeper.utils.MessageTools;
import net.dirtlands.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class HorseMount extends PluginCommand {
    @Override
    public String getName() {
        return "horsemount";
    }

    @Override
    protected Permission getPermissionType() {
        return super.getPermissionType();
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(MessageTools.parseFromPath(Main.getPlugin().config(), "Correct Usage",
                    Placeholder.parsed("command", "/horsemount {speed} {jump strength} {Color} {Style}")));
            return;
        }

        int speed;
        int jump;
        Horse.Color color;
        Horse.Style style;

        try {
            speed = Integer.parseInt(args[0]);
            jump = Integer.parseInt(args[1]);
            color = Horse.Color.valueOf(args[2]);
            style = Horse.Style.valueOf(args[3]);
        } catch (IllegalArgumentException e) {
            player.sendMessage(MessageTools.parseFromPath(Main.getPlugin().config(), "Correct Usage",
                    Placeholder.parsed("command", "/horsemount {speed} {jump strength} {Color} {Style}")));
            return;
        }

        ItemStack saddle = new ItemStack(Material.SADDLE, 1);

        List<Component> lore = new ArrayList<>();

        lore.add(MessageTools.parseText("<!italic><#856f2d>Speed: <#b8a567>" + speed));
        lore.add(MessageTools.parseText("<!italic><#856f2d>Jump: <#b8a567>" + jump));
        lore.add(MessageTools.parseText("<!italic><#856f2d>Color: <#b8a567>" + color));
        lore.add(MessageTools.parseText("<!italic><#856f2d>Style: <#b8a567>" + style));

        ItemMeta meta = saddle.getItemMeta();
        meta.displayName(MessageTools.parseText("<!italic><#7c3e12><bold>Horse Mount"));
        meta.lore(lore);

        saddle.setItemMeta(meta);

        player.getInventory().addItem(saddle);
    }
}
