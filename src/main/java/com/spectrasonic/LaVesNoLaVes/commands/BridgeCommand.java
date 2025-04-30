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
        @CommandPermission("bridge.admin")
        @Description("Inicia el minijuego del puente")
        @CommandCompletion("1|2|3")
        public void onStart(CommandSender sender, @Single String round) {
            try {
                int roundNumber = Integer.parseInt(round);
                if (roundNumber < 1 || roundNumber > 3) {
                    MessageUtils.sendMessage(sender,"&cRonda inválida. Debe ser 1, 2 o 3.");
                    return;
                }
                bridgeGame.startGame(sender, roundNumber);
                applyEffects(roundNumber);
            } catch (NumberFormatException e) {
                MessageUtils.sendMessage(sender,"&cFormato de ronda inválido. Debe ser 1, 2 o 3.");
            }
        }

        @Subcommand("stop")
        @Description("Detiene el minijuego del puente")
        public void onStop(CommandSender sender) {
            bridgeGame.stopGame(sender);
            removeEffects();
        }
    }

    @Subcommand("reload")
    @CommandPermission("bridge.admin")
    @Description("Recarga la configuración del plugin")
    public void onReload(CommandSender sender) {
        plugin.reloadPlugin(sender);
    }

    private void applyEffects(int round) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getGameMode() == GameMode.ADVENTURE) {
                applyEffect(player, round);
            }
        }
    }

    private void applyEffect(Player player, int round) {
        PotionEffectType effect;
        switch (round) {
            case 1:
                effect = PotionEffectType.BLINDNESS;
                break;
            case 2:
                effect = PotionEffectType.DARKNESS;
                break;
            case 3:
                effect = PotionEffectType.BLINDNESS;
                break;
            default:
                return;
        }
        player.addPotionEffect(new PotionEffect(effect, Integer.MAX_VALUE, 0));
    }

    private void removeEffects() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            removeEffects(player);
        }
    }

    private void removeEffects(Player player) {
        player.removePotionEffect(PotionEffectType.DARKNESS);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
    }
}
