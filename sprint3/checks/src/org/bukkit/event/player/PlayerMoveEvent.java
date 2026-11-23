package org.bukkit.event.player;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerMoveEvent extends PlayerEvent {
    private final Location from;
    private final Location to;

    public PlayerMoveEvent(Player player, Location from, Location to) {
        super(player);
        this.from = from;
        this.to = to;
    }

    public Location getFrom() { return from; }
    public Location getTo() { return to; }
}
