package eu.hindustries.timur.hramwatch;

import eu.hindustries.timur.hramwatch.commands.HRAMWatchCommand;
import eu.hindustries.timur.hramwatch.listeners.PlayerActivityListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class HRAMWatch extends JavaPlugin {

    private PlayerActivityListener activityListener;
    private BukkitTask checkTask;
    private final Set<UUID> warnedPlayers = new HashSet<>();
    private boolean afkKickerEnabled;

    @Override
    public void onEnable() {
        // Single, dynamic startup message that reads the version from plugin.yml.
        getLogger().info("H-RAMWatch v" + this.getDescription().getVersion() + " has been enabled.");

        // This method contains its own logging and handles the core setup.
        reloadPluginConfig();

        HRAMWatchCommand commandHandler = new HRAMWatchCommand(this);
        getCommand("hramwatch").setExecutor(commandHandler);
        getCommand("hramwatch").setTabCompleter(commandHandler);

        long afkTime = getConfig().getLong("afk-kick.afk-time-minutes", 10);
        this.activityListener = new PlayerActivityListener(afkTime);
        getServer().getPluginManager().registerEvents(activityListener, this);
    }

    @Override
    public void onDisable() {
        if (checkTask != null) {
            checkTask.cancel();
        }
        getLogger().info("H-RAMWatch has been disabled.");
    }

    public void reloadPluginConfig() {
        if (checkTask != null) {
            checkTask.cancel();
        }
        saveDefaultConfig();
        reloadConfig();

        this.afkKickerEnabled = getConfig().getBoolean("afk-kick.enabled-by-default", true);
        String status = afkKickerEnabled ? "ENABLED" : "DISABLED";
        getLogger().info("AFK Kicker is " + status + " by default.");

        startCheckTask();
    }

    private void startCheckTask() {
        long interval = getConfig().getLong("check-interval-seconds", 30) * 20; // Ticks
        checkTask = getServer().getScheduler().runTaskTimerAsynchronously(this, this::performChecks, interval, interval);
    }

    private void performChecks() {
        if (!afkKickerEnabled) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("hramwatch.bypass") || !activityListener.isAfk(player)) {
                warnedPlayers.remove(player.getUniqueId());
                continue;
            }

            Bukkit.getScheduler().runTask(this, () -> performSyncChecks(player));
        }
    }

    private void performSyncChecks(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        final int chunkThreshold = getConfig().getInt("afk-kick.chunk-threshold");
        final int entityThreshold = getConfig().getInt("afk-kick.entity-threshold");
        final double entityCheckRadius = getConfig().getDouble("afk-kick.entity-check-radius");
        final boolean warningEnabled = getConfig().getBoolean("afk-kick.warning-enabled");

        int loadedChunksInView = countLoadedChunksAroundPlayer(player);
        int nearbyEntities = player.getNearbyEntities(entityCheckRadius, entityCheckRadius, entityCheckRadius).size();

        boolean overLimit = loadedChunksInView > chunkThreshold || nearbyEntities > entityThreshold;

        if (overLimit) {
            if (warningEnabled && !warnedPlayers.contains(player.getUniqueId())) {
                String warningMsg = getConfig().getString("afk-kick.warning-message");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', warningMsg));
                warnedPlayers.add(player.getUniqueId());
            } else {
                kickPlayer(player, loadedChunksInView, nearbyEntities);
                warnedPlayers.remove(player.getUniqueId());
            }
        } else {
            warnedPlayers.remove(player.getUniqueId());
        }
    }

    private int countLoadedChunksAroundPlayer(Player player) {
        int viewDistance = player.getClientViewDistance();
        org.bukkit.Chunk centerChunk = player.getChunk();
        World world = player.getWorld();
        int count = 0;
        for (int x = -viewDistance; x <= viewDistance; x++) {
            for (int z = -viewDistance; z <= viewDistance; z++) {
                if (world.isChunkLoaded(centerChunk.getX() + x, centerChunk.getZ() + z)) {
                    count++;
                }
            }
        }
        return count;
    }

    private void kickPlayer(Player player, int chunks, int entities) {
        String kickReason = getConfig().getString("afk-kick.kick-message");
        final String finalKickReason = ChatColor.translateAlternateColorCodes('&', kickReason);

        player.kickPlayer(finalKickReason);

        getLogger().info(String.format("Kicked player %s for being AFK with high resource usage (Chunks in view: %d, Nearby Entities: %d).",
                player.getName(), chunks, entities));
    }

    public boolean toggleAfkKicker() {
        this.afkKickerEnabled = !this.afkKickerEnabled;
        return this.afkKickerEnabled;
    }
}