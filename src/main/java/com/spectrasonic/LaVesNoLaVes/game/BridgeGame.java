package com.spectrasonic.LaVesNoLaVes.game;

import com.fastasyncworldedit.core.FaweAPI;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.spectrasonic.LaVesNoLaVes.Main;
import com.spectrasonic.LaVesNoLaVes.tasks.SchematicSequenceTask;
import com.spectrasonic.Utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class BridgeGame {

    private final Main plugin;
    private final PlayerManager playerManager;
    private SchematicSequenceTask sequenceTask;
    private boolean isRunning = false;
    private final ParticleManager particleManager;
    
    // Configuración
    private BlockVector3 pastePivot;
    private Location respawnPoint;
    private List<String> schematicNames;
    
    public BridgeGame(Main plugin, PlayerManager playerManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.particleManager = new ParticleManager(plugin, this);
        loadConfig();
    }
    
    public void loadConfig() {
        // Cargar punto de pivote para pegar schematics
        ConfigurationSection pivotSection = plugin.getConfig().getConfigurationSection("paste_pivot");
        if (pivotSection != null) {
            int x = pivotSection.getInt("x");
            int y = pivotSection.getInt("y");
            int z = pivotSection.getInt("z");
            pastePivot = BlockVector3.at(x, y, z);
        } else {
            pastePivot = BlockVector3.at(0, 0, 0);
            plugin.getLogger().warning("No se encontró el punto de pivote en la configuración. Usando (0,0,0).");
        }
        
        // Cargar punto de respawn
        ConfigurationSection respawnSection = plugin.getConfig().getConfigurationSection("respawn_point");
        if (respawnSection != null) {
            double x = respawnSection.getDouble("x");
            double y = respawnSection.getDouble("y");
            double z = respawnSection.getDouble("z");
            World world = Bukkit.getWorlds().get(0); // Usar el mundo principal
            respawnPoint = new Location(world, x, y, z);
        } else {
            World world = Bukkit.getWorlds().get(0);
            respawnPoint = world.getSpawnLocation();
            plugin.getLogger().warning("No se encontró el punto de respawn en la configuración. Usando el spawn del mundo.");
        }
        
        // Cargar nombres de schematics
        schematicNames = plugin.getConfig().getStringList("schematic_names");
        if (schematicNames.isEmpty()) {
            schematicNames = new ArrayList<>();
            schematicNames.add("invisible_bridge");
            schematicNames.add("bridge_1");
            schematicNames.add("bridge_2");
            schematicNames.add("bridge_3");
            schematicNames.add("visible_bridge");
            schematicNames.add("bridge_4");
            schematicNames.add("bridge_5");

            plugin.getLogger().warning("No se encontraron nombres de schematics en la configuración. Usando valores por defecto.");
        }
    }
    
    public void startGame() {
        if (isRunning) {
            return;
        }
        
        isRunning = true;
        playerManager.resetScoredPlayers();
        
        // Iniciar la secuencia de schematics
        sequenceTask = new SchematicSequenceTask(plugin, this, schematicNames);
        sequenceTask.runTaskTimer(plugin, 0L, 30L); // 3 segundos (60 ticks)
        
        // Anunciar inicio del juego
        MessageUtils.broadcastTitle(
            "<gold><b>¡Cruza el Puente!</b></gold>", 
            "", 
            1, 3, 1
        );
    }
    
    public void stopGame() {
        if (!isRunning) {
            return;
        }
        
        isRunning = false;
        
        // Detener la tarea de secuencia
        if (sequenceTask != null) {
            sequenceTask.cancel();
            sequenceTask = null;
        }
        
        // Colocar el puente visible al finalizar
        pasteSchematic("visible_bridge");
        
        // Anunciar fin del juego
        // MessageUtils.broadcastTitle(
        //     "<red><b>Juego Terminado</b></red>", 
        //     "<yellow>El puente se ha detenido</yellow>", 
        //     1, 3, 1
        // );
    }
    
    public void pasteSchematic(String schematicName) {
        // Obtener el archivo del schematic
        File schematicFile = new File(plugin.getDataFolder().getParentFile(), 
                "FastAsyncWorldEdit/schematics/" + schematicName + ".schem");
        
        if (!schematicFile.exists()) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo encontrar el schematic: " + schematicName);
            return;
        }
        
        try {
            // Cargar el formato del schematic
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            if (format == null) {
                plugin.getLogger().log(Level.SEVERE, "Formato de schematic no soportado: " + schematicName);
                return;
            }
            
            // Leer el clipboard
            Clipboard clipboard;
            try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
                clipboard = reader.read();
            }
            
            // Obtener el mundo
            World world = Bukkit.getWorlds().get(0);
            
            // Pegar el schematic de forma asíncrona
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
                    Operation operation = new ClipboardHolder(clipboard)
                            .createPaste(editSession)
                            .to(pastePivot)
                            .ignoreAirBlocks(false)
                            .build();
                    
                    Operations.complete(operation);
                    
                    // Actualizar el mundo en el hilo principal
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        FaweAPI.getWorld(world.getName()).commit();
                    });
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Error al pegar el schematic: " + schematicName, e);
                }
            });
            
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error al cargar el schematic: " + schematicName, e);
        }

        particleManager.displayParticles(schematicName);
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    public Location getRespawnPoint() {
        return respawnPoint;
    }
    
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public ParticleManager getParticleManager() {
    return particleManager;
}
}
