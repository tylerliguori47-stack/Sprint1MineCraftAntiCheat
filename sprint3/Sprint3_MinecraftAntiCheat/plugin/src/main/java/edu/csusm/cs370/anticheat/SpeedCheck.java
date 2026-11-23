package edu.csusm.cs370.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffectType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Adapted from SJ-coder7 Sprint 2 SpeedCheck.java; avoids fixed TPS assumption. */
public final class SpeedCheck implements Listener {
    private final MinecraftAntiCheatPlugin plugin;
    private final Map<UUID, Sample> samples = new HashMap<>();
    private final Map<UUID, Integer> streak = new HashMap<>();
    private record Sample(Location location, long nanoTime) {}

    public SpeedCheck(MinecraftAntiCheatPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getScheduler().runTaskTimer(plugin, this::sample, 5L, 5L);
    }

    private void sample() {
        double max = plugin.getConfig().getDouble("speed.max-blocks-per-second", 8.0);
        int required = Math.max(1, plugin.getConfig().getInt("speed.consecutive-samples", 2));
        long now = System.nanoTime();
        for (Player p : Bukkit.getOnlinePlayers()) {
            UUID id = p.getUniqueId();
            Location current = p.getLocation();
            Sample prev = samples.put(id, new Sample(current.clone(), now));
            if (prev == null || prev.location().getWorld() == null ||
                    !prev.location().getWorld().equals(current.getWorld()) || exempt(p)) {
                streak.remove(id); continue;
            }
            double seconds = (now - prev.nanoTime()) / 1_000_000_000.0;
            if (seconds <= 0.0 || seconds > 2.0) { streak.remove(id); continue; }
            double dx = current.getX() - prev.location().getX();
            double dz = current.getZ() - prev.location().getZ();
            double speed = Math.hypot(dx, dz) / seconds;
            if (speed > max) {
                int count = streak.merge(id, 1, Integer::sum);
                if (count >= required) {
                    plugin.getViolations().flag(p, "SPEED", speed, max);
                    streak.put(id, 0);
                }
            } else streak.remove(id);
        }
    }

    private boolean exempt(Player p) {
        return p.hasPermission("anticheat.bypass") || p.isDead() ||
                p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR ||
                p.getAllowFlight() || p.isFlying() || p.isInsideVehicle() || p.isGliding() ||
                p.isSwimming() || p.hasPotionEffect(PotionEffectType.SPEED) ||
                p.hasPotionEffect(PotionEffectType.DOLPHINS_GRACE);
    }

    @EventHandler public void onJoin(PlayerJoinEvent e) { reset(e.getPlayer()); }
    @EventHandler public void onQuit(PlayerQuitEvent e) {
        samples.remove(e.getPlayer().getUniqueId()); streak.remove(e.getPlayer().getUniqueId());
    }
    @EventHandler public void onTeleport(PlayerTeleportEvent e) { reset(e.getPlayer()); }
    public void reset(Player p) { samples.remove(p.getUniqueId()); streak.remove(p.getUniqueId()); }
}
