package net.dirtlands;

import com.sk89q.worldguard.WorldGuard;
import jeeper.utils.PluginEnable;
import jeeper.utils.config.ConfigSetup;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.commands.tab.PluginTabCompleter;
import net.dirtlands.database.SQLite;
import net.dirtlands.files.NpcInventory;
import net.dirtlands.handler.CombatSafezoneHandler;
import net.dirtlands.tabscoreboard.TabMenu;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jooq.DSLContext;
import org.reflections.Reflections;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;

public class Main extends JavaPlugin {

    private static Main plugin;
    private NpcInventory npcInventory;
    private ConfigSetup config;
    private DSLContext dslContext;

    @Override
    public void onEnable(){
        plugin = this;

        PluginEnable.checkForPluginDependencies(List.of("Citizens", "WorldGuard", "LuckPerms", "ProtocolLib"), "dirtlands");

        startFileSetup();

        try {
            dslContext = SQLite.databaseSetup(getPlugin().getDataFolder().getCanonicalPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Main.initializeClasses();

        TabMenu.updateTabLoop();


        var sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
        sessionManager.registerHandler(CombatSafezoneHandler.FACTORY,null);
    }

    protected static void initializeClasses(){
        String packageName = Main.getPlugin().getClass().getPackage().getName();
        //load Listeners in net.dirtlands.listeners
        for(Class<?> listenerClass :new Reflections(packageName +".listeners").getSubTypesOf(Listener.class)) {
            try {
                Listener listener = (Listener) listenerClass.getDeclaredConstructor().newInstance(); //must have empty constructor
                Main.getPlugin().getServer().getPluginManager().registerEvents(listener, Main.getPlugin());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }


        //load PluginCommands in net.dirtlands.commands
        for(Class<? extends PluginCommand> commandClass :new Reflections(packageName +".commands").getSubTypesOf(PluginCommand.class)) {
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

    /*
     private boolean setupNMS() { //nms with interfaces
        String version = PluginEnable.getServerVersion();

        if (version.equals("")) {
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
    /**
     * @return the DSLContext, used to contact the SQLite database
     */
    public DSLContext getDslContext(){
        return dslContext;
    }
    public NpcInventory npcInventory() {
        return npcInventory;
    }
    public ConfigSetup config() {
        return config;
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

        config = new ConfigSetup("config", "dirtlands");
        config.get().options().header(header);
        config.readDefaults(this, "config.yml");
        config.get().options().copyDefaults(true);
        config.get().options().copyHeader(true);
        config.save();


        /*

        shopkeeper.yml

         */

        npcInventory = new NpcInventory();
        npcInventory.get().options().header(header + "\n\ncheck https://github.com/Ekinn7188/Dirtlands/blob/master/src/main/resources/shopkeeper.yml for an example shopkeeper setup");
        npcInventory.get().options().copyHeader(true);
        npcInventory.get().options().copyDefaults(false);
        npcInventory.save();


        //if making another file, add it to /dirtlands reload
    }



}
