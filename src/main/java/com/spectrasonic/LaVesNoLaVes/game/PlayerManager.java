package com.spectrasonic.LaVesNoLaVes.game;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerManager {
    
    private final Set<UUID> scoredPlayers = new HashSet<>();
    
    public boolean hasPlayerScored(Player player) {
        return scoredPlayers.contains(player.getUniqueId());
    }
    
    public void markPlayerAsScored(Player player) {
        scoredPlayers.add(player.getUniqueId());
    }
    
    public void resetScoredPlayers() {
        scoredPlayers.clear();
    }
}
