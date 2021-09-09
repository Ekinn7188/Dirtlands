package net.dirtlands;

import com.sk89q.worldguard.WorldGuard;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.commands.tab.PluginTabCompleter;
import net.dirtlands.files.Config;
import net.dirtlands.files.NpcInventory;
import net.dirtlands.files.Playerdata;
import net.dirtlands.files.Warps;
import net.dirtlands.handler.CombatSafezoneHandler;
import net.dirtlands.tabscoreboard.TabMenu;
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
    private NpcInventory npcInventory;
    private Warps warps;
    private Config config;
    private Playerdata playerData;

    @Override
    public void onEnable(){
        plugin = this;

        checkForPluginDependencies(List.of("Citizens", "WorldGuard", "LuckPerms", "ProtocolLib"));

        startFileSetup();
        Main.initializeClasses();

        TabMenu.updateTabLoop();


        var sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
        sessionManager.registerHandler(CombatSafezoneHandler.FACTORY,null);
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
        for(Class<? extends PluginCommand> commandClass :new Reflections(packageName +".commands").getSubTypesOf(PluginCommand .class))

        {
            try {
                PluginCommand pluginCommand = commandClass.getDeclaredConstructor().newInstance();
                Objects.requireNonNull(Main.getPlugin().getCommand(pluginCommand.getName())).setExecutor(pluginCommand);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        //load PluginTabCompleters in net.dirtlands.commands.tab
        for(Class<? extends PluginTabCompleter> completerClass :new Reflections(packageName +".commands.tab").getSubTypesOf(PluginTabCompleter .class)) {
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

    /*private boolean setupNMS() { //nms with interfaces
        String version;

        try {
            version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();//weird version
            return false;
        }
        if (version.equals("v1_17_R1")) {
            className = new ClassName_1_17_R1();

        }

        return className != null;
    }*/

    public static Main getPlugin(){
        return plugin;
    }

    public NpcInventory npcInventory() {
        return npcInventory;
    }
    public Playerdata playerdata() {
        return playerData;
    }
    public Warps warps() {
        return warps;
    }
    public Config config() {
        return config;
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

        config = new Config();
        config.get().options().header(header);
        config.get().addDefault("Chat Style", "<Prefix><#893900><Player></#893900><Suffix> &a\u00BB &7<Message>");
        config.get().addDefault("Dont Move Message", "&cDont move or teleportation is &4canceled");
        config.get().addDefault("Teleport Countdown", "&cTeleporting to <Location> in: &4<Time>");
        config.get().addDefault("Spawn Set", "&aSpawn point set!");
        config.get().addDefault("Teleport Canceled", "&cTeleport &4canceled&c!");
        config.get().addDefault("Teleport Success", "&aSuccessfully teleported to <Location>!");
        config.get().addDefault("Dirtlands Reloaded", "&aDirtlands Reloaded!");
        config.get().addDefault("Nickname Change", "&aYour nickname has been set to <Name>!");
        config.get().addDefault("Warp Created", "&a/warp <Name> was successfully created!");
        config.get().addDefault("Warp Deleted", "&a/warp <Name> was successfully deleted!");
        config.get().addDefault("Warp Doesnt Exist", "&a/warp <Name> does not exist!");
        config.get().addDefault("Warp List", "&aYour current warps: <Warps>");
        config.get().addDefault("No Warps", "&cThere are no warps!");
        config.get().addDefault("Home Created", "&a/home <Name> was successfully created!");
        config.get().addDefault("Home Deleted", "&a/home <Name> was successfully deleted!");
        config.get().addDefault("Home Doesnt Exist", "&a/home <Name> does not exist!");
        config.get().addDefault("Too Many Homes", "&cYou've created too many homes! You can make up to &4<Number>");
        config.get().addDefault("Home List", "&aYour current homes: <Homes>");
        config.get().addDefault("No Homes", "&cYou dont have any homes! Create one with /sethome {name}!"); //the brackets are intentional, there's no Template.of for it
        config.get().addDefault("Combat Time In Seconds", "10");
        config.get().addDefault("Combat Timer", "&cYou will untagged in &4<Time>&c seconds");
        config.get().addDefault("Not Combat Tagged", "&cYou're no longer combat tagged");
        config.get().addDefault("Command In Combat", "&cYou cant send commands when you're combat tagged!");
        config.get().addDefault("Enter Safezone In Combat Title", "&4No Entry");
        config.get().addDefault("Enter Safezone In Combat Subtitle", "&cYou cant enter safezones while in combat!");
        config.get().addDefault("Join Message", "&7[&2+&7] <Player>");
        config.get().addDefault("First Join Message", "&6<Player> has joined for the first time! (#<Number>)");
        config.get().addDefault("Leave Message", "&7[&4-&7] <Player>");
        config.get().addDefault("Broadcast Prefix", "&a[<#893900>Dirtlands</#893900>&a] ");
        config.get().addDefault("No Command Permission", "&cYou dont have permission to use this command!");
        config.get().addDefault("Player Only Command", "&cYou must be a player to execute this command!");
        config.get().addDefault("Player Death", "&c\u2620 <Message>");
        config.get().addDefault("Chat Cleared By Message", "&c&lChat cleared by <Player>!");
        config.get().addDefault("Chat Muted By Message", "&c&lChat muted by <Player>!");
        config.get().addDefault("Chat Unmuted By Message", "&c&lChat unmuted by <Player>!");
        config.get().addDefault("Chat Is Muted", "&cThe chat is currently &4muted&c!");
        config.get().addDefault("Npc Selected", "<Name>&a has been selected!");
        config.get().addDefault("No Npcs", "&cYou need to create an npc first!");
        config.get().addDefault("Player Doesnt Exist", "&4<Player>&c doesn't exist!");
        config.get().addDefault("Tablist Header", List.of("<#D1C59F>-----------------<#893900>Dirtlands<#D1C59F>-----------------", "").toArray());
        config.get().addDefault("Tablist Footer", List.of(" ", "<#D1C59F> <OnlinePlayers>/" + Bukkit.getServer().getMaxPlayers() + " Online").toArray());
        config.get().addDefault("Chat Color Set", "&aDefault chat color set");
        config.get().addDefault("Invalid Chat Color", "&cThat is not a valid chat color!");
        config.get().addDefault("Money Gained Actionbar", "&3+<money> &bExpensive Diamonds &o(You Have &3<balance>&b)");
        config.get().addDefault("Money Set Actionbar", "&bYour Balance Has Been Set To &3<money>");
        config.get().addDefault("Money Lost Actionbar", "&3-<money> &bExpensive Diamonds &o(You Have &3<balance>&b)");
        config.get().addDefault("Cant Afford Message", "&cYou can't afford this!");
        config.get().addDefault("Player Doesnt Have Enough Money", "&c<player> doesn't have enough money for this action");
        config.get().addDefault("Player Balance", "&3<player>&b has &3<balance> expensive diamonds");


        config.get().options().copyDefaults(true);
        config.get().options().copyHeader(true);
        config.save();

        /*


        warps.yml


         */


        warps = new Warps();
        warps.get().options().header(header);
        warps.get().addDefault("Spawn.Coords", "");
        warps.get().addDefault("Warps", "");
        warps.get().addDefault("Homes", "");
        warps.get().options().copyDefaults(true);
        warps.get().options().copyHeader(true);
        warps.save();

        /*

        shopkeeper.yml

         */

        npcInventory = new NpcInventory();
        npcInventory.get().options().header(header + "\n\ncheck https://github.com/Ekinn7188/Dirtlands/blob/master/src/main/resources/shopkeeper.yml for an example shopkeeper setup");
        npcInventory.get().options().copyHeader(true);
        npcInventory.get().options().copyDefaults(false);
        npcInventory.save();

        /*

        playerdata.yml

         */

        playerData = new Playerdata();
        playerData.get().options().copyHeader(false);
        playerData.get().options().copyDefaults(false);
        playerData.save();

        //if making another file, add it to /dirtlands reload
    }



}
