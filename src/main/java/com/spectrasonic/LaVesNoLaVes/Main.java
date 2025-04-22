package com.spectrasonic.LaVesNoLaVes;

import com.spectrasonic.LaVesNoLaVes.commands.CommandManager;
import com.spectrasonic.LaVesNoLaVes.game.BridgeGame;
import com.spectrasonic.LaVesNoLaVes.game.PlayerManager;
import com.spectrasonic.LaVesNoLaVes.listeners.PlayerMovementListener;
import com.spectrasonic.Utils.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    
    private BridgeGame bridgeGame;
    private PlayerManager playerManager;

    @Override
    public void onEnable() {
        // Guardar configuración por defecto
        saveDefaultConfig();
        
        // Inicializar componentes del juego
        this.playerManager = new PlayerManager();
        this.bridgeGame = new BridgeGame(this, playerManager);
        
        registerCommands();
        registerEvents();
        
        MessageUtils.sendStartupMessage(this);
    }

    @Override
    public void onDisable() {
        // Detener el juego si está en curso
        if (bridgeGame.isRunning()) {
            bridgeGame.stopGame(null); // Pasamos null ya que no hay un CommandSender claro
        }
        
        MessageUtils.sendShutdownMessage(this);
    }

    public void registerCommands() {
        new CommandManager(this, bridgeGame);
    }

    public void registerEvents() {
        getServer().getPluginManager().registerEvents(
            new PlayerMovementListener(this, bridgeGame, playerManager), 
            this
        );
    }
    
    public BridgeGame getBridgeGame() {
        return bridgeGame;
    }
    
    public void reloadPlugin(CommandSender sender) {
        reloadConfig();
        if (bridgeGame.isRunning()) {
            bridgeGame.stopGame(sender);
        }
        bridgeGame.loadConfig();
        MessageUtils.sendMessage(sender, "<green>Configuración recargada.</green>");
    }
}
