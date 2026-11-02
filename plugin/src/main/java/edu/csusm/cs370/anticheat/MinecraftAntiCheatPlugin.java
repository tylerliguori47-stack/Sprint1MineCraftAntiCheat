package edu.csusm.cs370.anticheat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/** New example scaffold. The original Sprint 1 plugin source was not available. */
public final class MinecraftAntiCheatPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("MinecraftAntiCheat example plugin enabled. Detection is not implemented.");
    }

    @Override
    public void onDisable() {
        getLogger().info("MinecraftAntiCheat example plugin disabled.");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("anticheat")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("status")) {
                sender.sendMessage("MinecraftAntiCheat example scaffold is loaded; cheat detection is not implemented.");
            } else {
                sender.sendMessage("Usage: /anticheat status");
            }
            return true;
        }
        return false;
    }
}
