# UltraTournamentsPlusPlus - Arena & Kit System Implementation

## Overview
This document summarizes the comprehensive arena and kit management system implemented for the UltraTournamentsPlusPlus Spigot plugin. The system provides full support for both PvP and Parkour tournament arenas with WorldGuard integration, along with a complete kit management system for player equipment and state management.

## Features Implemented

### Arena System
- **Arena Types**: Support for PvP and Parkour arena types
- **Spawn Points**: Multiple spawn points per arena with team assignment support
- **WorldGuard Integration**: Automatic region management and permission control
- **Arena Management**: Complete CRUD operations for arena creation, modification, and deletion
- **Validation**: Comprehensive arena validation for proper configuration

### Kit System
- **Equipment Management**: Full inventory, armor, and off-hand item management
- **Player State**: Health, hunger, experience, and potion effect management
- **Kit Application**: Safe kit application with rollback capabilities
- **Kit Validation**: Comprehensive validation for kit integrity

### Tournament Integration
- **Arena Assignment**: Tournaments can be assigned specific arenas
- **Kit Assignment**: Tournaments can use specific kits for participants
- **Tournament Types**: Support for different tournament formats with arena/kit combinations

## File Structure

### Core Models
- `Arena.java` - Arena model with spawn points and WorldGuard region support
- `ArenaType.java` - Enum for PvP and Parkour arena types
- `SpawnPoint.java` - Individual spawn point model with team assignment
- `Kit.java` - Complete kit model with inventory, armor, and effects
- `Tournament.java` - Updated to support arena and kit assignments

### Management Classes
- `ArenaManager.java` - Arena lifecycle management with WorldGuard integration
- `KitManager.java` - Kit creation, application, and player state management

### Command Handlers
- `ArenaCommand.java` - Arena management commands with tab completion
- `KitCommand.java` - Kit management commands with comprehensive subcommands

### Utility Classes
- `ArenaUtils.java` - Arena validation, player management, and utility functions
- `KitUtils.java` - Kit validation, application, and comparison utilities

## Configuration

### Arena Settings (config.yml)
```yaml
arenas:
  worldguard:
    auto-create-regions: true
    pvp-arena-flags:
      pvp: "allow"
      build: "deny"
    parkour-arena-flags:
      pvp: "deny"
      build: "deny"
  validation:
    min-spawn-distance: 5.0
    goal-tolerance: 2.0
  pvp:
    min-spawn-points: 2
    default-teams: ["Red", "Blue"]
  parkour:
    min-spawn-points: 1
    require-goal: true
```

### Kit Settings (config.yml)
```yaml
kits:
  validation:
    max-items: 36
    max-potion-effects: 10
  defaults:
    give-food: true
    food-level: 20
    clear-inventory: true
    full-health: true
  permissions:
    allow-player-creation: false
    require-kit-permissions: false
```

## Command Reference

### Arena Commands
- `/arena create <name> <type>` - Create a new arena
- `/arena delete <name>` - Delete an arena
- `/arena info <name>` - View arena information
- `/arena list` - List all arenas
- `/arena addspawn <arena> <name> [team]` - Add spawn point
- `/arena setgoal <arena>` - Set parkour goal location
- `/arena teleport <arena> [spawn]` - Teleport to arena

### Kit Commands
- `/kit create <name>` - Create kit from current inventory
- `/kit delete <name>` - Delete a kit
- `/kit info <name>` - View kit information
- `/kit list` - List all kits
- `/kit apply <name> [player]` - Apply kit to player
- `/kit save <name>` - Save current player state as kit

### Tournament Commands (Updated)
- `/tournament create <name> --arena <arena>` - Create tournament with arena
- `/tournament create <name> --kit <kit>` - Create tournament with kit
- `/tournament setarena <tournament> <arena>` - Assign arena to tournament
- `/tournament setkit <tournament> <kit>` - Assign kit to tournament

## Technical Implementation

### WorldGuard Integration
- Automatic region creation for new arenas
- Region flag management based on arena type
- Player region checking and management
- Permission-based region access control

### Arena Types

#### PvP Arenas
- Minimum 2 spawn points for team-based combat
- Team assignment support (Red/Blue by default)
- Configurable build permissions per arena
- PvP enabled by default

#### Parkour Arenas
- Single or multiple spawn points
- Goal location for completion detection
- PvP disabled for safety
- Build restrictions for course integrity

### Kit Management
- Complete player state capture and restoration
- Safe kit application with error handling
- Kit comparison and validation utilities
- Support for complex inventory arrangements

### Tournament Integration
- Optional arena assignment for location-based tournaments
- Optional kit assignment for standardized equipment
- Tournament validation ensures proper arena/kit configuration
- Player preparation system for tournament start

## Dependencies
- Spigot API 1.20.1
- WorldGuard 7.0.9
- WorldEdit 7.2.17
- Java 17

## Usage Examples

### Creating a PvP Arena
1. `/arena create pvparena1 PVP`
2. `/arena addspawn pvparena1 redspawn Red`
3. `/arena addspawn pvparena1 bluespawn Blue`
4. Arena automatically gets WorldGuard region with PvP enabled

### Creating a Parkour Arena
1. `/arena create parkour1 PARKOUR`
2. `/arena addspawn parkour1 start`
3. `/arena setgoal parkour1` (while standing at goal location)
4. Arena gets WorldGuard region with PvP disabled

### Creating a Tournament Kit
1. Equip desired items and armor
2. `/kit create pvpkit1`
3. Kit saves current inventory, armor, and player state

### Setting Up a Tournament
1. `/tournament create championship --arena pvparena1 --kit pvpkit1`
2. Tournament now has designated arena and standardized equipment
3. Players automatically get the kit when tournament starts

## Validation & Safety
- All arenas validated for proper spawn point configuration
- Kits validated for item integrity and effect safety
- Player state restoration on tournament completion
- WorldGuard integration ensures proper permissions
- Comprehensive error handling and logging

This implementation provides a complete, production-ready arena and kit management system that integrates seamlessly with the existing tournament framework while maintaining safety, flexibility, and ease of use.