package net.dirtlands.commands;

import net.dirtlands.tools.MessageTools;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class PluginCommand implements CommandExecutor {
    abstract public String getName();

    final public Optional<Permission> getPermission() {
        return Optional.ofNullable(getPermissionType());
    }

    protected Permission getPermissionType() {
        return null;
    }

    public boolean isRequiresPlayer() {
        return true;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (getPermission()
                .map(permission -> !sender.hasPermission(permission.getName()))
                .orElse(false)) {
            sender.sendMessage(MessageTools.parseFromPath("No Command Permission"));
            return true;
        }

        if (isRequiresPlayer()){
            if (sender instanceof Player){
                execute((Player) sender, args);
                return true;
            }
            sender.sendMessage(MessageTools.parseFromPath("Player Only Command"));
            return true;
        }

        execute(sender, args);
        return true;
    }

    public void execute(Player player, String[] args){}
    public void execute(CommandSender sender, String[] args) {}
}
