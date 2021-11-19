package net.dirtlands.commands.economy;

import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.economy.Economy;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EconomyCmd extends PluginCommand {

    private static final ConfigSetup config = Main.getPlugin().config();

    @Override
    public String getName() {
        return "economy";
    }

    @Override
    protected Permission getPermissionType() {
        return Permission.ECONOMY;
    }

    @Override
    public boolean isRequiresPlayer() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length >= 2) {
            Player player = Bukkit.getPlayer(args[1]);
            if (player == null){
                sender.sendMessage(MessageTools.parseFromPath(config,"Player Doesnt Exist", Template.template("Player", args[1])));
                return;
            }

            if (args[0].equalsIgnoreCase("get")) {
                sender.sendMessage(MessageTools.parseFromPath(config, "Player Balance",
                        Template.template("player", player.displayName()), Template.template("balance", String.valueOf(Economy.getBalance(player)))));
                return;
            }

            if (args.length < 3) {
                sender.sendMessage(MessageTools
                        .parseText("&cCorrect Usage: /economy <add/remove/forceremove/set/get> <player> <number(except for get command)>"));
            }

            switch (args[0].toLowerCase()){
                case "add":
                    try {
                        Economy.addMoney(player, Integer.parseInt(args[2]));
                        sender.sendMessage(MessageTools.parseFromPath(config, "Player Balance",
                                Template.template("player", player.displayName()), Template.template("balance", String.valueOf(Economy.getBalance(player)))));
                    } catch (NumberFormatException e) {
                        sender.sendMessage(MessageTools.parseText("&cCorrect Usage: /economy add <number> <player>"));
                    }
                    break;
                case "remove":
                    try {
                        if (!Economy.removeMoney(player, Integer.parseInt(args[2]))) {
                            sender.sendMessage(MessageTools.parseFromPath(config, "Player Doesnt Have Enough Money", Template.template("player", player.displayName())));
                        } else {
                            sender.sendMessage(MessageTools.parseFromPath(config, "Player Balance",
                                    Template.template("player", player.displayName()), Template.template("balance", String.valueOf(Economy.getBalance(player)))));
                        }

                    } catch (NumberFormatException e) {
                        sender.sendMessage(MessageTools.parseText("&cCorrect Usage: /economy remove <number> <player>"));
                    }
                    break;
                case "forceremove":
                    try {
                        Economy.forceRemoveMoney(player, Integer.parseInt(args[2]));
                        sender.sendMessage(MessageTools.parseFromPath(config, "Player Balance",
                                Template.template("player", player.displayName()), Template.template("balance", String.valueOf(Economy.getBalance(player)))));
                    } catch (NumberFormatException e) {
                        sender.sendMessage(MessageTools.parseText("&cCorrect Usage: /economy forceremove <number> <player>"));
                    }
                    break;
                case "set":
                    try {
                        Economy.setBalance(player, Integer.parseInt(args[2]));
                        sender.sendMessage(MessageTools.parseFromPath(config, "Player Balance",
                                Template.template("player", player.displayName()), Template.template("balance", String.valueOf(Economy.getBalance(player)))));
                    } catch (NumberFormatException e) {
                        sender.sendMessage(MessageTools.parseText("&cCorrect Usage: /economy set <number> <player>"));
                    }
                    break;
                default:
                    sender.sendMessage(MessageTools
                            .parseText("&cCorrect Usage: /economy <add/remove/forceremove/set/get> <player> <number(except for get command)>"));
                    break;
            }
        } else {
            sender.sendMessage(MessageTools
                    .parseText("&cCorrect Usage: /economy <add/remove/forceremove/set/get> <player> <number(except for get command)>"));
        }
    }
}
