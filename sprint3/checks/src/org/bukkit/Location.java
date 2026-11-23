package org.bukkit;

import org.bukkit.block.Block;

public final class Location implements Cloneable {
    private final World world;
    private final double x;
    private final double y;
    private final double z;

    public Location(World world, double x, double y, double z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public World getWorld() { return world; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public Block getBlock() { return world.getBlockAt(this); }
    @Override public Location clone() { return new Location(world, x, y, z); }
}
