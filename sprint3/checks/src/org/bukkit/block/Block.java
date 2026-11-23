package org.bukkit.block;

import org.bukkit.Material;

public interface Block {
    Material getType();
    boolean isLiquid();
}
