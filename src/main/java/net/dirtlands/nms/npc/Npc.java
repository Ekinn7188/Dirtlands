package net.dirtlands.nms.npc;

import net.minecraft.server.level.EntityPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public interface Npc {

    static List<EntityPlayer> NPC = new ArrayList<>();

    void spawn(Location loc, List<Player> visibility);
    boolean skin(EntityPlayer npc, List<Player> player, String username);//figure out how skins are rendered
    static List<EntityPlayer> getNpcs(){
        return NPC;
    }


}
