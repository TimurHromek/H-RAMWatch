# H-RAMWatch

![API](https://img.shields.io/badge/API-Paper_1.21-red.svg)
![Java](https://img.shields.io/badge/Java-17+-blue.svg)

H-RAMWatch is a lightweight but powerful server management plugin for PaperMC, specifically designed to protect server performance on resource-constrained hardware. Its core function is to act as a "smart resource limiter" that identifies and gently removes players who are both idle (AFK) and located in an area that consumes excessive server resources.

The plugin's core philosophy is to only target players who are both **idle (AFK)** and in a **resource-intensive area**. Active players will never be affected.

## Features

-   **Smart AFK Limiter:** Automatically kicks players who are AFK and are loading an excessive number of chunks or are near a high concentration of entities.
-   **In-Game Resource Dashboard:** A public command (`/hrw dashboard`) that displays live performance metrics for the server, worlds, and players.
-   **Live Admin Controls:**
    -   Enable or disable the AFK kicker on-the-fly with `/hrw toggle`.
    -   Reload the configuration file without a server restart using `/hrw reload`.
-   **Fully Configurable:** Almost every aspect of the plugin can be changed in the `config.yml` file.
-   **Permission-Based:** Fine-grained permissions allow you to control who can use admin commands and who should be exempt from being kicked.
-   **Lightweight:** The plugin is designed to have a minimal performance impact, running its checks asynchronously and scheduling API calls on the main thread for safety.

## Installation

1.  Download the latest release from the [Releases page](https://github.com/your-username/H-RAMWatch/releases).
2.  Place the `H-RAMWatch-X.X.X.jar` file into your server's `/plugins` directory.
3.  Start your server to generate the `plugins/H-RAMWatch/config.yml` file.
4.  Stop the server, edit the `config.yml` to your liking, and start it again.

## Commands & Permissions

| Command                               | Permission                 | Default    | Description                                                 |
| ------------------------------------- | -------------------------- | ---------- | ----------------------------------------------------------- |
| `/hrw dashboard [server\|worlds\|players]` | `hramwatch.dashboard`      | `true`     | Displays the resource usage dashboard.                      |
| `/hrw toggle`                         | `hramwatch.toggle`         | `op`       | Toggles the AFK kicker on or off live.                      |
| `/hrw reload`                         | `hramwatch.reload`         | `op`       | Reloads the plugin's configuration file.                    |
| *(No command)*                        | `hramwatch.bypass`         | `op`       | Grants immunity from being kicked by the AFK limiter.       |

## Configuration (`config.yml`)

The default configuration is well-balanced, but you can customize it to fit your server.

```yml
# --- H-RAMWatch Configuration ---

# How often (in seconds) the plugin should check players for high resource usage.
check-interval-seconds: 30

afk-kick:
  # Should the AFK kicker be enabled when the server starts?
  enabled-by-default: true

  # Time in minutes a player must be inactive to be considered AFK.
  afk-time-minutes: 10

  # If an AFK player has more than this many chunks loaded in their view, they are a candidate for kicking.
  chunk-threshold: 500

  # If an AFK player has more than this many entities within the check-radius, they are a candidate for kicking.
  entity-threshold: 250

  # The radius (in blocks) around the player to check for entities.
  entity-check-radius: 64

  # Set to true to send a warning message before kicking the player.
  warning-enabled: true
  
  # Message sent to the player if they are over the limits and warning-enabled is true.
  warning-message: "&c[Warning] You are AFK in a resource-intensive area and may be kicked soon."

  # The message shown to the player when they are kicked.
  kick-message: "&cYou were kicked for being AFK in a resource-intensive area.\n&eThis is done to keep the server running smoothly for everyone!"
```

## Building from Source

To build the plugin yourself, you'll need:
-   Java 17 (or newer)
-   Apache Maven

Clone the repository and run the following command from the project's root directory:
```bash
mvn clean package
```
The compiled JAR file will be located in the `target/` directory.
