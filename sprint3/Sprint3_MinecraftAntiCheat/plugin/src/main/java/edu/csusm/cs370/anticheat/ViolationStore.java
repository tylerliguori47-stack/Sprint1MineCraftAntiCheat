package edu.csusm.cs370.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

/** Server-thread-only event log. Records persist in plugins/MinecraftAntiCheat/violations.log. */
public final class ViolationStore {
    private final JavaPlugin plugin;
    private final Deque<String> recent = new ArrayDeque<>();
    private final Path log;
    private final long cooldownMs;
    private final java.util.Map<String, Long> lastAlert = new java.util.HashMap<>();

    public ViolationStore(JavaPlugin plugin) {
        this.plugin = plugin;
        this.log = plugin.getDataFolder().toPath().resolve("violations.log");
        this.cooldownMs = Math.max(1000, plugin.getConfig().getLong("alerts.cooldown-ms", 5000));
        try {
            Files.createDirectories(log.getParent());
            if (Files.exists(log)) {
                for (String line : Files.readAllLines(log, StandardCharsets.UTF_8)) remember(line);
            }
        } catch (IOException ex) {
            plugin.getLogger().warning("Could not load violation history: " + ex.getMessage());
        }
    }

    public void flag(Player player, String check, double observed, double threshold) {
        // No player-supplied text can inject a line into the log.
        String line = String.format(Locale.ROOT, "%s | %s | %s | %.3f | %.3f",
                Instant.now(), player.getUniqueId(), check, observed, threshold);
        remember(line);
        try {
            Files.writeString(log, line + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            plugin.getLogger().warning("Failed to save violation: " + ex.getMessage());
        }
        plugin.getLogger().warning("AntiCheat: " + player.getName() + " " + check + " " + observed);
        String key = player.getUniqueId() + ":" + check;
        long now = System.currentTimeMillis();
        if (now - lastAlert.getOrDefault(key, 0L) < cooldownMs) return;
        lastAlert.put(key, now);
        if (!plugin.getConfig().getBoolean("alerts.enabled", true)) return;
        String message = "§c[AntiCheat] §e" + player.getName() + " §7failed §c" + check
                + " §7(" + String.format(Locale.ROOT, "%.2f", observed) + ")";
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("anticheat.alerts")) staff.sendMessage(message);
        }
    }

    private void remember(String line) {
        recent.addLast(line);
        while (recent.size() > 20) recent.removeFirst();
    }

    public List<String> recent() { return new ArrayList<>(recent); }
}
