package net.dirtlands.commands.essentials.admin.punishments;

import jeeper.utils.MessageTools;
import net.dirtlands.listeners.punishments.Punishment;
import net.dirtlands.tools.ItemTools;
import net.dirtlands.tools.UUIDTools;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Punishments {

    /**
     * Creates an Inventory for the punishment of the specified player.
     * @param punishment Kick, Warn, Ban, or Mute
     * @param viewer The person that will see the menu
     * @param punishedName The name of the player being punished
     * @return the Inventory that will be opened
     */
    public static Inventory getPunishmentsMenu(Punishment punishment, Player viewer, String punishedName) {
        //a hashmap that stores default ban reasons and their times
        //key reason, value time in hours
        //-1 means permanent
        Map<String, Integer> reasons = new LinkedHashMap<>();
        if (punishment.equals(Punishment.BAN)) {
            reasons.put("Hacking / Exploiting", -1);//perm
            reasons.put("Explicit Content", 72);//3 days
            reasons.put("Duping", -1);//perm
            reasons.put("Excessive Toxicity", 48);//2 days
            reasons.put("Constant Harassment", 24);//1 day
        }
        else if (punishment.equals(Punishment.WARN) || punishment.equals(Punishment.MUTE)) {
            reasons.put("Begging for Staff", 1);
            reasons.put("Disrespect", 3);
            reasons.put("Saying Slurs", 168);//1 week
            reasons.put("Chat Flooding", 1);
            reasons.put("Harassment / Toxicity", 12);
        } else {
            return null;
        }

        try {
            OfflinePlayer punished = UUIDTools.checkNameAndUUID(viewer, punishedName);
            if (punished == null) {
                return null;
            }
            assert punished.getName() != null; //checked in checkNameAndUUID
            Inventory inventory = Bukkit.createInventory(viewer, 27, MessageTools.parseText("&c" + punishment.getPunishment() + " Player &4<player>",
                    Template.template("player", punished.getName())));

            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, ItemTools.createGuiItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, Component.empty(), 1));
            }

            ItemStack playerHead = ItemTools.getHead(punished);
            inventory.setItem(4, ItemTools.createGuiItem(playerHead, playerHead.getType(),
                    MessageTools.parseText("&c" + punishment.getPunishment() + " Player &4<player>", Template.template("player", punished.getName())),
                    1));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(" "));
            lore.add(MessageTools.parseText("<red>" + punishment.getPunishment() + " Reason: <dark_red>" + reasons.keySet().toArray()[0]));
            lore.addAll(determineTimeMessage((int) reasons.values().toArray()[0], punishment, "<red>", "<dark_red>"));

            inventory.setItem(11, ItemTools.createGuiItem(Material.RED_CONCRETE,
                    MessageTools.parseText("<dark_red>" + punishment.getPunishment()), 1, lore.toArray(new Component[0])));

            lore.clear();
            lore.add(Component.text(" "));
            lore.add(MessageTools.parseText("<#ff8c00>" + punishment.getPunishment() + " Reason: <#cc5500>" + reasons.keySet().toArray()[1]));
            lore.addAll(determineTimeMessage((int) reasons.values().toArray()[1], punishment, "<#ff8c00>", "<#cc5500>"));

            inventory.setItem(12, ItemTools.createGuiItem(Material.ORANGE_CONCRETE,
                    MessageTools.parseText("<#cc5500>" + punishment.getPunishment()), 1, lore.toArray(new Component[0])));

            lore.clear();
            lore.add(Component.text(" "));
            lore.add(MessageTools.parseText("<#ffe205>" + punishment.getPunishment() + " Reason: <gold>" + reasons.keySet().toArray()[2]));
            lore.addAll(determineTimeMessage((int) reasons.values().toArray()[2], punishment, "<#ffe205>", "<gold>"));

            inventory.setItem(13, ItemTools.createGuiItem(Material.YELLOW_CONCRETE,
                    MessageTools.parseText("<gold>" + punishment.getPunishment()), 1, lore.toArray(new Component[0])));

            lore.clear();
            lore.add(Component.text(" "));
            lore.add(MessageTools.parseText("<#39ff14>" + punishment.getPunishment() + " Reason: <#31b81f>" + reasons.keySet().toArray()[3]));
            lore.addAll(determineTimeMessage((int) reasons.values().toArray()[3], punishment, "<#39ff14>", "<#31b81f>"));

            inventory.setItem(14, ItemTools.createGuiItem(Material.LIME_CONCRETE,
                    MessageTools.parseText("<#31b81f>" + punishment.getPunishment()), 1, lore.toArray(new Component[0])));

            lore.clear();
            lore.add(Component.text(" "));
            lore.add(MessageTools.parseText("<dark_green>" + punishment.getPunishment() + " Reason: <#007700>" + reasons.keySet().toArray()[4]));
            lore.addAll(determineTimeMessage((int) reasons.values().toArray()[4], punishment, "<dark_green>", "<#007700>"));

            inventory.setItem(15, ItemTools.createGuiItem(Material.GREEN_CONCRETE,
                    MessageTools.parseText("<#007700>" + punishment.getPunishment()), 1, lore.toArray(new Component[0])));

            lore.clear();
            lore.add(Component.text(" "));
            lore.add(MessageTools.parseText("<red>" + punishment.getPunishment() + " Reason: <dark_red>Custom"));
            lore.add(MessageTools.parseText("<red>Time: <dark_red>Custom"));

            inventory.setItem(22, ItemTools.createGuiItem(Material.NAME_TAG,
                    MessageTools.parseText("<red>Custom " + punishment.getPunishment()), 1, lore.toArray(new Component[0])));
            return inventory;
        } catch (AssertionError e) {
            return null;
        }

    }

    /**
     * Determines the time message portion of a punishment
     * @param timeValue the length of the punishment in hours
     * @param punishment the punishment type
     * @param lightColor the lightest color of the message
     * @param darkColor the darkest color of the message
     * @return a component with length 2, index 0 is the default time and index 1 is the message saying "right click to edit time".
     */
    private static List<Component> determineTimeMessage(int timeValue, Punishment punishment, String lightColor, String darkColor) {
        List<Component> message = new ArrayList<>();

        if (timeValue == -1) {
            message.add(MessageTools.parseText(lightColor + "Time: " + darkColor + "Permanent"));
            message.add(MessageTools.parseText(lightColor + "Right Click to Edit Time"));
        } else if (punishment.equals(Punishment.BAN)) { //convert hours to days
            message.add(MessageTools.parseText(lightColor + "Time: "+ darkColor + timeValue/24 + " Hours"));
            message.add(MessageTools.parseText(lightColor + "Right Click to Edit Time"));
        } else if (punishment.equals(Punishment.MUTE)) { //keep time in hours, but convert to days if divisible by 24 hours
            message.add(MessageTools.parseText(lightColor + "Time: "+ darkColor + (timeValue%24==0 ? timeValue/24 + " Days" : timeValue + " Hours")));
            message.add(MessageTools.parseText(lightColor + "Right Click to Edit Time"));
        } else {
            message.add(Component.empty());
            message.add(Component.empty());
        }

        return message;
    }


}
