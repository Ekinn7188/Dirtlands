package net.dirtlands.listeners.punishments;

import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.tools.UUIDTools;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jooq.DSLContext;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static net.dirtlands.listeners.punishments.PunishmentTools.timeMenu;

public class PunishmentMenuListener implements Listener {

    static ConfigSetup config = Main.getPlugin().config();
    static DSLContext dslContext = Main.getPlugin().getDslContext();

    static Pattern timePattern = Pattern.compile("\\s[0-9]+?\\s");


    @EventHandler
    public void onMenuClick(InventoryClickEvent e) {
        String inventoryName = PlainTextComponentSerializer.plainText().serialize(e.getView().title());
        for (Punishment punishment : Punishment.values()) {
            if (inventoryName.contains(punishment.getPunishment() + " Player ")){
                e.setCancelled(true);
                String playerName = PlainTextComponentSerializer.plainText().serialize(e.getView().title()).replace(punishment.getPunishment() + " Player ", "");

                OfflinePlayer punished = UUIDTools.checkNameAndUUID(e.getWhoClicked(), playerName);
                ItemStack clicked = e.getCurrentItem();
                if (punished == null || punished.getName() == null || clicked == null) {
                    return;
                }

                List<Material> buttons = List.of(Material.RED_CONCRETE, Material.ORANGE_CONCRETE, Material.YELLOW_CONCRETE, Material.LIME_CONCRETE, Material.GREEN_CONCRETE);
                for (Material button : buttons) {
                    //custom time
                    if (clicked.getType().equals(Material.NAME_TAG)) {
                        PunishmentTools.customPunishment(punishment, punished, e.getWhoClicked());
                        return;
                    }

                    if (clicked.getType().equals(button)) {
                        List<Component> lore = clicked.lore();

                        if (lore == null || lore.size() < 2) {
                            return;
                        }

                        String reason = PlainTextComponentSerializer.plainText().serialize(lore.get(1)).replace(punishment.getPunishment() + " Reason: ", "");

                        //if right click, edit time
                        if (e.getClick().isRightClick()) {
                            timeMenu(punishment, punished, e.getWhoClicked(), reason);
                            return;
                        }
                        int timeNumber = -1; //if these values aren't changed set and player is muted/banned, its permanent
                        String timeUnit = "";
                        //get the time as a number
                        if (lore.size() >= 3) {
                            String secondLine = PlainTextComponentSerializer.plainText().serialize(lore.get(2));
                            Matcher matcher = timePattern.matcher(secondLine);
                            if (matcher.find()) {
                                timeNumber = parseInt(matcher.group().replace(" ", ""));
                                timeUnit = secondLine.substring(matcher.end());
                            }
                        }

                        //null if permanent
                        LocalDateTime endTime = null;

                        if (timeNumber != -1 && !timeUnit.equals("")) {
                            if (timeUnit.equalsIgnoreCase("Days")) {
                                endTime = LocalDateTime.now().plus(timeNumber, ChronoUnit.DAYS);
                            } else if (timeUnit.equalsIgnoreCase("Hours")) {
                                endTime = LocalDateTime.now().plus(timeNumber, ChronoUnit.HOURS);
                            } else if (timeUnit.equalsIgnoreCase("Minutes")) {
                                endTime = LocalDateTime.now().plus(timeNumber, ChronoUnit.MINUTES);
                            }
                        }
                        PunishmentTools.addPunishmentToDB(punishment, e.getWhoClicked().getUniqueId().toString(), punished, LocalDateTime.now(), endTime, reason);
                    }
                }
                e.getWhoClicked().closeInventory();
                return;
            }
        }
    }

}
