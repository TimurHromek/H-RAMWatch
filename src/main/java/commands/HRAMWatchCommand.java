package eu.hindustries.timur.hramwatch.commands;

import eu.hindustries.timur.hramwatch.HRAMWatch;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HRAMWatchCommand implements CommandExecutor, TabCompleter {

    private final HRAMWatch plugin;

    public HRAMWatchCommand(HRAMWatch plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "dashboard":
                handleDashboard(sender, args);
                break;
            case "toggle":
                handleToggle(sender);
                break;
            case "reload":
                handleReload(sender);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown command. Use /hramwatch for help.");
                break;
        }
        return true;
    }

    private void handleDashboard(CommandSender sender, String[] args) {
        if (!sender.hasPermission("hramwatch.dashboard")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }

        if (args.length < 2) {
            sendGeneralUsage(sender);
            return;
        }
        switch (args[1].toLowerCase()) {
            case "server":
                sendGeneralUsage(sender);
                break;
            case "worlds":
                sendWorldUsage(sender);
                break;
            case "players":
                sendPlayerUsage(sender);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Usage: /hrw dashboard [server|worlds|players]");
                break;
        }
    }

    private void sendGeneralUsage(CommandSender sender) {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        long allocatedMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024;
        long usedMemory = allocatedMemory - freeMemory;

        sender.sendMessage(ChatColor.GOLD + "--- Server Memory Usage ---");
        sender.sendMessage(String.format(ChatColor.YELLOW + "Used Memory: " + ChatColor.WHITE + "%d MB", usedMemory));
        sender.sendMessage(String.format(ChatColor.YELLOW + "Allocated Memory: " + ChatColor.WHITE + "%d MB", allocatedMemory));
        sender.sendMessage(String.format(ChatColor.YELLOW + "Max Memory (Xmx): " + ChatColor.WHITE + "%d MB", maxMemory));
    }

    private void sendWorldUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "--- Per-World Resource Usage ---");
        for (World world : Bukkit.getWorlds()) {
            int tileEntityCount = 0;
            // Iterate loaded chunks to get the tile entity count correctly
            for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                tileEntityCount += chunk.getTileEntities().length;
            }
            sender.sendMessage(String.format(ChatColor.AQUA + "%s:" + ChatColor.WHITE + " %d Chunks, %d Entities, %d Tile Entities",
                    world.getName(), world.getLoadedChunks().length, world.getEntities().size(), tileEntityCount));
        }
    }

    private void sendPlayerUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "--- Per-Player Resource Usage (Proxies) ---");
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "No players online.");
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                int loadedChunksInView = countLoadedChunksAroundPlayer(player);
                int nearbyEntities = player.getNearbyEntities(64, 64, 64).size();
                sender.sendMessage(String.format(ChatColor.AQUA + "%s:" + ChatColor.WHITE + " ~%d Chunks in View, %d Nearby Entities",
                        player.getName(), loadedChunksInView, nearbyEntities));
            }
        }
    }

    // Helper method to count loaded chunks in a player's view
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

    private void handleToggle(CommandSender sender) {
        if (!sender.hasPermission("hramwatch.toggle")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }
        boolean newState = plugin.toggleAfkKicker();
        String status = newState ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED";
        sender.sendMessage(ChatColor.YELLOW + "The AFK Kicker has been " + status + ChatColor.YELLOW + ".");
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("hramwatch.reload")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }
        plugin.reloadPluginConfig();
        sender.sendMessage(ChatColor.GREEN + "H-RAMWatch configuration has been reloaded.");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "--- H-RAMWatch Help ---");
        if (sender.hasPermission("hramwatch.dashboard")) {
            sender.sendMessage(ChatColor.AQUA + "/hrw dashboard [server|worlds|players]" + ChatColor.WHITE + " - View resource usage.");
        }
        if (sender.hasPermission("hramwatch.toggle")) {
            sender.sendMessage(ChatColor.AQUA + "/hrw toggle" + ChatColor.WHITE + " - Enable or disable the AFK kicker.");
        }
        if (sender.hasPermission("hramwatch.reload")) {
            sender.sendMessage(ChatColor.AQUA + "/hrw reload" + ChatColor.WHITE + " - Reload the plugin's configuration.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>();
            if (sender.hasPermission("hramwatch.dashboard")) subCommands.add("dashboard");
            if (sender.hasPermission("hramwatch.toggle")) subCommands.add("toggle");
            if (sender.hasPermission("hramwatch.reload")) subCommands.add("reload");
            return StringUtil.copyPartialMatches(args[0], subCommands, new ArrayList<>());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("dashboard") && sender.hasPermission("hramwatch.dashboard")) {
            return StringUtil.copyPartialMatches(args[1], Arrays.asList("server", "worlds", "players"), new ArrayList<>());
        }
        return Collections.emptyList();
    }
}