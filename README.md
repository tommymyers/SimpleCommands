SimpleCommands API
==================

SimpleCommands is a Bukkit plugin that allows plugin developers to register commands only using code. No need to add your commands to the plugin YAML

**[Source Code](https://bitbucket.org/tommymyers/simplecommands/src)**

How to use
------------
1. Download the latest version on the [downloads page](https://bitbucket.org/tommymyers/simplecommands/downloads) and add it as a dependency to your Java project
2. Add SimpleCommands to your plugin YAML dependency: `depend: [SimpleCommands]`
3. Create an instance of `Manager` passing your plugin as the parameter
 * You can call `setDefaultPermissionMessage` to set a default permission message for your plugin
4. Create a class that `implements CommandListener` and define some commands. Here's an example:
```java
@CommandHandler(name = "cheese")
public boolean cheese(CommandSender sender, Command command, String label, String[] args) {
    sender.sendMessage("Cheese!");
    return true;
}
```
 * Notice the structure of the method? It uses the same structure as the `onCommand` method Bukkit uses, but you can name the method whatever you want.
 * The `CommandHandler` annotation is important as it describes the command to the API

5. Copy the following code into your plugin's main class and replace `manager` with whatever you named the manager instance
```java
@Override
public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    return manager.executeCommand(sender, command, label, args);
}
```
 * This is crucial because as the commands are registered to your plugin, you must call the `manager.executeCommand` method and SimpleCommands will invoke the correct method

@CommandHandler
---------------
If you want to define other attributes like description or aliases then just define them in the annotation. Example:
```java
@CommandHandler(name = "teleport", aliases = {"tp", "t"}, usage = "teleport <user>",
description = "Teleports to a player", permission = "teleportation.teleport",
 noPermissionMessage = "You do not have permission for this command")
```
