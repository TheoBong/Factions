package com.massivecraft.factions.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface PortalListenerBase {
    boolean shouldCancel(Location location, Player player);
}