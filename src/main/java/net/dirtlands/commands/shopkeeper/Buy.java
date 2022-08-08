package net.dirtlands.commands.shopkeeper;

import jeeper.utils.MessageTools;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.economy.Currency;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Buy extends PluginCommand {
    @Override
    public String getName() {
        return "buy";
    }

    @Override
    protected Permission getPermissionType() {
        return Permission.SHOPKEEPER;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(MessageTools.parseFromPath(Main.getPlugin().config(), "Correct Usage",
                    Placeholder.unparsed("command", "/buy <expensive tokens> <tokens>")));
        }

        buySellLoreCommand(player, args, "Buy");
    }

    protected static void buySellLoreCommand(Player player, String[] args, String buySell) {
        ItemStack item = player.getEquipment().getItemInMainHand();

        if (item.getType().equals(Material.AIR)) {
            player.sendMessage(MessageTools.parseFromPath(Main.getPlugin().config(), "Item In Hand"));
            return;
        }

        List<Component> lore = item.lore();
        if (lore == null) {
            lore = new ArrayList<>();
        }

        int expensiveTokens = 0, tokens = 0;
        try {
            expensiveTokens = Integer.parseInt(args[0]);
            tokens = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(MessageTools.parseFromPath(Main.getPlugin().config(), "Invalid Number"));
            return;
        } catch (IndexOutOfBoundsException e) {
            //Continue, the value will be 0 if there's an index exception
        }

        Currency price = new Currency(expensiveTokens, tokens);
        price.convertTokensToExpensiveTokens();

        expensiveTokens = price.getExpensiveTokens();
        tokens = price.getTokens();

        AtomicInteger buySellLine = new AtomicInteger(-1);

        List<Component> finalLore = lore;
        lore.forEach(line -> {
            if (PlainTextComponentSerializer.plainText().serialize(line).contains(buySell)) {
                buySellLine.set(finalLore.indexOf(line));
            }
        });

        if (buySellLine.get() == -1) {
            if (expensiveTokens != 0 && tokens != 0) {
                lore.add(MessageTools.parseText("<!italic><#2BD5D5>" + buySell + ": <dark_aqua><white>"
                        + Currency.EXPENSIVE_TOKEN_CHARACTER + "</white> x " + expensiveTokens + " <gold><white>"
                        + Currency.TOKEN_CHARACTER + "</white> x " + tokens));
            } else if (tokens != 0) {
                lore.add(MessageTools.parseText("<!italic><#2BD5D5>" + buySell + ": <gold><white>"
                        + Currency.TOKEN_CHARACTER + "</white> x " + tokens));
            } else {
                lore.add(MessageTools.parseText("<!italic><#2BD5D5>" + buySell + ": <dark_aqua><white>"
                        + Currency.EXPENSIVE_TOKEN_CHARACTER + "</white> x " + expensiveTokens));
            }
        } else {
            if (expensiveTokens != 0 && tokens != 0) {
                lore.set(buySellLine.get(), MessageTools.parseText("<!italic><#2BD5D5>" + buySell + ": <dark_aqua><white>"
                        + Currency.EXPENSIVE_TOKEN_CHARACTER + "</white> x " + expensiveTokens + " <gold><white>"
                        + Currency.TOKEN_CHARACTER + "</white> x " + tokens));
            } else if (tokens != 0) {
                lore.set(buySellLine.get(), MessageTools.parseText("<!italic><#2BD5D5>" + buySell + ": <gold><white>"
                        + Currency.TOKEN_CHARACTER + "</white> x " + tokens));
            } else {
                lore.set(buySellLine.get(), MessageTools.parseText("<!italic><#2BD5D5>" + buySell + ": <dark_aqua><white>"
                        + Currency.EXPENSIVE_TOKEN_CHARACTER + "</white> x " + expensiveTokens));
            }
        }

        item.lore(lore);
        player.sendMessage(MessageTools.parseFromPath(Main.getPlugin().config(), "Shop Lore Set"));
    }
}
