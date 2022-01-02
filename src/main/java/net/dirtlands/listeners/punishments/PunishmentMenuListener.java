package net.dirtlands.listeners.punishments;

import dirtlands.db.Tables;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.essentials.admin.punishments.PunishmentHistory;
import net.dirtlands.database.DatabaseTools;
import net.dirtlands.log.LogColor;
import net.dirtlands.tools.UUIDTools;
import net.dv8tion.jda.api.EmbedBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jooq.DSLContext;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class PunishmentMenuListener implements Listener {

    static ConfigSetup config = Main.getPlugin().config();
    static DSLContext dslContext = Main.getPlugin().getDslContext();

    static Pattern timePattern = Pattern.compile("\\s[0-9]+?\\s");


    @EventHandler
    public void onMenuClick(InventoryClickEvent e) {
        String inventoryName = PlainTextComponentSerializer.plainText().serialize(e.getView().title());
        for (Punishment punishment : Punishment.values()) {
            if (inventoryName.contains(punishment.getPunishment() + " Player ")){
                punishmentMenu(e, punishment);
                return;
            }
        }
        if (inventoryName.contains("'s Punishment History")){
            punishmentHistoryMenu(e);
        }
    }

    private void punishmentHistoryMenu(InventoryClickEvent e) {
        e.setCancelled(true);
        ItemStack item = e.getCurrentItem();
        if (item == null) {
            return;
        }
        Component itemName = item.getItemMeta().displayName();
        if (itemName == null) {
            return;
        }

        Inventory inv = e.getClickedInventory();
        if (inv == null) {
            return;
        }

        if (item.getType().equals(Material.RED_CONCRETE) || item.getType().equals(Material.ORANGE_CONCRETE)
                || item.getType().equals(Material.YELLOW_CONCRETE) || item.getType().equals(Material.LIME_CONCRETE)) {

            ItemStack playerHead = inv.getItem(4);
            if (playerHead == null) {
                return;
            }
            Component playerHeadName = playerHead.getItemMeta().displayName();
            if (playerHeadName == null) {
                return;
            }

            String playerName = PlainTextComponentSerializer.plainText().serialize(playerHeadName).substring(7);

            String uuid = UUIDTools.getUuid(playerName);

            OfflinePlayer player = UUIDTools.checkNameAndUUID(e.getWhoClicked(), playerName);
            if (player == null) {
                return;
            }


            List<Component> lore = item.lore();
            if (lore == null) {
                return;
            }

            String punishmentType = PlainTextComponentSerializer.plainText().serialize(itemName);

            dslContext.deleteFrom(Tables.PUNISHMENTS)
                    .where(Tables.PUNISHMENTS.USERID.eq(DatabaseTools.getUserID(uuid))
                            .and(Tables.PUNISHMENTS.PUNISHMENTTYPE.eq(punishmentType))
                            .and(Tables.PUNISHMENTS.PUNISHMENTSTART
                                    .startsWith(DatabaseTools.stringToLocalDateTime(PlainTextComponentSerializer.plainText()
                                            .serialize(lore.get(3)).substring(7))))
                    ).execute();

            Main.getPlugin().getJda().getGuilds().forEach(guild -> {
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle(punishmentType + " Removed From History");
                embedBuilder.addField("Player", player.getName(), true);
                embedBuilder.addField("Revoked By", e.getWhoClicked().getName(), true);
                embedBuilder.setColor(Color.BLUE);
                embedBuilder.setThumbnail("https://minotar.net/helm/" + player.getName() + "/64");
                try {
                    Objects.requireNonNull(guild.getTextChannelById(921676351696687175L)).sendMessage(" ").setEmbeds(embedBuilder.build()).queue();
                } catch (NullPointerException exception) {
                    exception.printStackTrace();
                }
            });



            Bukkit.getLogger().warning(LogColor.RED + e.getWhoClicked().getName() + " has removed a " + PlainTextComponentSerializer.plainText().serialize(itemName).toLowerCase() + " from " + playerName + "'s punishment history." + LogColor.RESET);

            e.getWhoClicked().openInventory(PunishmentHistory.makeHistoryMenu(player, e.getWhoClicked()));


        }
    }

    private void punishmentMenu(InventoryClickEvent e, Punishment punishment) {
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
                    PunishmentTools.timeMenu(punishment, punished, e.getWhoClicked(), reason);
                    return;
                }
                int timeNumber = -1; //if these values aren't changed set and player is muted/banned, its permanent
                String timeUnit = "";
                //get the time as a number
                if (lore.size() >= 3) {
                    String secondLine = PlainTextComponentSerializer.plainText().serialize(lore.get(2));
                    Matcher matcher = timePattern.matcher(secondLine);
                    if (matcher.find()) {
                        timeNumber = Integer.parseInt(matcher.group().replace(" ", ""));
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
                PunishmentTools.addPunishmentToDB(e.getWhoClicked(), punishment, e.getWhoClicked().getUniqueId().toString(), "", punished, LocalDateTime.now(), endTime, reason);
            }
        }
        e.getWhoClicked().closeInventory();
    }

}
