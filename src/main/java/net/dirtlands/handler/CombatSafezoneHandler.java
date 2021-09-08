package net.dirtlands.handler;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;
import net.dirtlands.listeners.CombatTag;
import net.dirtlands.tools.MessageTools;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;

import java.util.Objects;
import java.util.Set;

public class CombatSafezoneHandler extends FlagValueChangeHandler<StateFlag.State> {
    public static final Factory FACTORY = new Factory();

    public static class Factory extends Handler.Factory<CombatSafezoneHandler> {
        @Override
        public CombatSafezoneHandler create(Session session) {
            return new CombatSafezoneHandler(session);
        }
    }

    protected CombatSafezoneHandler(Session session) {
        super(session, Flags.PVP);
    }

    @Override
    protected void onInitialValue(LocalPlayer player, ApplicableRegionSet set, StateFlag.State value) {
    }

    @Override
    protected boolean onSetValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, StateFlag.State currentValue, StateFlag.State lastValue, MoveType moveType) {
        return false;
    }

    @Override
    protected boolean onAbsentValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, StateFlag.State lastValue, MoveType moveType) {
        return false;
    }

    @Override
    public boolean onCrossBoundary(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType) {
        boolean flagDisabled = !toSet.testState(player, Flags.PVP);

        if (flagDisabled && CombatTag.getTasks().containsKey(player.getUniqueId())){//if the flag is off and the player is in combat

            Title title = Title.title(MessageTools.parseFromPath("Enter Safezone In Combat Title"),
                    MessageTools.parseFromPath("Enter Safezone In Combat Subtitle"));

            Objects.requireNonNull(Bukkit.getPlayer(player.getUniqueId())).showTitle(title);
            return false;
        }

        return true; //return whether the movement should be allowed
    }
}
