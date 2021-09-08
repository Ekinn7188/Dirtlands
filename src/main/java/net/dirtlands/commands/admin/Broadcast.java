package net.dirtlands.commands.admin;

import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.tools.MessageTools;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class Broadcast extends PluginCommand {
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

            Bukkit.broadcast(MessageTools.parseFromPath("Broadcast Prefix").append(messageComponent));
        }
    }
}