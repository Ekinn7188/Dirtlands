package net.dirtlands.commands.admin;

import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class Broadcast extends PluginCommand {
    private static ConfigSetup config = Main.getPlugin().config();

    @Override
    public String getName() {
        return "broadcast";
    }

    @Override
    protected Permission getPermissionType() {
        return Permission.BROADCAST;
    }

    @Override
    public boolean isRequiresPlayer() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 0) {
            String message = String.join(" ", args);
            Component messageComponent = MessageTools.parseText(message);

            for (String url : MessageTools.fetchURLs(message)) {
                messageComponent = messageComponent.clickEvent(ClickEvent.openUrl(url));
            }

            Bukkit.broadcast(MessageTools.parseFromPath(config, "Broadcast Prefix").append(messageComponent));
        }
    }
}