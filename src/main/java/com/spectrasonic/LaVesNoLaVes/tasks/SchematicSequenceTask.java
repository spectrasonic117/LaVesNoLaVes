package com.spectrasonic.LaVesNoLaVes.tasks;

import com.spectrasonic.LaVesNoLaVes.Main;
import com.spectrasonic.LaVesNoLaVes.game.BridgeGame;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class SchematicSequenceTask extends BukkitRunnable {

    private final Main plugin;
    private final BridgeGame bridgeGame;
    private final List<String> schematicNames;
    private int currentIndex = 0;

    public SchematicSequenceTask(Main plugin, BridgeGame bridgeGame, List<String> schematicNames) {
        this.plugin = plugin;
        this.bridgeGame = bridgeGame;
        this.schematicNames = schematicNames;
    }

    @Override
    public void run() {
        if (!bridgeGame.isRunning()) {
            this.cancel();
            return;
        }
        
        // Obtener el nombre del schematic actual
        String currentSchematic = schematicNames.get(currentIndex);
        
        // Pegar el schematic
        bridgeGame.pasteSchematic(currentSchematic);
        
        // Enviar mensaje de acción
        // MessageUtils.broadcastActionBar("<yellow>Puente cambiando: <gold>" + currentSchematic + "</gold></yellow>");
        
        // Avanzar al siguiente schematic
        currentIndex = (currentIndex + 1) % schematicNames.size();
    }
}
