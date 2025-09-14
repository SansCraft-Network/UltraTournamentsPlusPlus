# Round Tracking and Spectator System Implementation

## Overview
This document details the complete implementation of the round tracking and spectator management system for UltraTournamentsPlusPlus. The system provides automatic round management with winner detection, countdown timers, and comprehensive spectator functionality with WorldGuard region integration.

## Features Implemented

### Round Tracking System
- **Automatic Winner Detection**: Automatically declares winners when competitors are defeated in PvP rounds
- **3-Second Countdown Timer**: Configurable countdown before rounds start with visual and audio feedback  
- **Player State Management**: Saves and restores player inventories, health, and effects
- **Arena Integration**: Seamless integration with arena system for teleportation and region validation
- **Kit Application**: Automatic kit application when rounds start
- **Death Prevention**: Prevents item drops and experience loss during tournament rounds

### Spectator System
- **WorldGuard Integration**: Uses WorldGuard regions to control spectator access
- **Tournament Viewing**: Players can spectate specific tournaments from designated regions
- **Admin Management**: Comprehensive admin tools for managing spectator regions and teleport locations
- **Permission Control**: Granular permission system for spectator access and administration

## File Structure

### Core Models
- `Round.java` - Complete round model with status tracking, participant management, and validation
- Updated `Tournament.java` - Enhanced with arena/kit assignment support

### Management Classes
- `RoundManager.java` - Round lifecycle management with countdown timers and winner detection
- `SpectatorManager.java` - Spectator region and tournament viewing management

### Event Listeners
- `RoundListener.java` - Automatic winner detection for player deaths and disconnections

### Commands
- Enhanced `TournamentCommand.java` - Spectator commands and admin region management

## Command Reference

### Player Commands
- `/tournament spectate <tournament>` - Spectate a tournament (requires spectator region)
- `/tournament spectatorarea` - Teleport to spectator area

### Admin Commands
- `/tournament spectatorregion set <world> <region>` - Set spectator region for a world
- `/tournament spectatorregion remove <world>` - Remove spectator region from world
- `/tournament spectatorregion list` - List all configured spectator regions
- `/tournament spectatorarea set` - Set spectator teleport location

## Configuration

### Round Management Settings (config.yml)
```yaml
rounds:
  default-preparation-time: 3
  auto-detect-winners: true
  cleanup-delay: 3
  announcements:
    countdown-messages: true
    countdown-sounds: true
    round-start: true
    round-winners: true
  pvp-rounds:
    prevent-death-drops: true
    keep-inventory: true
    teleport-defeated: true
  validation:
    min-participants: 2
    max-participants: 16
```

### Spectator System Settings (config.yml)
```yaml
spectator:
  enabled: true
  permissions:
    spectate-permission: "ultratournaments.spectate"
    admin-permission: "ultratournaments.admin.spectator"
  regions: {} # Configured via commands
  settings:
    auto-teleport: false
    show-tournament-info: true
    send-updates: true
```

### Permissions (plugin.yml)
```yaml
ultratournaments.spectate:
  description: Allows spectating tournaments
  default: true

ultratournaments.admin.spectator:
  description: Allows managing spectator regions and settings
  default: op
```

## Technical Implementation

### Round Lifecycle Management

#### Round States
1. **PENDING** - Round created but not started
2. **PREPARING** - Players being teleported, countdown starting
3. **ACTIVE** - Round in progress
4. **COMPLETED** - Round finished with winner
5. **CANCELLED** - Round was cancelled

#### Round Start Process
1. Create round with participants, arena, and kit assignments
2. Start preparation phase - teleport players to arena spawn points
3. Apply kits to participants
4. Save player states for restoration
5. Begin countdown timer with audio/visual feedback
6. Start active round phase
7. Monitor for winner conditions

#### Winner Detection
- **PvP Rounds**: Automatic detection on player death or disconnect
- **1v1 Matches**: Remaining player wins automatically
- **Multi-player**: Last player standing wins
- **Region Validation**: Only deaths within arena regions count
- **Item Protection**: Prevents drops and experience loss

### Spectator System Architecture

#### WorldGuard Integration
- **Region Validation**: Checks if players are in configured spectator regions
- **Permission Control**: Only players in spectator regions can use spectate commands
- **Teleport Management**: Configurable teleport locations within spectator regions

#### Spectator Management
- **Tournament Association**: Links spectators to specific tournaments
- **State Tracking**: Maintains spectator-tournament relationships
- **Configuration Persistence**: Saves spectator regions and teleport locations

## Usage Examples

### Setting Up a PvP Tournament with Rounds
1. Create arena: `/arena create pvparena1 PVP`
2. Add spawn points: `/arena addspawn pvparena1 redspawn Red`
3. Create kit: `/kit create pvpkit1`
4. Create tournament with arena and kit assignments
5. Start round: Plugin automatically manages countdown and winner detection

### Configuring Spectator System
1. Create WorldGuard region: `/rg define spectator_lobby`
2. Set spectator region: `/tournament spectatorregion set world spectator_lobby`
3. Set teleport location: `/tournament spectatorarea set` (while standing in desired location)
4. Players can now use: `/tournament spectate <tournament>` from within the region

### Round Execution Flow
1. **Preparation Phase**: 
   - Players teleported to arena spawns
   - Kits applied
   - Player states saved
   - Countdown messages sent

2. **Active Phase**:
   - "FIGHT!" announcement
   - Winner detection active
   - Death/disconnect monitoring

3. **Completion Phase**:
   - Winner announced
   - Player states restored
   - Round cleanup after delay

## Safety Features

### Player Protection
- **State Restoration**: Complete inventory, health, and effect restoration
- **Item Protection**: No item loss during tournament rounds
- **Experience Protection**: No experience loss on death
- **Teleportation Safety**: Validates locations before teleporting

### System Validation
- **Round Validation**: Comprehensive checks before round start
- **Arena Validation**: Ensures proper arena configuration
- **Kit Validation**: Validates kit integrity before application
- **Permission Validation**: Checks permissions before allowing actions

### Error Handling
- **Graceful Failures**: Proper cleanup on errors
- **Player Notification**: Clear error messages to players
- **Logging**: Comprehensive logging for debugging
- **Rollback**: State restoration on failures

## Integration Points

### Arena System Integration
- **Spawn Point Management**: Uses arena spawn points for player positioning
- **Team Assignment**: Supports team-based spawn assignment for PvP
- **Region Validation**: Validates deaths occur within arena regions
- **Goal Detection**: Future support for parkour completion detection

### Tournament System Integration
- **Tournament Phases**: Integrates with tournament lifecycle management
- **Participant Management**: Works with tournament participant lists
- **Status Updates**: Updates tournament status based on round progress
- **Result Recording**: Records round results for tournament brackets

### Kit System Integration
- **Automatic Application**: Applies tournament kits to round participants
- **State Management**: Saves player states before kit application
- **Restoration**: Restores original states after round completion
- **Validation**: Ensures kit compatibility with round requirements

## Performance Considerations

### Efficient Operations
- **State Caching**: Caches player states for quick restoration
- **Region Queries**: Optimized WorldGuard region checking
- **Timer Management**: Efficient countdown timer implementation
- **Memory Management**: Proper cleanup of completed rounds

### Scalability
- **Multiple Rounds**: Support for concurrent rounds
- **Large Tournaments**: Handles tournaments with many participants
- **Region Management**: Efficient spectator region management
- **State Storage**: Optimized player state storage

This implementation provides a complete, production-ready round tracking and spectator system that seamlessly integrates with the existing tournament infrastructure while maintaining safety, performance, and ease of use.