package net.dirtlands.files;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Playerdata {

    private final File file;
    private FileConfiguration config;

    //finds or generates custom config
    public Playerdata(){
        file = new File(Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("Dirtlands")).getDataFolder(), "playerdata.yml");

        if (!file.exists()){
            try{
                if (file.createNewFile()){
                    Bukkit.broadcast(Component.text()
                            .content("File \"playerdata.yml\" Created Successfully!")
                            .color(NamedTextColor.GREEN).build());
                }
            } catch(IOException e){
                Bukkit.broadcast(Component.text()
                        .content("IO Exception in file Playerdata.java. Error details are in the logs.")
                        .color(NamedTextColor.RED).build());
                Bukkit.getLogger().info(e.toString());
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration get(){
        return config;
    }

    public void save(){
        try{
            config.save(file);
        } catch (IOException e) {
            Bukkit.broadcast(Component.text()
                    .content("IO Exception in file Playerdata.java. Couldn't save file. Error details are in the logs.")
                    .color(NamedTextColor.RED).build());
            Bukkit.getLogger().info(e.toString());
        }
    }

    public void reload(){
        config = YamlConfiguration.loadConfiguration(file);
    }

}
