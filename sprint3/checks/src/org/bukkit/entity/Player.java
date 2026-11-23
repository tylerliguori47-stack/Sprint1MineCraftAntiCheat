package org.bukkit.entity;

import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.potion.PotionEffectType;

public interface Player extends CommandSender {
    UUID getUniqueId();
    String getName();
    Location getLocation();
    GameMode getGameMode();
    boolean isDead();
    boolean getAllowFlight();
    boolean isFlying();
    boolean isInsideVehicle();
    boolean isGliding();
    boolean isSwimming();
    boolean isOnGround();
    boolean isClimbing();
    boolean hasPotionEffect(PotionEffectType effect);
}
