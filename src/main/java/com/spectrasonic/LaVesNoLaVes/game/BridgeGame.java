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
import com.spectrasonic.LaVesNoLaVes.tasks.CustomSchematicSequenceTask;
import com.spectrasonic.Utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class BridgeGame {

    private final Main plugin;
    private final PlayerManager playerManager;
    private boolean isRunning = false;
    private final ParticleManager particleManager;
    
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
        ConfigurationSection pivotSection = plugin.getConfig().getConfigurationSection("paste_pivot");
        if (pivotSection != null) {
            int x = pivotSection.getInt("x");
            int y = pivotSection.getInt("y");
            int z = pivotSection.getInt("z");
            pastePivot = BlockVector3.at(x, y, z);
        } else {
            pastePivot = BlockVector3.at(0, 0, 0);
            MessageUtils.sendConsoleMessage("<yellow>No se encontró la sección 'paste_pivot' en config.yml. Usando (0,0,0).</yellow>");
        }
        
        ConfigurationSection respawnSection = plugin.getConfig().getConfigurationSection("respawn_point");
        World defaultWorld = Bukkit.getWorlds().get(0);
        if (respawnSection != null && defaultWorld != null) {
            double x = respawnSection.getDouble("x");
            double y = respawnSection.getDouble("y");
            double z = respawnSection.getDouble("z");
            respawnPoint = new Location(defaultWorld, x, y, z);
        } else {
            if (defaultWorld != null) {
                 respawnPoint = defaultWorld.getSpawnLocation();
                MessageUtils.sendConsoleMessage("<yellow>No se encontró la sección 'respawn_point' en config.yml. Usando el spawn del mundo principal.</yellow>");
            } else {
                 respawnPoint = new Location(null, 0, 100, 0);
                MessageUtils.sendConsoleMessage("<red>No se encontró el mundo por defecto. El punto de respawn no se pudo establecer correctamente.</red>");
            }
        }

        schematicNames = plugin.getConfig().getStringList("schematic_names");

        if (schematicNames.isEmpty()) {
            MessageUtils.sendConsoleMessage("<red>La lista 'schematic_names' en config.yml está vacía o no existe. ¡El juego no funcionará correctamente sin schematics!</red>");
            schematicNames = Collections.emptyList();
        } else {
            MessageUtils.sendConsoleMessage("<green>Cargados " + schematicNames.size() + " nombres de schematics en config.yml.</green>");
        }
    }

    public void startGame(CommandSender sender) {
        if (isRunning) {
            MessageUtils.sendMessage(sender, "<yellow>El juego ya está en ejecución.</yellow>");
            return;
        }

        if (schematicNames == null || schematicNames.isEmpty()) {
            MessageUtils.sendMessage(sender, "<red>No se puede iniciar el juego Bridge: La lista de schematics está vacía. Revisa 'schematic_names' en config.yml.</red>");
             return;
        }
        
        isRunning = true;
        playerManager.resetScoredPlayers();
        MessageUtils.sendMessage(sender, "<green>Iniciando BridgeGame...</green>");

        CustomSchematicSequenceTask.start(
            plugin,
            this,
            schematicNames
        );
        
        MessageUtils.broadcastTitle(
            "<gold><b>¡Cruza el Puente!</b></gold>",
            "",
            1, 3, 1
        );
        MessageUtils.sendMessage(sender, "<green>BridgeGame iniciado correctamente.</green>");
    }
    
    public void stopGame(CommandSender sender) {
        if (!isRunning) {
            MessageUtils.sendMessage(sender, "<yellow>El juego no está en ejecución.</yellow>");
            return;
        }

        MessageUtils.sendMessage(sender, "<green>Deteniendo BridgeGame...</green>");
        isRunning = false;
        
        boolean visibleBridgeExists = new File(plugin.getDataFolder().getParentFile(),
                "FastAsyncWorldEdit/schematics/visible_bridge.schem").exists();

        if (visibleBridgeExists) {
             pasteSchematic("visible_bridge");
            MessageUtils.sendMessage(sender, "<yellow>Puente 'visible_bridge' pegado al detener el juego.");
        } else {
            MessageUtils.sendMessage(sender, "<yellow>El schematic 'visible_bridge' no se encontró. No se pudo restaurar el puente visible al detener.</yellow>");
        }

        MessageUtils.sendMessage(sender, "<green>BridgeGame detenido.</green>");
    }

    public void pasteSchematic(String schematicName) {
        File schematicFolder = new File(plugin.getDataFolder().getParentFile(), "FastAsyncWorldEdit/schematics");
        if (!schematicFolder.exists()) {
            MessageUtils.sendConsoleMessage("<red>La carpeta de schematics de FAWE no existe en: " + schematicFolder.getPath() + "</red>");
            return;
        }

        File schematicFile = new File(schematicFolder, schematicName + ".schem");
            
        if (!schematicFile.exists()) {
            MessageUtils.sendConsoleMessage("<red>No se pudo encontrar el archivo schematic: " + schematicFile.getPath() + "</red>");
            return;
        }
            
        ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
        if (format == null) {
            MessageUtils.sendConsoleMessage("<red>Formato de schematic no soportado para: " + schematicName + "</red>");
            return;
        }

        World world = (respawnPoint != null && respawnPoint.getWorld() != null) ? respawnPoint.getWorld() : Bukkit.getWorlds().get(0);
        if (world == null) {
            MessageUtils.sendConsoleMessage("<red>No se pudo obtener un mundo válido para pegar el schematic.</red>");
            return;
        }

        if (pastePivot == null) {
            MessageUtils.sendConsoleMessage("<red>El punto de pegado (pastePivot) es nulo. Verifica la config.</red>");
            return;
        }

        try {
            Clipboard clipboard;
            try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
                clipboard = reader.read();
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
                    Operation operation = new ClipboardHolder(clipboard)
                            .createPaste(editSession)
                            .to(pastePivot)
                            .ignoreAirBlocks(false)
                            .build();
                    
                    Operations.complete(operation);
                    
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        FaweAPI.getWorld(world.getName()).commit();
                    });
                } catch (Exception e) {
                    MessageUtils.sendConsoleMessage("<red>Error al pegar el schematic: " + schematicName + "</red>");
                }
            });
            
        } catch (IOException e) {
            MessageUtils.sendConsoleMessage("<red>Error al cargar el schematic: " + schematicName + "</red>");
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
