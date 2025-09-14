# UltraTournamentsPlusPlus Development Guide

## Project Overview
UltraTournamentsPlusPlus is a comprehensive tournament management plugin for Minecraft/Spigot servers with Challonge integration and Discord portal support.

## Features
- Tournament creation and management
- Multiple tournament types (Single/Double Elimination, Round Robin, Swiss)
- Challonge API integration for bracket management
- Discord bot integration for announcements and live updates
- Player registration and management
- Comprehensive permission system

## Development Setup

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- VS Code with Java Extension Pack
- Spigot 1.20.1+ server for testing

### Building the Project
1. Open terminal in project root
2. Run `mvn clean package` to build
3. Find the compiled JAR in `target/` directory

### VS Code Tasks
- **Maven: Clean** - Clean build artifacts
- **Maven: Compile** - Compile source code
- **Maven: Package** - Build JAR file
- **Maven: Test** - Run unit tests
- **Copy to Test Server** - Copy built JAR to test server

### Project Structure
```
src/
├── main/
│   ├── java/network/sanscraft/ultratournamentsplusplus/
│   │   ├── UltraTournamentsPlusPlus.java (Main plugin class)
│   │   ├── commands/ (Command handlers)
│   │   ├── listeners/ (Event listeners)
│   │   ├── managers/ (Core business logic)
│   │   ├── models/ (Data models)
│   │   ├── config/ (Configuration management)
│   │   ├── discord/ (Discord integration)
│   │   ├── storage/ (Database management)
│   │   ├── api/ (External API integrations)
│   │   └── utils/ (Utility classes)
│   └── resources/
│       ├── plugin.yml (Plugin metadata)
│       └── config.yml (Default configuration)
└── test/ (Unit tests)
```

## Configuration

### Main Config (config.yml)
The plugin supports extensive configuration including:
- Tournament settings (default types, participant limits)
- Discord integration (bot token, channels)
- Challonge API integration
- Database settings
- Message customization

### Plugin.yml
Defines plugin metadata, commands, and permissions.

## Commands
- `/tournament` - Main admin command for tournament management
- `/tournjoin` - Join a tournament
- `/tournleave` - Leave a tournament
- `/tournlist` - List all tournaments
- `/tourninfo` - Get tournament information

## Permissions
- `ultratournaments.*` - All permissions
- `ultratournaments.admin` - Admin permissions
- `ultratournaments.player.*` - Player permissions

## Dependencies
- **Spigot API** - Minecraft server integration
- **JDA** - Discord bot functionality
- **Gson** - JSON handling for Challonge API
- **OkHttp** - HTTP client for API calls
- **SLF4J** - Logging framework

## Development Notes
- The project uses Maven for dependency management and building
- Code follows Google Java Style guidelines
- All external dependencies are shaded to avoid conflicts
- Database abstraction supports SQLite and MySQL
- Discord integration is optional and configurable

## Testing
Set up a local Spigot server for testing:
1. Download Spigot 1.20.1+
2. Configure the "Copy to Test Server" task path
3. Use VS Code tasks to build and deploy

## Contributing
1. Follow the existing code structure and patterns
2. Add appropriate JavaDoc comments
3. Include unit tests for new functionality
4. Update configuration documentation as needed