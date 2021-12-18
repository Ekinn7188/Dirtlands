package net.dirtlands.commands.essentials.admin.punishments;

import dirtlands.db.Tables;
import dirtlands.db.tables.records.PunishmentsRecord;
import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.database.DatabaseTools;
import net.dirtlands.listeners.punishments.Punishment;
import net.dirtlands.tools.ItemTools;
import net.dirtlands.tools.UUIDTools;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PunishmentHistory extends PluginCommand {
    static DSLContext dslContext = Main.getPlugin().getDslContext();
    static ConfigSetup config = Main.getPlugin().config();

    @Override
    public String getName() {
        return "history";
    }

    @Override
    protected Permission getPermissionType() {
        return Permission.HISTORY;
    }

    @Override
    public boolean isRequiresPlayer() {
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(MessageTools.parseText("&cCorrect Usage: /history {player}"));
            return;
        }

        OfflinePlayer punished = UUIDTools.checkNameAndUUID(sender, args[0]);
        if (punished == null) {
            return;
        }

        if (sender instanceof Player p) {
            p.openInventory(makeHistoryMenu(punished, p));
        }

    }

    public static Inventory makeHistoryMenu(OfflinePlayer player, HumanEntity viewer){
        var history = dslContext.selectFrom(Tables.PUNISHMENTS)
                .where(Tables.PUNISHMENTS.USERID.eq(DatabaseTools.getUserID(player.getUniqueId())))
                .orderBy(DSL.case_(Tables.PUNISHMENTS.PUNISHMENTTYPE)
                                .when(Punishment.BAN.getPunishment(), 1)
                                .when(Punishment.MUTE.getPunishment(), 2)
                                .when(Punishment.KICK.getPunishment(), 3)
                                .when(Punishment.WARN.getPunishment(), 4)
                                .else_(5).asc()
                        , Tables.PUNISHMENTS.PUNISHMENTEND.desc().nullsLast()).limit(28).fetch();

        assert player.getName() != null; //checked in checkNameAndUUID

        Inventory inv = Bukkit.createInventory(viewer, 54, MessageTools.parseText("<dark_red>" + player.getName() + "<red>'s Punishment History"));

        for (int i = 0; i < 9; i++) {
            inv.setItem(i, ItemTools.createGuiItem(Material.BLACK_STAINED_GLASS_PANE, Component.text(""), 1));
            inv.setItem(i+45, ItemTools.createGuiItem(Material.BLACK_STAINED_GLASS_PANE, Component.text(""), 1));
            if (i == 4) {
                ItemStack playerHead = ItemTools.getHead(player);
                inv.setItem(4, ItemTools.createGuiItem(playerHead, playerHead.getType(),
                        MessageTools.parseText("&cPlayer &4<player>", Template.template("player", player.getName())),
                        1));
            }
            if (i*9 <= 45) {
                inv.setItem(i*9, ItemTools.createGuiItem(Material.BLACK_STAINED_GLASS_PANE, Component.text(""), 1));
            }
            if (i!=0 && i*9-1 <= 53) {
                inv.setItem(i*9-1, ItemTools.createGuiItem(Material.BLACK_STAINED_GLASS_PANE, Component.text(""), 1));
            }
        }

        for (PunishmentsRecord punishment : history) {
            inv.addItem(makeHistoryItem(player, punishment));
        }

        return inv;
    }

    private static ItemStack makeHistoryItem(OfflinePlayer player, PunishmentsRecord punishment){
        assert player.getName() != null; //checked in checkNameAndUUID

        String punisherUUID = dslContext.select(Tables.USERS.USERUUID).from(Tables.USERS).where(Tables.USERS.USERID.eq(punishment.getPunisherid())).fetchOne(Tables.USERS.USERUUID);
        String punisherName = "Console";
        if (punisherUUID != null) {
            punisherName = Bukkit.getOfflinePlayer(UUID.fromString(punisherUUID)).getName();
        }
        if (punisherName == null) {
            punisherName = "Console";
        }

        String punishmentType = punishment.getPunishmenttype();
        String reason = punishment.getPunishmentreason();
        String start = DatabaseTools.localDateTimeToString(punishment.getPunishmentstart());
        String end = DatabaseTools.localDateTimeToString(punishment.getPunishmentend());

        Map<List<String>, Material> punishmentInfo = switch(Punishment.valueOf(punishmentType.toUpperCase().replace(" ", "_"))) {
            case IP_BAN -> Map.of(List.of("<gray>", "<dark_gray>"), Material.GRAY_CONCRETE);
            case BAN -> Map.of(List.of("<red>", "<dark_red>"), Material.RED_CONCRETE);
            case MUTE -> Map.of(List.of("<#ff8c00>", "<#cc5500>"), Material.ORANGE_CONCRETE);
            case KICK -> Map.of(List.of("<#ffe205>", "<gold>"), Material.YELLOW_CONCRETE);
            case WARN -> Map.of(List.of("<#39ff14>", "<#31b81f>"), Material.LIME_CONCRETE);
        };

        List<String> colors = punishmentInfo.entrySet().iterator().next().getKey();
        String lightColor = colors.get(0);
        String darkColor = colors.get(1);
        Material material = punishmentInfo.entrySet().iterator().next().getValue();

        List<Component> lore = new ArrayList<>();
        lore.add(MessageTools.parseText(" "));
        lore.add(MessageTools.parseText(lightColor + "Reason: "+ darkColor +"<reason>", Template.template("reason", reason == null ? "None" : reason)));
        lore.add(MessageTools.parseText(lightColor + "Punished By: " + darkColor +"<punisher>", Template.template("punisher", punisherName)));
        lore.add(MessageTools.parseText(lightColor + "Start: " + darkColor +"<start>", Template.template("start", start)));
        if (!punishment.getPunishmenttype().equals(Punishment.WARN.getPunishment()) && !punishment.getPunishmenttype().equals(Punishment.KICK.getPunishment())) {
            if (end == null) {
                lore.add(MessageTools.parseText( "<red>End: <end>", Template.template("end", "Permanent")));
            } else {
                if (punishment.getPunishmentend().isAfter(LocalDateTime.now())) {
                    lore.add(MessageTools.parseText("<red>End: <end>", Template.template("end", end)));
                } else {
                    lore.add(MessageTools.parseText("<green>End: <end>", Template.template("end", end)));
                }
            }
        }
        lore.add(MessageTools.parseText(" "));
        lore.add(MessageTools.parseText(darkColor + "Click" + lightColor + " to clear from " + darkColor + player.getName() + lightColor + "'s record"));

        return ItemTools.createGuiItem(material, MessageTools.parseText(lightColor + punishmentType), 1, lore.toArray(new Component[0]));
    }
}
