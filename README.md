# UltraTournamentsPlusPlus

[![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=flat&logo=java&logoColor=white)](https://www.oracle.com/java/)
[![Spigot](https://img.shields.io/badge/Spigot-1.20.1+-FF5722?style=flat&logo=minecraft&logoColor=white)](https://www.spigotmc.org/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-C71A36?style=flat&logo=apache-maven&logoColor=white)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

Tournament manager plugin for Minecraft/Spigot with Challonge support and a Discord portal for managing tournaments, announcing rounds live, and allowing players to join.

## Features

### 🏆 Tournament Management
- **Multiple Tournament Types**: Single Elimination, Double Elimination, Round Robin, Swiss System
- **Flexible Configuration**: Customizable participant limits, auto-start options, and cleanup settings
- **Player Management**: Easy join/leave system with permission controls
- **Real-time Status**: Live tournament status tracking and updates

### 🌐 External Integrations
- **Challonge API**: Automatic bracket creation and synchronization
- **Discord Bot**: Live announcements, match results, and tournament updates
- **Economy Support**: Entry fees and prize pools (if economy plugin available)

### 💾 Data Management
- **Multi-Database Support**: SQLite (default) and MySQL/PostgreSQL
- **Persistent Storage**: Tournament data survives server restarts
- **Backup & Recovery**: Automatic data preservation

### 🎮 Player Experience
- **Intuitive Commands**: Simple `/tournjoin`, `/tournleave`, `/tournlist` commands
- **Rich Information**: Detailed tournament info with `/tourninfo`
- **Permission System**: Granular control over player and admin capabilities

## Quick Start

### Installation
1. Download the latest release from [Releases](https://github.com/SansCraft-Network/UltraTournamentsPlusPlus/releases)
2. Place the JAR file in your server's `plugins/` directory
3. Restart your server
4. Configure the plugin in `plugins/UltraTournamentsPlusPlus/config.yml`

### Basic Configuration
```yaml
# Enable basic tournament features
tournaments:
  default-max-participants: 16
  default-type: "SINGLE_ELIMINATION"
  allow-player-creation: false

# Optional: Enable Discord integration
discord:
  enabled: true
  token: "YOUR_BOT_TOKEN"
  guild-id: "YOUR_GUILD_ID"
  announcement-channel: "YOUR_CHANNEL_ID"
```

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/tournament` | Main admin command | `ultratournaments.admin` |
| `/tournjoin <name>` | Join a tournament | `ultratournaments.player.join` |
| `/tournleave <name>` | Leave a tournament | `ultratournaments.player.leave` |
| `/tournlist` | List all tournaments | `ultratournaments.player.list` |
| `/tourninfo <name>` | Get tournament info | `ultratournaments.player.info` |

## Permissions

```yaml
ultratournaments.*          # All permissions
├── ultratournaments.admin  # Admin permissions
│   ├── *.create           # Create tournaments
│   ├── *.delete           # Delete tournaments
│   ├── *.start            # Start tournaments
│   ├── *.stop             # Stop tournaments
│   └── *.config           # Modify configuration
└── ultratournaments.player.* # Player permissions
    ├── *.join            # Join tournaments
    ├── *.leave           # Leave tournaments
    ├── *.list            # View tournament list
    └── *.info            # View tournament info
```

## Development

### Building from Source
```bash
git clone https://github.com/SansCraft-Network/UltraTournamentsPlusPlus.git
cd UltraTournamentsPlusPlus
mvn clean package
```

### Development Environment
- **Java**: 17+
- **Maven**: 3.6+
- **Spigot**: 1.20.1+
- **IDE**: VS Code with Java Extension Pack (recommended)

See [DEVELOPMENT.md](DEVELOPMENT.md) for detailed development setup.

## API Integration

### Challonge
1. Get your API key from [Challonge Developer Settings](https://challonge.com/settings/developer)
2. Configure in `config.yml`:
```yaml
challonge:
  enabled: true
  api-key: "YOUR_API_KEY"
```

### Discord Bot
1. Create a bot at [Discord Developer Portal](https://discord.com/developers/applications)
2. Add bot to your server with appropriate permissions
3. Configure in `config.yml`:
```yaml
discord:
  enabled: true
  token: "YOUR_BOT_TOKEN"
  guild-id: "YOUR_GUILD_ID"
  announcement-channel: "CHANNEL_ID"
```

## Support & Documentation

- **Issues**: [GitHub Issues](https://github.com/SansCraft-Network/UltraTournamentsPlusPlus/issues)
- **Discord**: [SansCraft Network Discord](https://discord.gg/sanscraft)
- **Wiki**: [Project Wiki](https://github.com/SansCraft-Network/UltraTournamentsPlusPlus/wiki)

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- **Spigot/Bukkit** - Minecraft server API
- **Challonge** - Tournament bracket platform
- **Discord** - Communication platform
- **JDA** - Java Discord API wrapper

---

Made with ❤️ by [SansCraft Network](https://github.com/SansCraft-Network)
