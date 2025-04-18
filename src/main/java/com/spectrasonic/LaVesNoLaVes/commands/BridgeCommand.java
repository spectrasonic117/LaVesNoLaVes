package com.spectrasonic.LaVesNoLaVes.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.spectrasonic.LaVesNoLaVes.Main;
import com.spectrasonic.LaVesNoLaVes.game.BridgeGame;
import com.spectrasonic.Utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@CommandAlias("bridge|br")
public class BridgeCommand extends BaseCommand {

    private final Main plugin;
    private final BridgeGame bridgeGame;
    private int particleCount;

    public BridgeCommand(Main plugin, BridgeGame bridgeGame) {
        this.plugin = plugin;
        this.bridgeGame = bridgeGame;
        loadConfig();
    }

    private void loadConfig() {
        particleCount = plugin.getConfig().getInt("particle_count", 100);
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

            applyBlindnessEffect();
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

            removeBlindnessEffect();
        }
    }

    @Subcommand("reload")
    @CommandPermission("bridge.admin")
    @Description("Recarga la configuración del plugin")
    public void onReload(CommandSender sender) {
        plugin.reloadPlugin();
        loadConfig();
        MessageUtils.sendMessage(sender, "<green>Configuración recargada.</green>");
    }

    private void applyBlindnessEffect() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getGameMode() == GameMode.ADVENTURE) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 3, false, false));
            }
        }
    }

    private void removeBlindnessEffect() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.removePotionEffect(PotionEffectType.BLINDNESS);
        }
    }
}
