package org.bukkit;

import java.util.Collection;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

/** Desktop fixture for the two static lookups used by the reviewed sources. */
public final class Bukkit {
    private static Collection<? extends Player> players = List.of();
    private static BukkitScheduler scheduler;

    public static void useFixture(Collection<? extends Player> online, BukkitScheduler clock) {
        players = online;
        scheduler = clock;
    }

    public static Collection<? extends Player> getOnlinePlayers() { return players; }
    public static BukkitScheduler getScheduler() { return scheduler; }
}
