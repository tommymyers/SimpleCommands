package me.tommymyers.utils.simplecommands;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
//import org.bukkit.craftbukkit.v1_6_R3.CraftServer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleCommands extends JavaPlugin {

    private PluginDescriptionFile pdf;
    private String pluginName;
    private String pluginVersion;
    private String pluginAuthor;
    private String pluginDescription;
    private static final Logger LOGGER = Logger.getLogger("Minecraft");
    private static Manager manager;

    /**
     * When the SimpleCommands API and plugin is enabled.
     */
    @Override
    public void onEnable() {
        /**
         * START INITIALIZING VARIABLES
         */
        pdf = getDescription();
        pluginName = pdf.getName();
        pluginVersion = pdf.getVersion();
        pluginAuthor = pdf.getAuthors().get(0);
        pluginDescription = pdf.getDescription();
        manager = new Manager(this);
        /**
         * END INITIALIZATION
         */

        SimpleCommands.getManager().setPluginDefaultNoPermissionMessage(this, "\u00A7cYou do not have access to that command!");
        SimpleCommands.getManager().registerCommandListener(this, new CommandListener() {
            @CommandHandler(name = "simplecommands", description = "Displays information about the installed version of SimpleCommands.",
                    aliases = {"sc"}, usage = "simplecommands", permission = "simplecommands.simplecommands")
            public boolean myCommand(CommandSender sender, Command command, String label, String[] args) {
                sender.sendMessage("This server has " + pluginName + " v" + pluginVersion + ".");
                sender.sendMessage(pluginDescription);
                sender.sendMessage("Author: " + pluginAuthor);
                return true;
            }
        });
    }

    /**
     * When the SimpleCommands API and plugin is disabled.
     */
    @Override
    public void onDisable() { //WHEN THE PLUGIN IS DISABLED

    }

    /**
     * Logs a message to the console with the plugin/API name
     *
     * @param message Message to be logged to the console.
     */
    public void logMessage(String message) {
        LOGGER.log(Level.INFO, "[" + pluginName + "] {0}", message);
    }

    /**
     * Returns the SimpleCommands manager used to register and set command listeners and other values.
     *
     * @return SimpleCommands Manager
     */
    public static Manager getManager() {
        return manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return manager.executeCommand(sender, command, label, args);
    }

    /**
     * The SimpleCommands API Manager
     */
    public class Manager {

        private final SimpleCommands plugin;
        private final Map<Plugin, Set<CommandListener>> registeredListeners = new HashMap<Plugin, Set<CommandListener>>();
        private final Map<Plugin, String> pluginDefaultNoPermissionMessages = new HashMap<Plugin, String>();
        private final Set<PluginCommand> commands = new HashSet<PluginCommand>();

        /**
         * This constructor should be called only once in the main class.
         *
         * @param plugin The SimpleCommands main class
         */
        private Manager(SimpleCommands plugin) {
            this.plugin = plugin;
        }

        /**
         * This method sets a default 'no permission message' to be displayed to the user who does not have permission to use the command. (This is
         * for only the specified plugin)
         *
         * @param plugin The Plugin to set the value for
         * @param noPermissionMessage The no permission message to set
         */
        public void setPluginDefaultNoPermissionMessage(Plugin plugin, String noPermissionMessage) {
            pluginDefaultNoPermissionMessages.remove(plugin);
            pluginDefaultNoPermissionMessages.put(plugin, noPermissionMessage);
        }

        /**
         * Gets the default no permission message for the specified plugin... I really should stop documenting methods which will probably not even be
         * used externally :P
         *
         * @param plugin The plugin to get the 'no permission message'
         * @return The plugin default 'no permission message'
         */
        private String getPluginDefaultNoPermissionMessage(Plugin plugin) {
            return pluginDefaultNoPermissionMessages.get(plugin);
        }

        /**
         * Self explanatory *sigh
         *
         *
         * @param plugin
         */
        private boolean doesPluginHaveDefaultNoPermissionMessage(Plugin plugin) {
            return pluginDefaultNoPermissionMessages.containsKey(plugin);
        }

        /**
         * I might as well document for these next ones because their cool.
         *
         * @param sender The player (or console) who executed the command
         * @param command The command which was executed
         * @param label The alias used (can be just the command name).
         * @param args The given arguments.
         * @return
         */
        public boolean executeCommand(CommandSender sender, Command command, String label, String[] args) {
            boolean exc = false;
            fullCheck:
            for (Plugin plugins : getRegisteredListeners().keySet()) {
                Set<CommandListener> listeners = getRegisteredListeners().get(plugins);
                for (CommandListener listener : listeners) {
                    Class list = listener.getClass();
                    Method[] methods = list.getDeclaredMethods();
                    methodcheck:
                    for (Method method : methods) {
                        if (method.getModifiers() != Modifier.PUBLIC) {
                            continue;
                        }
                        boolean isBoolean = method.getReturnType().equals(boolean.class);
                        if (method.getReturnType().equals(void.class) || isBoolean) {
                            Annotation[] annots = method.getDeclaredAnnotations();
                            for (Annotation annot : annots) {
                                if (annot instanceof CommandHandler) {
                                    CommandHandler handle = (CommandHandler) annot;
                                    if (handle.name() != null) {
                                        Class<?>[] pars = method.getParameterTypes();
                                        if (pars.length >= 3 && pars.length <= 4) {
                                            boolean isFormattedCorrectly = ((pars[0].equals(CommandSender.class) && pars[1].equals(String.class)
                                                    && pars[2].equals(String[].class)) && pars.length == 3) || ((pars[0].equals(CommandSender.class)
                                                    && pars[1].equals(Command.class) && pars[2].equals(String.class)
                                                    && pars[3].equals(String[].class)) && pars.length == 4);
                                            boolean doesHaveCommandArgument = pars.length == 4;
                                            if (!isFormattedCorrectly) {
                                                continue methodcheck;
                                            }
                                            String name = handle.name();
                                            String usage = handle.usage();
                                            String description = handle.description();
                                            List<String> aliases = Arrays.asList(handle.aliases());
                                            String permission = handle.permission();
                                            String noPermissionMessage = handle.noPermissionMessage();
                                            if (doesPluginHaveDefaultNoPermissionMessage(plugins)) {
                                                noPermissionMessage = getPluginDefaultNoPermissionMessage(plugins);
                                            }
                                            if (command.getName().equals(name) && command.getUsage().equals(usage)
                                                    && command.getDescription().equals(description)
                                                    && command.getAliases().equals(aliases)
                                                    && command.getPermission().equals(permission)
                                                    && command.getPermissionMessage().equals(noPermissionMessage)) {
                                                try {
                                                    if (isBoolean) {
                                                        if (doesHaveCommandArgument) {
                                                            exc = (Boolean) method.invoke(listener, sender,
                                                                    command, label, args);
                                                        } else {
                                                            exc = (Boolean) method.invoke(listener, sender,
                                                                    label, args);
                                                        }
                                                    } else {
                                                        if (doesHaveCommandArgument) {
                                                            method.invoke(listener, sender,
                                                                    command, label, args);
                                                        } else {
                                                            method.invoke(listener, sender,
                                                                    label, args);
                                                        }
                                                        exc = true;
                                                    }
                                                    break fullCheck;
                                                } catch (IllegalAccessException ex) {
                                                    Logger.getLogger(SimpleCommands.class.getName()).log(Level.SEVERE, null, ex);
                                                } catch (IllegalArgumentException ex) {
                                                    Logger.getLogger(SimpleCommands.class.getName()).log(Level.SEVERE, null, ex);
                                                } catch (InvocationTargetException ex) {
                                                    Logger.getLogger(SimpleCommands.class.getName()).log(Level.SEVERE, null, ex);
                                                }

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return exc;
        }

        /**
         * Gets all the registered listeners.
         *
         * @return The map containing all registered listeners and the plugins linked with it.
         */
        private Map<Plugin, Set<CommandListener>> getRegisteredListeners() {
            return registeredListeners;
        }

        /**
         * Reads and registers all the commands from all the other plugin's command listeners.
         */
        private void loadAndRegisterCommandsForPluginAndListener(Plugin plugin, CommandListener listener) throws Exception {
            commands.clear();
            Plugin plugins = plugin;
            String name;
            String description;
            String usage;
            String permission;
            String noPermissionMessage;
            List<String> aliases;
            Class craftServerClass = Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".CraftServer");
            Object craftServer = Bukkit.getServer();
            Field map = craftServerClass.getDeclaredField("commandMap");
            map.setAccessible(true);
            SimpleCommandMap cmdMap = (SimpleCommandMap) map.get(craftServer);
            Class cmdClass = PluginCommand.class;
            Constructor cons = cmdClass.getDeclaredConstructor(String.class, Plugin.class);
            cons.setAccessible(true);
            Class list = listener.getClass();
            Method[] methods = list.getDeclaredMethods();
            methodcheck:
            for (Method method : methods) {
                if (method.getModifiers() != Modifier.PUBLIC) {
                    continue;
                }
                boolean isBoolean = method.getReturnType().equals(boolean.class);
                if (method.getReturnType().equals(void.class) || isBoolean) {
                    Annotation[] annots = method.getDeclaredAnnotations();
                    for (Annotation annot : annots) {
                        if (annot instanceof CommandHandler) {
                            CommandHandler handle = (CommandHandler) annot;
                            if (handle.name() != null) {
                                Class<?>[] pars = method.getParameterTypes();
                                if (pars.length >= 3 && pars.length <= 4) {
                                    boolean isFormattedCorrectly = ((pars[0].equals(CommandSender.class) && pars[1].equals(String.class)
                                            && pars[2].equals(String[].class)) && pars.length == 3) || ((pars[0].equals(CommandSender.class)
                                            && pars[1].equals(Command.class) && pars[2].equals(String.class)
                                            && pars[3].equals(String[].class)) && pars.length == 4);
                                    if (!isFormattedCorrectly) {
                                        continue methodcheck;
                                    }
                                    name = handle.name();
                                    usage = handle.usage();
                                    description = handle.description();
                                    aliases = Arrays.asList(handle.aliases());
                                    permission = handle.permission();
                                    noPermissionMessage = handle.noPermissionMessage();
                                    if (doesPluginHaveDefaultNoPermissionMessage(plugins)) {
                                        noPermissionMessage = getPluginDefaultNoPermissionMessage(plugins);
                                    }
                                    PluginCommand command = (PluginCommand) cons.newInstance(name, plugins);
                                    command.setUsage(usage);
                                    command.setDescription(description);
                                    command.setAliases(aliases);
                                    command.setPermission(permission);
                                    command.setPermissionMessage(noPermissionMessage);
                                    command.setExecutor(plugins);
                                    cmdMap.register("", command);
                                    commands.add(command);
                                }
                            }
                        }
                    }
                }
            }
        }

        /**
         * Registers the command listener for your plugin so SimpleCommands recognizes it.
         *
         * @param plugin Your plugin.
         * @param listener The listener to register.
         */
        public void registerCommandListener(Plugin plugin, CommandListener listener) {
            Set<CommandListener> listeners = new HashSet<CommandListener>();
            if (doesPluginHaveListeners(plugin)) {
                listeners = getPluginListeners(plugin);
            }
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
            removeAllPluginListeners(plugin);
            registeredListeners.put(plugin, listeners);
            try {
                loadAndRegisterCommandsForPluginAndListener(plugin, listener);
            } catch (Exception ex) {
                Logger.getLogger(SimpleCommands.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        /**
         * Removes all plugin listeners for the given plugin.
         *
         * @param plugin The plugin to remove all registered listeners from.
         */
        private void removeAllPluginListeners(Plugin plugin) {
            registeredListeners.remove(plugin);
        }

        /**
         * Checks whether a plugin has any listeners.
         *
         * @param plugin The plugin to check for.
         * @return Whether a plugin has any listeners at all.
         */
        private boolean doesPluginHaveListeners(Plugin plugin) {
            return registeredListeners.containsKey(plugin);
        }

        /**
         * Gets all the command listeners for the specified plugin
         *
         * @param plugin The plugin to get all listeners for.
         * @return All the command listeners for the given plugin.
         */
        private Set<CommandListener> getPluginListeners(Plugin plugin) {
            return registeredListeners.get(plugin);
        }
    }
}
