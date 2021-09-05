package net.dirtlands;

import com.sk89q.worldguard.WorldGuard;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.commands.tab.PluginTabCompleter;
import net.dirtlands.files.Config;
import net.dirtlands.files.Warps;
import net.dirtlands.handler.CombatSafezoneHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class Main extends JavaPlugin {

    private static Main plugin;

    @Override
    public void onEnable(){
        plugin = this;

        checkForPluginDependencies(List.of("Citizens", "WorldGuard", "LuckPerms", "ProtocolLib"));

        startFileSetup();

        //CitizensAPI.getNPCRegistry();
        //scheduler runs after the first server tick, which makes sure all plugins are fully ready
        Bukkit.getServer().getScheduler().runTask(this, () -> {
            Main.initializeClasses();

            var sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
            sessionManager.registerHandler(CombatSafezoneHandler.FACTORY,null);
        });
    }

    protected static void initializeClasses(){
        String packageName = Main.getPlugin().getClass().getPackage().getName();
        //load Listeners in net.dirtlands.listeners
        for(
                Class<?> listenerClass :new

                Reflections(packageName +".listeners").

                getSubTypesOf(Listener.class))

        {
            try {
                Listener listener = (Listener) listenerClass.getDeclaredConstructor().newInstance(); //must have empty constructor
                Main.getPlugin().getServer().getPluginManager().registerEvents(listener, Main.getPlugin());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }


        //load PluginCommands in net.dirtlands.commands
        for(
                Class<? extends PluginCommand> commandClass :new

                Reflections(packageName +".commands").

                getSubTypesOf(PluginCommand .class))

        {
            try {
                PluginCommand pluginCommand = commandClass.getDeclaredConstructor().newInstance();
                Objects.requireNonNull(Main.getPlugin().getCommand(pluginCommand.getName())).setExecutor(pluginCommand);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        //load PluginTabCompleters in net.dirtlands.commands.tab
        for(
                Class<? extends PluginTabCompleter> completerClass :new

                Reflections(packageName +".commands.tab").

                getSubTypesOf(PluginTabCompleter .class))

        {
            try {
                PluginTabCompleter tabCompleter = completerClass.getDeclaredConstructor().newInstance();
                for (String commandName : tabCompleter.getNames()) {
                    Objects.requireNonNull(Main.getPlugin().getCommand(commandName)).setTabCompleter(tabCompleter);
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }

        }

    }

    public static Main getPlugin(){
        return plugin;
    }

    @SuppressWarnings("ConstantConditions")
    public void checkForPluginDependencies(List<String> pluginNames) {
        for (String plugin : pluginNames){
            if (getServer().getPluginManager().getPlugin(plugin) == null || !getServer().getPluginManager().getPlugin(plugin).isEnabled()) {
                getLogger().log(Level.SEVERE, plugin + " not found or not enabled");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }
    }


    private void startFileSetup(){
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        String header = """
                ############################################################
                # +------------------------------------------------------+ #
                # |                      Dirtlands                       | #
                # +------------------------------------------------------+ #
                ############################################################
                
                Developed by: Jeeper_ (Jeeper#6808)
                
                Color Choices:
                   Hex Code Example: <#FFFFFF>text</#FFFFFF>
                   Make sure to surround the text with its color and make sure it ends with a /!!
                
                   Minecraft Color Code Example: &aI'm typing in red!
                   This website has all the codes! https://www.digminecraft.com/lists/color_list_pc.php
            
                    Extra info on text formatting: https://docs.adventure.kyori.net/minimessage.html#the-components
                
                
                ******* /dirtlands reload to reload files *******
                """;

        /*


        config.yml


        */

        Config.setup();
        Config.get().options().header(header);
        Config.get().addDefault("Chat Style", "<Prefix><#893900><Player></#893900><Suffix> &a\u00BB &7<Message>");
        Config.get().addDefault("Dont Move Message", "&cDont move or teleportation is &4canceled");
        Config.get().addDefault("Teleport Countdown", "&cTeleporting to <Location> in: &4<Time>");
        Config.get().addDefault("Spawn Set", "&aSpawn point set!");
        Config.get().addDefault("Teleport Canceled", "&cTeleport &4canceled&c!");
        Config.get().addDefault("Teleport Success", "&aSuccessfully teleported to <Location>!");
        Config.get().addDefault("Dirtlands Reloaded", "&aDirtlands Reloaded!");
        Config.get().addDefault("Nickname Change", "&aYour nickname has been set to <Name>!");
        Config.get().addDefault("Warp Created", "&a/warp <Name> was successfully created!");
        Config.get().addDefault("Warp Deleted", "&a/warp <Name> was successfully deleted!");
        Config.get().addDefault("Warp Doesnt Exist", "&a/warp <Name> does not exist!");
        Config.get().addDefault("Warp List", "&aYour current warps: <Warps>");
        Config.get().addDefault("No Warps", "&cThere are no warps!");
        Config.get().addDefault("Home Created", "&a/home <Name> was successfully created!");
        Config.get().addDefault("Home Deleted", "&a/home <Name> was successfully deleted!");
        Config.get().addDefault("Home Doesnt Exist", "&a/home <Name> does not exist!");
        Config.get().addDefault("Too Many Homes", "&cYou've created too many homes! You can make up to &4<Number>");
        Config.get().addDefault("Home List", "&aYour current homes: <Homes>");
        Config.get().addDefault("No Homes", "&cYou dont have any homes! Create one with /sethome {name}!"); //the brackets are intentional, there's no Template.of for it
        Config.get().addDefault("Combat Time In Seconds", "10");
        Config.get().addDefault("Combat Timer", "&cYou will untagged in &4<Time>&c seconds");
        Config.get().addDefault("Not Combat Tagged", "&cYou're no longer combat tagged");
        Config.get().addDefault("Command In Combat", "&cYou cant send commands when you're combat tagged!");
        Config.get().addDefault("Enter Safezone In Combat Title", "&4No Entry");
        Config.get().addDefault("Enter Safezone In Combat Subtitle", "&cYou cant enter safezones while in combat!");
        Config.get().addDefault("Join Message", "&7[&2+&7] <Player>");
        Config.get().addDefault("First Join Message", "&6<Player> has joined for the first time! (#<Number>)");
        Config.get().addDefault("Leave Message", "&7[&4-&7] <Player>");
        Config.get().addDefault("Broadcast Prefix", "&a[<#893900>Dirtlands</#893900>&a] ");
        Config.get().addDefault("No Command Permission", "&cYou dont have permission to use this command!");
        Config.get().addDefault("Player Only Command", "&cYou must be a player to execute this command!");
        Config.get().addDefault("Player Death", "&c\u2620 {Message}");
        Config.get().addDefault("Chat Cleared By Message", "&c&lChat cleared by <Player>!");
        Config.get().addDefault("Chat Muted By Message", "&c&lChat muted by <Player>!");
        Config.get().addDefault("Chat Unmuted By Message", "&c&lChat unmuted by <Player>!");
        Config.get().addDefault("Chat Is Muted", "&cThe chat is currently &4muted&c!");
        Config.get().addDefault("Npc Selected", "<Name>&a has been selected!");
        Config.get().addDefault("No Npcs", "&cYou need to create an npc first!");
        Config.get().addDefault("Player Doesnt Exist", "&4<Player>&c doesn't exist!");

        Config.get().options().copyDefaults(true);
        Config.get().options().copyHeader(true);
        Config.save();

        /*


        warps.yml


         */


        Warps.setup();
        Warps.get().options().header(header);
        Warps.get().addDefault("Spawn.Coords", "");
        Warps.get().addDefault("Warps", "");
        Warps.get().addDefault("Homes", "");
        Warps.get().options().copyDefaults(true);
        Warps.get().options().copyHeader(true);
        Warps.save();

        //if making another file, add it to /dirtlands reload
    }



}
