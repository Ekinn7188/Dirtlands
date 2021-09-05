package net.dirtlands.commands.npc;

import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.nms.npc.Npc;
import net.dirtlands.tools.ConfigTools;
import net.kyori.adventure.text.minimessage.Template;
import net.minecraft.server.level.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class NpcCommand extends PluginCommand {

    private static HashMap<UUID, EntityPlayer> selectedNpc = new HashMap<>();

    @Override
    public String getName() {
        return "npc";
    }

    @Override
    protected Permission getPermissionType() {
        return Permission.NPC;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length > 0){
            switch (args[0].toLowerCase()){
                case "create":
                    Main.getPlugin().getNpc().spawn(player.getLocation(), new ArrayList<>(Bukkit.getOnlinePlayers()));
                    break;
                case "select":
                    selectNearestNPC(player);
                    break;
                case "skin":
                    if (args.length > 1){
                        if (!Main.getPlugin().getNpc().skin(selectedNpc.get(player.getUniqueId()), new ArrayList<>(Bukkit.getOnlinePlayers()), args[1])){
                            player.sendMessage(ConfigTools.parseFromPath("Player Doesnt Exist", Template.of("Player", args[1])));
                        }
                    }
                    break;
            }
        }
    }

    protected void selectNearestNPC(Player player){
        List<EntityPlayer> allNpcs = Npc.getNpcs();
        Location playerLocation = player.getLocation();

        if (allNpcs.isEmpty()) {
            player.sendMessage(ConfigTools.parseFromPath("No Npcs"));
            return;
        }

        EntityPlayer closestNPC;
        closestNPC = allNpcs.get(0);
        closestNPC = allNpcs.remove(0);
        for (EntityPlayer npc : allNpcs){
            if (npc.getBukkitEntity().getLocation().distance(playerLocation) <= closestNPC.getBukkitEntity().getLocation().distance(playerLocation)) {
                closestNPC = npc;
            }
        }

        selectedNpc.put(player.getUniqueId(), closestNPC);
        player.sendMessage(ConfigTools.parseFromPath("Npc Selected", Template.of("Name", closestNPC.displayName)));
    }
}
