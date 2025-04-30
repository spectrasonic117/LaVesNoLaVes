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
import org.bukkit.potion.PotionEffectType;

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
        Player player = event.getPlayer();
        if (bridgeGame.isRunning() && playerManager.hasPlayerScored(player)) {
            player.removePotionEffect(PotionEffectType.BLINDNESS);
        }

        if (!bridgeGame.isRunning()) {
            return;
        }
            
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Block blockBelow = player.getLocation().subtract(0, 2, 0).getBlock();

        if (blockBelow.getType() == Material.BLACK_STAINED_GLASS) {
            player.teleport(bridgeGame.getRespawnPoint());
            pointsManager.subtractPoints(player, bridgeGame.getPointsToRevoke());
            TeleportEffectUtils.createDNAHelix(plugin, bridgeGame.getRespawnPoint(), 3.0, 20);
            MessageUtils.sendMessage(player, "<red><b>¡Has caído! -" + bridgeGame.getPointsToRevoke() + " Puntos</red>");
        }
            
        if (blockBelow.getType() == Material.STONE) {
            if (playerManager.hasPlayerScored(player)) {
                return;
            }

            playerManager.markPlayerAsScored(player);
            pointsManager.addPoints(player, bridgeGame.getPointsToGrant());
            MessageUtils.sendTitle(player, 
                "<green><b>¡Has llegado!</b></green>", 
                "<yellow><b>+" + bridgeGame.getPointsToGrant() + " Puntos</b></yellow>", 
                1, 3, 1
            );
            SoundUtils.playerSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        }
    }
}
