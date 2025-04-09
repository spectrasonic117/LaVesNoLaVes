package com.spectrasonic.LaVesNoLaVes.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.spectrasonic.LaVesNoLaVes.Main;
import com.spectrasonic.LaVesNoLaVes.game.BridgeGame;
import com.spectrasonic.Utils.MessageUtils;
import org.bukkit.command.CommandSender;

@CommandAlias("bridge")
public class BridgeCommand extends BaseCommand {

    private final Main plugin;
    private final BridgeGame bridgeGame;

    public BridgeCommand(Main plugin, BridgeGame bridgeGame) {
        this.plugin = plugin;
        this.bridgeGame = bridgeGame;
    }

    @Subcommand("game")
    @CommandPermission("bridge.admin")
    @Description("Controla el minijuego del puente")
    public class GameCommands extends BaseCommand {

        @Subcommand("start")
        @Description("Inicia el minijuego del puente")
        public void onStart(CommandSender sender) {
            if (bridgeGame.isRunning()) {
                MessageUtils.sendMessage(sender, "<red>El juego ya está en ejecución.</red>");
                return;
            }
            
            bridgeGame.startGame();
            MessageUtils.sendMessage(sender, "<green>¡Minijuego Comenzado!</green>");
        }

        @Subcommand("stop")
        @Description("Detiene el minijuego del puente")
        public void onStop(CommandSender sender) {
            if (!bridgeGame.isRunning()) {
                MessageUtils.sendMessage(sender, "<red>El juego no está en ejecución.</red>");
                return;
            }
            
            bridgeGame.stopGame();
            MessageUtils.sendMessage(sender, "<red>Minijuego Detenido.</red>");
        }
    }

    @Subcommand("reload")
    @CommandPermission("bridge.admin")
    @Description("Recarga la configuración del plugin")
    public void onReload(CommandSender sender) {
        plugin.reloadPlugin();
        MessageUtils.sendMessage(sender, "<green>Configuración recargada.</green>");
    }
}
