# 🎮 Maze Runner Game

A Java-based maze game built with LibGDX framework where players navigate through challenging mazes, collect keys, avoid enemies and traps, and reach the exit before time runs out!

## 📋 Table of Contents
- [Features](#features)
- [How to Run](#how-to-run)
- [How to Play](#how-to-play)
- [Game Mechanics](#game-mechanics)
- [Controls](#controls)
- [Technologies Used](#technologies-used)

## ✨ Features

- **5 Challenging Levels** - Progressive difficulty across 5 handcrafted maze levels
- **Endless Mode** - Procedurally generated mazes for unlimited gameplay
- **Level Progression** - Complete levels sequentially with automatic progression
- **Multiple Enemy Types**:
  - Patrol Enemies - Follow predefined paths
  - Chaser Enemies - Hunt the player when in range
- **Interactive Traps**:
  - Damage Traps - Cause instant damage
  - Sludge Traps - Slow down player movement
- **Power-ups**:
  - Health Boost - Restore one life
  - Speed Boost - Temporary movement speed increase
- **Combat System** - Attack and defeat enemies for bonus points
- **Score System** - Earn points by collecting items, defeating enemies, and completing levels quickly
- **Background Music** - Immersive audio experience
- **Dynamic Camera** - Follows player movement with zoom controls

## 🚀 How to Run

### Prerequisites
- Java 17 or higher
- Gradle (included via wrapper)

### Running the Game

**Windows:**
```bash
./gradlew desktop:run
```

**Linux/Mac:**
```bash
./gradlew desktop:run
```

## 🎯 How to Play

### Objective
Navigate through the maze, collect at least one key, and reach the exit before time runs out while staying alive!

### Win Condition
1. Collect at least **1 KEY** (required to unlock the exit)
2. Find and reach the **EXIT**
3. Complete within the **120-second time limit**
4. Stay alive (don't lose all 5 lives)

### Losing Condition
- Time runs out (120 seconds)
- All lives are lost (HP reaches 0)

## 🎮 Game Mechanics

### Game Elements

| Element | Description | Points |
|---------|-------------|--------|
| 🔑 **Key** | Required to unlock the exit. At least one must be collected. | +100 |
| 🚪 **Exit** | The goal! Can only enter with a key. | Victory! |
| 💚 **Health Power-up** | Restores 1 life | +50 |
| ⚡ **Speed Power-up** | 1.5x speed boost for 2.5 seconds (green tint) | +50 |
| 👹 **Enemies** | Patrol or chase the player. Touch = lose 1 life. | +100 (when defeated) |
| 🔴 **Damage Trap** | Causes instant damage when stepped on | - |
| 🔵 **Sludge Trap** | Significantly reduces movement speed | - |

### HUD Information
- **Lives**: Current health (starts at 5)
- **Key**: Shows if key is collected (COLLECTED/MISSING)
- **Time**: Countdown timer (120 seconds)
- **Score**: Current score
- **Exit Arrow**: Directional indicator pointing to the exit

## 🕹️ Controls

| Action | Key |
|--------|-----|
| **Move Up** | ↑ / W |
| **Move Down** | ↓ / S |
| **Move Left** | ← / A |
| **Move Right** | → / D |
| **Sprint** | Shift (configurable) |
| **Attack** | Space (configurable) |
| **Pause** | ESC |
| **Zoom In** | + / = |
| **Zoom Out** | - |

## 🛠️ Technologies Used

- **Language**: Java 17
- **Framework**: LibGDX (Game Development Framework)
- **Build Tool**: Gradle
- **Graphics**: 2D Sprite-based rendering
- **Audio**: MP3 background music support

## 📁 Project Structure

```
├── core/                      # Core game logic
│   └── src/de/tum/cit/fop/maze/
│       ├── MazeRunnerGame.java        # Main game class
│       ├── GameScreen2.java           # Main gameplay screen
│       ├── MenuScreen.java            # Main menu
│       ├── GameMap.java               # Map loading and rendering
│       └── objects/                   # Game entities
│           ├── Player.java
│           ├── enemies/
│           ├── traps/
│           └── powerups/
├── desktop/                   # Desktop launcher
├── assets/                    # Game resources
│   ├── maps/                  # Level files
│   ├── *.png                  # Textures
│   └── background.mp3         # Music
└── maps/                      # Source map files
    ├── level-1.properties
    ├── level-2.properties
    ├── level-3.properties
    ├── level-4.properties
    ├── level-5.properties
    └── endless.properties
```

## 🐛 Known Issues

- Key texture visibility may vary depending on screen
- File chooser dialog may show errors on some systems (fallback to level-1 works)

## 🎓 Credits

Developed as part of a university project for Fundamentals of Programming course.

## 📝 License

This project is for educational purposes.

---

**Enjoy the game! 🎮**