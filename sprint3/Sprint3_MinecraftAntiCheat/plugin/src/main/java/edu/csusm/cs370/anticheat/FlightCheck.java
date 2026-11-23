package edu.csusm.cs370.anticheat;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffectType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Adapted from SJ-coder7 Sprint 2 FlightCheck.java. Alert-only: no unsafe teleport. */
public final class FlightCheck implements Listener {
    private final MinecraftAntiCheatPlugin plugin;
    private final Map<UUID, Integer> airTicks = new HashMap<>();
    private final Map<UUID, Integer> suspicions = new HashMap<>();

    public FlightCheck(MinecraftAntiCheatPlugin plugin) { this.plugin = plugin; }

    @EventHandler public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        Location from = e.getFrom(), to = e.getTo();
        if (to == null || (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ())) return;
        UUID id = p.getUniqueId();
        if (exempt(p) || p.isOnGround() || nearClimbable(p)) {
            airTicks.remove(id); suspicions.remove(id); return;
        }
        int ticks = airTicks.merge(id, 1, Integer::sum);
        double dy = to.getY() - from.getY();
        int minimum = Math.max(5, plugin.getConfig().getInt("flight.minimum-air-moves", 12));
        boolean hover = ticks > minimum && Math.abs(dy) < 0.03;
        boolean rise = ticks > minimum + 2 && dy > 0.0;
        if (hover || rise) {
            int count = suspicions.merge(id, 1, Integer::sum);
            if (count >= Math.max(1, plugin.getConfig().getInt("flight.consecutive-suspicious-moves", 3))) {
                plugin.getViolations().flag(p, hover ? "FLIGHT_HOVER" : "FLIGHT_ASCEND", dy, 0.03);
                suspicions.put(id, 0);
            }
        } else suspicions.remove(id);
    }

    private boolean exempt(Player p) {
        return p.hasPermission("anticheat.bypass") || p.isDead() ||
                p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR ||
                p.getAllowFlight() || p.isFlying() || p.isGliding() || p.isInsideVehicle() ||
                p.isSwimming() || p.hasPotionEffect(PotionEffectType.LEVITATION) ||
                p.hasPotionEffect(PotionEffectType.SLOW_FALLING) ||
                p.hasPotionEffect(PotionEffectType.JUMP_BOOST);
    }
    private boolean nearClimbable(Player p) {
        var block = p.getLocation().getBlock();
        String type = block.getType().name();
        return block.isLiquid() || p.isClimbing() || type.contains("WEB") || type.contains("SCAFFOLDING") ||
                type.contains("BUBBLE_COLUMN") || type.contains("HONEY_BLOCK");
    }
    @EventHandler public void onQuit(PlayerQuitEvent e) { reset(e.getPlayer()); }
    @EventHandler public void onTeleport(PlayerTeleportEvent e) { reset(e.getPlayer()); }
    public void reset(Player p) { airTicks.remove(p.getUniqueId()); suspicions.remove(p.getUniqueId()); }
}
