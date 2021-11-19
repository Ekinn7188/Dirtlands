package net.dirtlands.listeners;


import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import jeeper.utils.MessageTools;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatTag implements Listener {
    private static final Map<UUID, Integer> tasks = new HashMap<>();
    private static ConfigSetup config = Main.getPlugin().config();

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e){
        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player){
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();

            LocalPlayer localDamager = WorldGuardPlugin.inst().wrapPlayer((Player) e.getDamager());
            Location DamagerLocation = localDamager.getLocation();
            ApplicableRegionSet damagerRegionSet = query.getApplicableRegions(DamagerLocation);

            LocalPlayer localEntity = WorldGuardPlugin.inst().wrapPlayer((Player) e.getEntity());
            Location EntityLocation = localEntity.getLocation();
            ApplicableRegionSet entityRegionSet = query.getApplicableRegions(EntityLocation);

            //only if player is in pvp region or player attacks someone in region
            if (damagerRegionSet.testState(localDamager, Flags.PVP) && entityRegionSet.testState(localEntity, Flags.PVP)){
                combatCountdown((Player)e.getDamager(), Main.getPlugin());
                combatCountdown((Player) e.getEntity(), Main.getPlugin());
            }
        }
    }

    public static Map<UUID, Integer> getTasks(){
        return tasks;
    }

    @EventHandler
    public void playerCommand(PlayerCommandPreprocessEvent e){
        Player player = e.getPlayer();
        if (tasks.containsKey(player.getUniqueId())){

            e.setCancelled(true);
            player.sendMessage(MessageTools.parseFromPath(config, "Command In Combat"));
        }
    }

    @EventHandler
    public void logoutEvent(PlayerQuitEvent e){
        Player player = e.getPlayer();
        if (tasks.containsKey(player.getUniqueId())){
            e.getPlayer().setHealth(0);
            Bukkit.getScheduler().cancelTask(tasks.get(player.getUniqueId()));
            tasks.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void deathEvent(PlayerDeathEvent e){
        Player player = e.getEntity().getPlayer();
        assert player != null;
        if (tasks.containsKey(player.getUniqueId())){
            Bukkit.getScheduler().cancelTask(tasks.get(player.getUniqueId()));
            tasks.remove(player.getUniqueId());
        }
    }

    public static void combatCountdown(Player player, Main plugin){
        if (tasks.containsKey(player.getUniqueId())){
            Bukkit.getScheduler().cancelTask(tasks.get(player.getUniqueId()));
        }

        tasks.put(player.getUniqueId(), Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            final int configTime = Integer.parseInt(MessageTools.getString(config, "Combat Time In Seconds"));
            int time = configTime;

            @Override
            public void run() {
                if (time == 0){
                    player.sendMessage(MessageTools.parseFromPath(config, "Not Combat Tagged"));
                    player.sendActionBar(Component.empty());
                    Bukkit.getScheduler().cancelTask(tasks.get(player.getUniqueId()));
                    tasks.remove(player.getUniqueId());
                } else{
                    final Component message = MessageTools.parseFromPath(config, "Combat Timer", Template.template("Time", String.valueOf(time)));
                    player.sendActionBar(message);
                    time--;
                }
            }
        }, 0, 20));
    }
}
