package net.dirtlands.tools;

import jeeper.utils.MessageTools;
import jeeper.utils.config.Config;
import net.dirtlands.Main;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.UUID;
import java.util.regex.Pattern;

public class UUIDTools {

    private static final Pattern UUID_FIX = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");
    static Config config = Main.getPlugin().config();

    /**
     * Gets the UUID of a player from the Mojang API.
     * @param name The username of the player.
     * @return their uuid
     */
    public static String getUuid(String name) {
        String url = "https://api.mojang.com/users/profiles/minecraft/"+name;
        try {
            String UUIDJson = IOUtils.toString(new URL(url), Charset.defaultCharset());
            if(UUIDJson.isEmpty()) {
                return null;
            }
            JSONObject UUIDObject = (JSONObject) JSONValue.parseWithException(UUIDJson);
            return UUID_FIX.matcher(UUIDObject.get("id").toString().replace("-", "")).replaceAll("$1-$2-$3-$4-$5");
        } catch (IOException | ParseException e) {
            //not a valid name, will return null
        }
        return null;
    }

    /**
     * Gets the name of an offline player and checks if it's a real name
     * @param name the name to check
     * @param sender the sender to send error messages to
     * @return the name of the player or null if it's not a real name
     */
    public static String getNameIfExists(String name, CommandSender sender) {
        boolean isPlayer = sender instanceof Player; // flag to determine whether player or console
        String UUID = getUuid(name);

        if (UUID == null) {
            sender.sendMessage(MessageTools.parseFromPath(config,"Player Doesnt Exist", Placeholder.parsed("player", name)));
            return null;
        }
        OfflinePlayer playerArg = Bukkit.getOfflinePlayer(java.util.UUID.fromString(UUID));
        String username = playerArg.getName();
        if (username == null) {
            sender.sendMessage(MessageTools.parseFromPath(config,"Player Hasnt Logged In", Placeholder.parsed("player", name)));
            return null;
        }

        return username;
    }

    public static @Nullable OfflinePlayer checkNameAndUUID(CommandSender sender, String name) {
        String playerUUID = UUIDTools.getUuid(name);

        if (playerUUID == null) {
            sender.sendMessage(MessageTools.parseFromPath(config,"Player Doesnt Exist", Placeholder.parsed("player", name)));
            return null;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(playerUUID));
        String playerName = player.getName();
        if (playerName == null) {
            sender.sendMessage(MessageTools.parseFromPath(config,"Player Hasnt Logged In", Placeholder.parsed("player", name)));
            return null;
        }

        return player;
    }

}
