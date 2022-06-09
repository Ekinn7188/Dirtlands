package net.dirtlands.listeners;


import com.destroystokyo.paper.event.block.TNTPrimeEvent;
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
import net.citizensnpcs.api.CitizensAPI;
import net.dirtlands.Main;
import net.dirtlands.commands.Permission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatTag implements Listener {
    private static final Map<UUID, Integer> tasks = new HashMap<>();
    private static final ConfigSetup config = Main.getPlugin().config();

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e){
        //only players attacking each other, no npcs
        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player && !(CitizensAPI.getNPCRegistry().isNPC(e.getDamager())) && !(CitizensAPI.getNPCRegistry().isNPC(e.getEntity()))){
            if (e.getEntity().hasMetadata("NPC")) {
                return;
            }

            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();

            LocalPlayer localDamager = WorldGuardPlugin.inst().wrapPlayer((Player) e.getDamager());
            Location DamagerLocation = localDamager.getLocation();
            ApplicableRegionSet damagerRegionSet = query.getApplicableRegions(DamagerLocation);

            LocalPlayer localEntity = WorldGuardPlugin.inst().wrapPlayer((Player) e.getEntity());
            Location EntityLocation = localEntity.getLocation();
            ApplicableRegionSet entityRegionSet = query.getApplicableRegions(EntityLocation);

            //only if player is in pvp region or player attacks someone in region
            if (damagerRegionSet.testState(localDamager, Flags.PVP) && entityRegionSet.testState(localEntity, Flags.PVP)) {
                combatCountdown((Player) e.getDamager(), Main.getPlugin());
                combatCountdown((Player) e.getEntity(), Main.getPlugin());
            }
        } else if ((e.getDamager() instanceof EnderCrystal || e.getEntity() instanceof EnderCrystal) && (e.getEntity() instanceof Player || e.getDamager() instanceof Player)){
            combatCountdown(e.getDamager() instanceof Player ? (Player) e.getDamager() : (Player) e.getEntity(), Main.getPlugin());
        }
        if (e.getEntity() instanceof Player) {
            if (e.getCause().equals(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)) {
                if (e.getEntity() instanceof Player) {
                    combatCountdown((Player) e.getEntity(), Main.getPlugin());
                }
            }
            else if (e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
                if (e.getDamager().getType().equals(EntityType.PRIMED_TNT)) {
                    combatCountdown((Player) e.getEntity(), Main.getPlugin());
                }
            }
        }

    }

    public static Map<UUID, Integer> getTasks(){
        return tasks;
    }

    @EventHandler
    public void playerCommand(PlayerCommandPreprocessEvent e){
        Player player = e.getPlayer();
        if (tasks.containsKey(player.getUniqueId()) && !player.hasPermission(Permission.BYPASS_COMBAT.getName())){
            e.setCancelled(true);
            player.sendMessage(MessageTools.parseFromPath(config, "Command In Combat"));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getHand() == null || e.getHand().equals(EquipmentSlot.OFF_HAND) || e.getClickedBlock() == null) {
            return;
        }

        World.Environment environment = e.getPlayer().getWorld().getEnvironment();
        if (e.getClickedBlock().getType().equals(Material.RESPAWN_ANCHOR) && e.getClickedBlock().getBlockData().equals(Bukkit.createBlockData("minecraft:respawn_anchor[charges=4]"))) {
            if (environment.equals(World.Environment.NORMAL) || environment.equals(World.Environment.CUSTOM)) {
                combatCountdown(e.getPlayer(), Main.getPlugin());
            }
        }
        else if (e.getClickedBlock().getBlockData() instanceof Bed) {
            if ((environment.equals(World.Environment.NETHER) || environment.equals(World.Environment.THE_END))) {
                combatCountdown(e.getPlayer(), Main.getPlugin());
            }
        }
    }

    @EventHandler
    public void onTNTPrime(TNTPrimeEvent e) {
        if (!(e.getPrimerEntity() instanceof Player)) {
            return;
        }
        combatCountdown((Player) e.getPrimerEntity(), Main.getPlugin());
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
                    final Component message = MessageTools.parseFromPath(config, "Combat Timer", Template.template("time", String.valueOf(time)));
                    player.sendActionBar(message);
                    time--;
                }
            }
        }, 0, 20));
    }
}
