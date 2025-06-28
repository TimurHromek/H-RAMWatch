package eu.hindustries.timur.hramwatch.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerActivityListener implements Listener {

    private final Map<UUID, Long> lastActivity = new ConcurrentHashMap<>();
    private final long afkTimeMillis;

    public PlayerActivityListener(long afkTimeMinutes) {
        this.afkTimeMillis = afkTimeMinutes * 60 * 1000;
    }

    private void updateActivity(Player player) {
        lastActivity.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        updateActivity(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        lastActivity.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.hasChangedBlock()) {
            updateActivity(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        updateActivity(event.getPlayer());
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        updateActivity(event.getPlayer());
    }

    public boolean isAfk(Player player) {
        if (!lastActivity.containsKey(player.getUniqueId())) {
            updateActivity(player);
            return false;
        }
        return (System.currentTimeMillis() - lastActivity.get(player.getUniqueId())) > afkTimeMillis;
    }
}