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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleCommands extends JavaPlugin {

    private PluginDescriptionFile pdf;
    private String pluginName;
    private String pluginVersion;
    private String pluginAuthor;
    private String pluginDescription;
    private static final Logger LOGGER = Logger.getLogger("Minecraft");
    private Manager manager;

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

        manager.registerCommandListener(new CommandListener() {
            @CommandHandler(name = "simplecommands", description = "Displays information about the installed version of SimpleCommands.",
                    aliases = {"sc"}, usage = "simplecommands", permission = "simplecommands.simplecommands")
            public boolean myCommand(CommandSender sender, Command command, String label, String[] args) {
                sender.sendMessage(ChatColor.YELLOW + "This server has " + ChatColor.GREEN + pluginName + " v" + pluginVersion);
                sender.sendMessage(ChatColor.YELLOW + pluginDescription);
                sender.sendMessage(ChatColor.YELLOW + "Author: " + ChatColor.GREEN + pluginAuthor);
                return true;
            }
        });
    }

    @Override
    public void onDisable() {
    }

    public void logMessage(String message) {
        LOGGER.log(Level.INFO, "[" + pluginName + "] {0}", message);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return manager.executeCommand(sender, command, label, args);
    }

}
