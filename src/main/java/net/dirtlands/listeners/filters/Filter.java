package net.dirtlands.listeners.filters;

import jeeper.utils.MessageTools;
import jeeper.utils.config.Config;
import net.dirtlands.Main;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.HumanEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Filter {
    public static Config config = Main.getPlugin().config();
    public static final ArrayList<Pattern> blockedPatterns = getBlockedRegex(Objects.requireNonNull(config.get().getStringList("Word Filter")));

    public static ArrayList<Pattern> getBlockedRegex(List<String> BlockedWords) {
        ArrayList<Pattern> patterns = new ArrayList<>();

        for (String word : BlockedWords) {
            StringBuilder patternBuilder = new StringBuilder("(^|\\s)");

            char[] blockedChar = word.toCharArray();
            boolean lastCharDouble = false;
            for (int i = 0; i < blockedChar.length; i++){
                String searchChar = switch (String.valueOf(blockedChar[i])) {
                    case "a" -> "(a|4)";
                    case "b" -> "(b|8)";
                    case "e" -> "(e|3)";
                    case "g" -> "(g|9)";
                    case "i" -> "(i|1)";
                    case "l" -> "(l|1)";
                    case "o" -> "(o|0)";
                    case "s" -> "(s|5)";
                    case "t" -> "(t|7)";
                    default -> String.valueOf(blockedChar[i]);
                };
                if (searchChar.equals(" ")) {
                    continue;
                }
                if (i != blockedChar.length-1 && blockedChar[i] == blockedChar[i+1]) {
                    patternBuilder.append("((").append(searchChar).append(")(").append(searchChar).append("|\\s|\\.|_|-)+?)");
                    lastCharDouble = true;
                    continue;
                }
                if (lastCharDouble) {
                    lastCharDouble = false;
                    continue;
                }
                if (i == blockedChar.length-1) {
                    patternBuilder.append("(").append(searchChar).append(")+?");
                    continue;
                }
                patternBuilder.append("(").append(searchChar).append(")+?").append("(\\s|\\.|_|-)*?");
            }
            patternBuilder.append("(\\s|$)");
            patterns.add(Pattern.compile(patternBuilder.toString(), Pattern.CASE_INSENSITIVE));
        }
        return patterns;
    }

    /**
     * @param p the Player to send an error message to
     * @param messageString the message to check
     * @return true if the message is blocked, false if not
     */
    public static boolean blockCheck(HumanEntity p, String messageString) {
        for (Pattern pattern : blockedPatterns){
            Matcher matcher = pattern.matcher(messageString);
            if (matcher.find()){
                String blockedMessage = matcher.group();
                if (matcher.group().charAt(0) == ' ') {
                    blockedMessage = blockedMessage.substring(1);
                }

                p.sendMessage(MessageTools.parseFromPath(config, "Chat Blocked", Placeholder.parsed("word", blockedMessage.trim())));
                return true;
            }
        }
        return false;
    }

}
