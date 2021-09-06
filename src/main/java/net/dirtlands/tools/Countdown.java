package net.dirtlands.tools;

import net.dirtlands.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.HashMap;
import java.util.UUID;

public class Countdown {

    private static final HashMap<UUID, Integer> tasks = new HashMap<>();

    public static HashMap<UUID, Integer> getTasks() {
        return tasks;
    }

    public static void startCountdown(Player player, String coordsLocation, String destination, Main plugin){

        if (tasks.containsKey(player.getUniqueId())){
            Bukkit.getScheduler().cancelTask(tasks.get(player.getUniqueId()));
        }

        tasks.put(player.getUniqueId(), Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            int time = 5;
            @Override
            public void run() {
                if(time == 5){
                    player.sendMessage(ConfigTools.parseFromPath("Dont Move Message"));
                }
                if (time == 0){
                    player.teleport(LocationTools.stringToLocation(Main.getPlugin().warps().get().getString(coordsLocation)));
                    player.sendMessage(ConfigTools.parseFromPath("Teleport Success", Template.of("Location", destination)));
                    Bukkit.getScheduler().cancelTask(tasks.get(player.getUniqueId()));
                    tasks.remove(player.getUniqueId());
                }
                else{
                    final Title.Times times = Title.Times.of(Duration.ofMillis(500), Duration.ofMillis(500), Duration.ofMillis(500));
                    final Title title = Title.title(ConfigTools.parseFromPath("Teleport Countdown", Template.of("Time", String.valueOf(time)),
                            Template.of("Location", destination)), Component.empty(), times);
                    player.showTitle(title);
                    time--;
                }
            }
        }, 0, 20));
    }

}
