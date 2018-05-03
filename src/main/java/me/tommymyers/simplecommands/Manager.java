/*
 * Copyright (C) 2018 Tommy Myers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.tommymyers.simplecommands;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;

public class Manager {
    private SimpleCommandMap bukkitCommandMap;
    private final Map<Command, Tuple> commandHandlers = new HashMap<>();
    private String defaultPermissionMessage;
    private Plugin plugin;

    public Manager(Plugin plugin) {
        this.plugin = plugin;
        try {
            Class craftServerClass = Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".CraftServer");
            Object craftServer = Bukkit.getServer();
            Field map = craftServerClass.getDeclaredField("commandMap");
            map.setAccessible(true);
            bukkitCommandMap = (SimpleCommandMap) map.get(craftServer);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to get the Bukkit Command Map. Everyone panic!", ex);
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    public boolean executeCommand(CommandSender sender, Command command, String label, String[] args) {
        Tuple tuple = commandHandlers.get(command);
        Method method = tuple.method;
        if (method != null) {
            try {
                method.setAccessible(true);
                return (boolean) method.invoke(tuple.listener, sender, command, label, args);
            } catch (IllegalAccessException | IllegalArgumentException | SecurityException | InvocationTargetException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to invoke the command handler", ex);
            }
        }
        return false;
    }

    private void registerCommandHandlers(CommandListener listener) throws Exception {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            CommandHandler commandDefinition = (CommandHandler) method.getAnnotation(CommandHandler.class);
            if (commandDefinition != null) {
                if (method.getReturnType().equals(boolean.class)) {
                    Class<?>[] pars = method.getParameterTypes();
                    if (pars.length == 4) {
                        if (pars[0].equals(CommandSender.class) && pars[1].equals(Command.class)
                                && pars[2].equals(String.class) && pars[3].equals(String[].class)) {
                            String name = commandDefinition.name();
                            String description = commandDefinition.description();
                            String usage = commandDefinition.usage();
                            String permission = commandDefinition.permission();
                            String permissionMessage = commandDefinition.noPermissionMessage();
                            String[] aliases = commandDefinition.aliases();

                            Class pluginCommandClass = PluginCommand.class;
                            Constructor cons = pluginCommandClass.getDeclaredConstructor(String.class, Plugin.class);
                            cons.setAccessible(true);
                            PluginCommand command = (PluginCommand) cons.newInstance(name, plugin);
                            command.setUsage(usage);
                            if (usage.trim().isEmpty()) {
                                command.setUsage(name);
                            }
                            command.setDescription(description);
                            command.setAliases(Arrays.asList(aliases));
                            command.setPermission(permission);
                            command.setPermissionMessage(permissionMessage);
                            if (getDefaultPermissionMessage() != null) {
                                command.setPermissionMessage(getDefaultPermissionMessage());
                            }
                            command.setExecutor(plugin);
                            Tuple tuple = new Tuple(listener, method);
                            commandHandlers.put(command, tuple);
                            bukkitCommandMap.register(plugin.getName(), command);
                        }
                    }
                }
            }
        }
    }

    public void registerCommandListener(CommandListener listener) {
        try {
            registerCommandHandlers(listener);
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to register command handlers", ex);
        }
    }

    public void setDefaultPermissionMessage(String permissionMessage) {
        this.defaultPermissionMessage = permissionMessage;
    }

    public String getDefaultPermissionMessage() {
        return this.defaultPermissionMessage;
    }

    private class Tuple {

        protected CommandListener listener;
        protected Method method;

        protected Tuple(CommandListener listener, Method method) {
            this.listener = listener;
            this.method = method;
        }
    }
    
}
