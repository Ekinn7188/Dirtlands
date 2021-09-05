package net.dirtlands.nms.npc;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.dirtlands.files.Npcs;
import net.dirtlands.tools.LocationTools;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.UUID;

public class Npc_1_17_R1 implements Npc {

    @Override
    public void spawn(Location loc, List<Player> visibility) {
        MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer worldServer = ((CraftWorld) loc.getWorld()).getHandle();
        String name = "rename me!";
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), name);
        EntityPlayer npc = new EntityPlayer(minecraftServer, worldServer, gameProfile);
        NPC.add(npc);
        npc.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

        for (Player p : visibility) {
            addNPCPacket(npc, p);
        }

        Npcs.get().set("Npc." + npc.getId() + ".Name", name);
        Npcs.get().set("Npc." + npc.getId() + ".Location", LocationTools.locationToString(loc));
        Npcs.save();
        Npcs.reload();
    }

    @Override
    public boolean skin(EntityPlayer npc, List<Player> players, String username) {
        for (Player p : players) {
            removeNPCPacket(npc, p);
        }

        String texture, signature;


        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
            InputStreamReader reader = new InputStreamReader(url.openStream());
            String uuid = new JsonParser().parse(reader).getAsJsonObject().get("id").getAsString();

            URL url2 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            InputStreamReader reader2 = new InputStreamReader(url2.openStream());
            JsonObject property = new JsonParser().parse(reader2).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
            texture = property.get("value").getAsString();
            signature = property.get("signature").getAsString();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        npc.getProfile().getProperties().put("textures", new Property("textures", texture, signature));

        npc.getDataWatcher().set(DataWatcherRegistry.a.a(17), (byte) 127);

        for (Player p : players) {
            addNPCPacket(npc, p);
        }

        return true;
    }

    private void addNPCPacket(EntityPlayer npc, Player p) {
        PlayerConnection connection = ((CraftPlayer) p).getHandle().b;
        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, npc));
        connection.sendPacket(new PacketPlayOutNamedEntitySpawn(npc));
        connection.sendPacket(new PacketPlayOutEntityMetadata(npc.getId(), npc.getDataWatcher(), true));
        connection.sendPacket(new PacketPlayOutEntityHeadRotation(npc, (byte) (npc.getBukkitYaw() * 256 / 360)));
    }

    public void removeNPCPacket(EntityPlayer npc, Player player) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().b;
        connection.sendPacket(new PacketPlayOutEntityDestroy(npc.getId()));
    }
}
