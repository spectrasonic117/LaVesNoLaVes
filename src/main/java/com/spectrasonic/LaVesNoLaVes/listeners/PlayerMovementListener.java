package com.spectrasonic.LaVesNoLaVes.listeners;

import com.spectrasonic.LaVesNoLaVes.Main;
import com.spectrasonic.LaVesNoLaVes.game.BridgeGame;
import com.spectrasonic.LaVesNoLaVes.game.PlayerManager;
import com.spectrasonic.Utils.MessageUtils;
import com.spectrasonic.Utils.PointsManager;
import com.spectrasonic.Utils.SoundUtils;
import com.spectrasonic.Utils.TeleportEffectUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMovementListener implements Listener {

    private final Main plugin;
    private final BridgeGame bridgeGame;
    private final PlayerManager playerManager;
    private final PointsManager pointsManager;

    public PlayerMovementListener(Main plugin, BridgeGame bridgeGame, PlayerManager playerManager) {
        this.plugin = plugin;
        this.bridgeGame = bridgeGame;
        this.playerManager = playerManager;
        this.pointsManager = new PointsManager(plugin);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Solo procesar si el juego está en ejecución
        if (!bridgeGame.isRunning()) {
            return;
        }
        
        // Solo procesar si el jugador ha cambiado de bloque
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        Player player = event.getPlayer();
        Block blockBelow = player.getLocation().subtract(0, 2, 0).getBlock();
        
        // Verificar si el jugador está sobre vidrio negro tintado
        if (blockBelow.getType() == Material.BLACK_STAINED_GLASS) {
            // Teletransportar al jugador al punto de respawn
            player.teleport(bridgeGame.getRespawnPoint());
            pointsManager.subtractPoints(player, 1);
            
            // Crear efecto de teletransporte
            TeleportEffectUtils.createDNAHelix(plugin, bridgeGame.getRespawnPoint(), 3.0, 20);
            
            // Enviar mensaje
            MessageUtils.sendMessage(player, "<red><b>¡Has caído! -1 Punto</red>");
        }
        
        // Verificar si el jugador ha llegado a la meta (piedra)
        if (blockBelow.getType() == Material.STONE) {
            // Verificar si el jugador ya ha puntuado
            if (playerManager.hasPlayerScored(player)) {
                return;
            }
            
            // Marcar al jugador como puntuado
            playerManager.markPlayerAsScored(player);
            
            // Dar puntos al jugador
            pointsManager.addPoints(player, 10);
            
            // Enviar título y sonido
            MessageUtils.sendTitle(player, 
                "<green><b>¡Has llegado!</b></green>", 
                "<yellow><b>+10 Puntos</b></yellow>", 
                1, 3, 1
            );
            
            SoundUtils.playerSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        }
    }
}
