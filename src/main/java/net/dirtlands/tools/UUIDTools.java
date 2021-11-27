package net.dirtlands.tools;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Pattern;

public class UUIDTools {

    private static final Pattern UUID_FIX = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");
    /**
     * Gets the UUID of a player from the Mojang API.
     * @param name The username of the player.
     * @return their uuid
     */
    public static String getUuid(String name) {
        String url = "https://api.mojang.com/users/profiles/minecraft/"+name;
        try {
            String UUIDJson = IOUtils.toString(new URL(url));
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
}
