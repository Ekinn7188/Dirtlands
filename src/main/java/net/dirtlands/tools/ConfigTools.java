package net.dirtlands.tools;

import net.dirtlands.files.Config;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ConfigTools {

    public static Component parseFromPath(@NotNull String path, @NotNull Template... placeholders){
        String message = getString(path);

        message = message.replaceAll("&a", "<green>")
                .replaceAll("&b", "<aqua>")
                .replaceAll("&c", "<red>")
                .replaceAll("&d", "<light_purple>")
                .replaceAll("&e", "<yellow>")
                .replaceAll("&f", "<white>")
                .replaceAll("&1", "<dark_blue>")
                .replaceAll("&2", "<dark_green>")
                .replaceAll("&3", "<dark_aqua>")
                .replaceAll("&4", "<dark_red>")
                .replaceAll("&5", "<dark_purple>")
                .replaceAll("&6", "<gold>")
                .replaceAll("&7", "<gray>")
                .replaceAll("&8", "<dark_gray>")
                .replaceAll("&9", "<blue>")
                .replaceAll("&0", "<black>")
                .replaceAll("&k", "<obfuscated>")
                .replaceAll("&l", "<bold>")
                .replaceAll("&m", "<strikethrough>")
                .replaceAll("&n", "<underline>")
                .replaceAll("&o", "<italic>")
                .replaceAll("&r", "<reset>");

        return MiniMessage.get().parse(message, placeholders);
    }



    public static String getString(@NotNull String path){
        try{
            return Objects.requireNonNull(Config.get().getString(path));
        } catch (NullPointerException e){
            Bukkit.broadcast(Component.text()
                    .content("Config \"" + path + "\" does not exist. Check logs for details")
                    .color(NamedTextColor.RED).build());
            return "";
        }

    }

}
