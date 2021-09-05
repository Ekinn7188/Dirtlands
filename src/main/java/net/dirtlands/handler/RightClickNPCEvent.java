package net.dirtlands.handler;

import net.minecraft.server.level.EntityPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RightClickNPCEvent extends Event implements Cancellable {

    private final Player player;
    private final EntityPlayer npc;
    private boolean isCancelled;
    private static final HandlerList HANDLERS = new HandlerList();

    public RightClickNPCEvent(Player player, EntityPlayer npc){
        this.player = player;
        this.npc = npc;
    }

    public Player getPlayer(){
        return player;
    }

    public EntityPlayer getNpc(){
        return npc;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean bl) {
        isCancelled = bl;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
