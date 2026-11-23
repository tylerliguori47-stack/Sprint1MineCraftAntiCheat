package org.bukkit;

import org.bukkit.block.Block;

public interface World {
    Block getBlockAt(Location location);
}
