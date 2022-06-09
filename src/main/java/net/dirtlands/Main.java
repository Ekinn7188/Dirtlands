package net.dirtlands;

import com.sk89q.worldguard.WorldGuard;
import jeeper.utils.PluginEnable;
import jeeper.utils.config.Config;
import net.dirtlands.commands.PluginCommand;
import net.dirtlands.commands.tab.PluginTabCompleter;
import net.dirtlands.database.SQLite;
import net.dirtlands.handler.CombatSafezoneHandler;
import net.dirtlands.log.LogFilter;
import org.apache.logging.log4j.LogManager;
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
    private Config config;
    private DSLContext dslContext;
    @Override
    public void onEnable(){
        //makes sure the server has all the required plugins. if not, the plugin will disable
        PluginEnable.checkForPluginDependencies(List.of("WorldGuard", "LuckPerms", "Citizens", "Jeeper-Essentials"), "dirtlands");

        org.apache.logging.log4j.core.Logger coreLogger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
        coreLogger.addFilter(new LogFilter());

        Main.initializeClasses();

        var sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
        sessionManager.registerHandler(CombatSafezoneHandler.FACTORY,null);
    }

    @Override
    public void onLoad() {
        plugin = this;
        startFileSetup();
        try {
            dslContext = SQLite.databaseSetup(getPlugin().getDataFolder().getCanonicalPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static Main getPlugin(){
        return plugin;
    }

    public DSLContext getDslContext(){
        return dslContext;
    }
    public Config config() {
        return config;
    }

    private void startFileSetup(){
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        /*


        config.yml


        */

        config = new Config("config", "dirtlands");
        config.readDefaults(this, "config.yml");
        config.get().options().copyDefaults(true);
        //config.get().options().parseComments(true);
        config.save();

        //if making another file, add it to /dirtlands reload
    }


    /**
     * Initializes all Commands in net.dirtlands.commands<br>
     * Initializes all Listeners in net.dirtlands.listeners<br>
     * Initializes all TabCompleters in net.dirtlands.commands.tab<br>
     */
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



}
