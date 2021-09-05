package net.dirtlands.files;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Npcs {

    private static File file;
    private static FileConfiguration config;

    //finds or generates npc.yml file
    public static void setup(){
        file = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Dirtlands")).getDataFolder(), "npcs.yml");

        if (!file.exists()){
            try{
                if (file.createNewFile()){
                    Bukkit.broadcast(Component.text()
                            .content("File \"npcs.yml\" Created Successfully!")
                            .color(NamedTextColor.GREEN).build());
                }
            } catch(IOException e){
                Bukkit.broadcast(Component.text()
                        .content("IO Exception in file Npcs.java. Error details are in the logs.")
                        .color(NamedTextColor.RED).build());
                Bukkit.getLogger().info(e.toString());
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration get(){
        return config;
    }

    public static void save(){
        try{
            config.save(file);
        } catch (IOException e) {
            Bukkit.broadcast(Component.text()
                    .content("IO Exception in file Npcs.java. Couldn't save file. Error details are in the logs.")
                    .color(NamedTextColor.RED).build());
            Bukkit.getLogger().info(e.toString());
        }
    }

    public static void reload(){
        config = YamlConfiguration.loadConfiguration(file);
    }

}
