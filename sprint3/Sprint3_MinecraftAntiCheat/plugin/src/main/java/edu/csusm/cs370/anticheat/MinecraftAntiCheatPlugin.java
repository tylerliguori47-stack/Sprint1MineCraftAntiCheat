package edu.csusm.cs370.anticheat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/** Sprint 3 entry point: integrates Sprint 2 flight/speed with persistent reporting. */
public final class MinecraftAntiCheatPlugin extends JavaPlugin {
    private ViolationStore violations;

    @Override public void onEnable() {
        saveDefaultConfig();
        violations = new ViolationStore(this);
        getServer().getPluginManager().registerEvents(new SpeedCheck(this), this);
        getServer().getPluginManager().registerEvents(new FlightCheck(this), this);
        getLogger().info("Sprint 3 AntiCheat enabled: Speed, Flight, violation log, staff alerts");
    }

    public ViolationStore getViolations() { return violations; }

    @Override public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                       @NotNull String label, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("anticheat")) return false;
        if (!sender.hasPermission("anticheat.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command."); return true;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
            sender.sendMessage("§a[AntiCheat] Sprint 3 active | Checks: Speed, Flight | Mode: alert only");
            return true;
        }
        if (args[0].equalsIgnoreCase("recent")) {
            List<String> lines = violations.recent();
            sender.sendMessage("§e[AntiCheat] Last " + lines.size() + " violation(s):");
            for (String line : lines) sender.sendMessage("§7" + line);
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            sender.sendMessage("§a[AntiCheat] Threshold configuration reloaded. Alert cooldown changes need restart.");
            return true;
        }
        sender.sendMessage("§eUsage: /anticheat [status|recent|reload]");
        return true;
    }
}
