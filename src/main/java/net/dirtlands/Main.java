package net.dirtlands;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.sk89q.worldguard.WorldGuard;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.commands.tab.PluginTabCompleter;
import net.dirtlands.files.Config;
import net.dirtlands.files.Npcs;
import net.dirtlands.files.Warps;
import net.dirtlands.handler.CombatSafezoneHandler;
import net.dirtlands.listeners.shopkeepers.Shopkeeper;
import net.dirtlands.nms.npc.Npc;
import net.dirtlands.nms.npc.Npc_1_17_R1;
import net.minecraft.server.level.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;

public class Main extends JavaPlugin {

    private static Main plugin;
    private Npc npc;
    private static ProtocolManager protocolManager;

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onEnable(){
        plugin = this;
        startFileSetup();

        if (loadNetMinecraftServer()) {

            String packageName = getClass().getPackage().getName();
            addPacketListeners();

            //load Listeners in net.dirtlands.listeners
            for (Class<?> listenerClass : new Reflections(packageName + ".listeners").getSubTypesOf(Listener.class)) {
                try {
                    Listener listener = (Listener) listenerClass.getDeclaredConstructor().newInstance(); //must have empty constructor
                    getServer().getPluginManager().registerEvents(listener, this);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }


            //load PluginCommands in net.dirtlands.commands
            for (Class<? extends PluginCommand> commandClass : new Reflections(packageName + ".commands").getSubTypesOf(PluginCommand.class)) {
                try {
                    PluginCommand pluginCommand = commandClass.getDeclaredConstructor().newInstance();
                    getCommand(pluginCommand.getName()).setExecutor(pluginCommand);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

            //load PluginTabCompleters in net.dirtlands.commands.tab
            for (Class<? extends PluginTabCompleter> completerClass : new Reflections(packageName + ".commands.tab").getSubTypesOf(PluginTabCompleter.class)) {
                try {
                    PluginTabCompleter tabCompleter = completerClass.getDeclaredConstructor().newInstance();
                    for (String commandName : tabCompleter.getNames()) {
                        getCommand(commandName).setTabCompleter(tabCompleter);
                    }
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }

            }
            var sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
            sessionManager.registerHandler(CombatSafezoneHandler.FACTORY, null);
        } else {
            getLogger().info("Server version is incompatible!");

            Bukkit.getPluginManager().disablePlugin(this);
        }


    }

    @Override
    public void onLoad() {
        protocolManager = ProtocolLibrary.getProtocolManager();
    }

    private boolean loadNetMinecraftServer(){
        String version;

        try{
            version = Bukkit.getServer().getClass().getPackageName().split("\\.")[3];
        } catch (ArrayIndexOutOfBoundsException e){
            return false; //weird version
        }

        getLogger().info("Your server is running version " + version);

        if (version.equals("v1_17_R1")){
            npc = new Npc_1_17_R1();

        } else {
            return false;
        }


        return true;
    }

    public static Main getPlugin(){
        return plugin;
    }

    public Npc getNpc(){
        return npc;
    }
    public ProtocolManager getProtocolManager(){
        return protocolManager;
    }

    private void addPacketListeners(){

        protocolManager.addPacketListener(

                new PacketAdapter(this, PacketType.Play.Client.USE_ENTITY) {

                    @Override
                    public void onPacketReceiving(PacketEvent e) {

                        if (e.getPacket().getEnumEntityUseActions().readSafely(0).getAction().name().equalsIgnoreCase("INTERACT")) {
                            boolean npcFound = false;
                            String npcName = "";
                            for (EntityPlayer npc : Npc.getNpcs()){ // check if npc is registered
                                if (npc.getId() == e.getPacket().getIntegers().read(0)){
                                    npcName = npc.getDisplayName().getText();
                                    npcFound = true;
                                    break;
                                }
                            }
                            if (!npcFound) {
                                return;
                            }

                            Shopkeeper.openMenu(e.getPlayer(), npcName);
                        }


                    }


                }

        );

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

        /*


        npcs.yml


         */

        Npcs.setup();
        Npcs.get().options().header(header);
        Npcs.get().addDefault("Npcs", "");
        Npcs.get().options().copyDefaults(true);
        Npcs.get().options().copyHeader(true);
        Npcs.save();

        //if making another file, add it to /dirtlands reload
    }



}
