package net.dirtlands.commands.economy;

import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.economy.Economy;
import net.dirtlands.tools.UUIDTools;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

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

            OfflinePlayer player = UUIDTools.checkNameAndUUID(sender, args[1]);
            if (player == null) {
                return;
            }

            String name = player.getName();
            assert name != null;//checked in UUIDTools.checkNameAndUUID

            if (args[0].equalsIgnoreCase("get")) {
                sender.sendMessage(MessageTools.parseFromPath(config, "Player Balance",
                        Template.template("player", name), Template.template("balance", Economy.commaSeperatedBalance(player))));
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
                                Template.template("player", name), Template.template("balance", Economy.commaSeperatedBalance(player))));
                    } catch (NumberFormatException e) {
                        sender.sendMessage(MessageTools.parseFromPath(config, "Correct Usage", Template.template("command", "/economy add <number> <player>")));
                    }
                    break;
                case "remove":
                    try {
                        if (!Economy.removeMoney(player, Integer.parseInt(args[2]))) {
                            sender.sendMessage(MessageTools.parseFromPath(config, "Player Doesnt Have Enough Money", Template.template("player", name)));
                        } else {
                            sender.sendMessage(MessageTools.parseFromPath(config, "Player Balance",
                                    Template.template("player", name), Template.template("balance", Economy.commaSeperatedBalance(player))));
                        }

                    } catch (NumberFormatException e) {
                        sender.sendMessage(MessageTools.parseFromPath(config, "Correct Usage", Template.template("command", "/economy remove <number> <player>")));
                    }
                    break;
                case "forceremove":
                    try {
                        Economy.forceRemoveMoney(player, Integer.parseInt(args[2]));
                        sender.sendMessage(MessageTools.parseFromPath(config, "Player Balance",
                                Template.template("player", name), Template.template("balance", Economy.commaSeperatedBalance(player))));
                    } catch (NumberFormatException e) {
                        sender.sendMessage(MessageTools.parseFromPath(config, "Correct Usage", Template.template("command", "/economy forceremove <number> <player>")));
                    }
                    break;
                case "set":
                    try {
                        Economy.setBalance(player, Integer.parseInt(args[2]));
                        sender.sendMessage(MessageTools.parseFromPath(config, "Player Balance",
                                Template.template("player", name), Template.template("balance", Economy.commaSeperatedBalance(player))));
                    } catch (NumberFormatException e) {
                        sender.sendMessage(MessageTools.parseFromPath(config, "Correct Usage", Template.template("command", "/economy set <number> <player>")));
                    }
                    break;
                default:
                    sender.sendMessage(MessageTools.parseFromPath(config, "Correct Usage", Template.template("command",
                            "/economy <add/remove/forceremove/set/get> <player> <number(except for get command)>")));
                    break;
            }
        } else {
            sender.sendMessage(MessageTools.parseFromPath(config, "Correct Usage", Template.template("command",
                    "/economy <add/remove/forceremove/set/get> <player> <number(except for get command)>")));
        }
    }




}
