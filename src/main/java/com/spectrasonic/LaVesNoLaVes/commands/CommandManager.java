package com.spectrasonic.LaVesNoLaVes.commands;

import co.aikar.commands.PaperCommandManager;
import com.spectrasonic.LaVesNoLaVes.Main;
import com.spectrasonic.LaVesNoLaVes.game.BridgeGame;

public class CommandManager {

    public CommandManager(Main plugin, BridgeGame bridgeGame) {
        PaperCommandManager manager = new PaperCommandManager(plugin);
        
        // Registrar comandos
        manager.registerCommand(new BridgeCommand(plugin, bridgeGame));
    }
}
