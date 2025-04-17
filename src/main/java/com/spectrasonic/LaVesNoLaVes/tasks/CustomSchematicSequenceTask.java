package com.spectrasonic.LaVesNoLaVes.tasks;

import com.spectrasonic.LaVesNoLaVes.Main;
import com.spectrasonic.LaVesNoLaVes.game.BridgeGame;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * Scheduler para pegar esquemas con intervalos variables.
 * En nuestro caso: 60, 20, 20, 60, 20 ticks (3s, 1s, 1s, 3s, 1s).
 */
public final class CustomSchematicSequenceTask {

    // define aquí los intervalos en ticks
    private static final List<Long> INTERVALS = List.of(60L, 20L, 20L, 60L, 20L);

    /**
     * Arranca la secuencia a partir del índice 0.
     */
    public static void start(Main plugin, BridgeGame bridgeGame, List<String> schematicNames) {
        scheduleNext(plugin, bridgeGame, schematicNames, 0);
    }

    /**
     * Programa la siguiente ejecución en base al índice dado.
     */
    private static void scheduleNext(Main plugin,
                                     BridgeGame bridgeGame,
                                     List<String> schematicNames,
                                     int index) {
        long delay = INTERVALS.get(index % INTERVALS.size());
        new BukkitRunnable() {
            @Override
            public void run() {
                // Si el juego ya no está en curso, no pegar nada ni reprogramar.
                if (!bridgeGame.isRunning()) {
                    return;
                }

                // Pega el schematic correspondiente
                String name = schematicNames.get(index % schematicNames.size());
                bridgeGame.pasteSchematic(name);

                // Programa la siguiente ejecución
                scheduleNext(
                  plugin,
                  bridgeGame,
                  schematicNames,
                  (index + 1) % schematicNames.size()
                );
            }
        }.runTaskLater(plugin, delay);
    }

    // Prevent instantiation
    private CustomSchematicSequenceTask() { }
}
