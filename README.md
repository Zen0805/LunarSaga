# LunarSaga 🌙🗡️

**LunarSaga** is a **2D top-down game prototype** built with **libGDX (Java)**.  
This project started by following a tutorial to learn the workflow (project structure, assets pipeline, screens, input, etc.), then I gradually extend it with my own features.

> Status: **WIP** (learning + iterative development). The goal is to build solid foundations first, then expand gameplay.

---

## Tech Stack
- **Language:** Java
- **Framework:** libGDX
- **Build:** Gradle
- **Desktop backend:** LWJGL3
- **Tools:** TexturePacker (planned/used for sprite atlases), Git

---

## Features (Current / Planned)
### Current
- Basic project structure (core / desktop)
- Asset folder setup and iteration workflow
- Ongoing implementation of screens, input handling, and game loop fundamentals

### Planned
- Screen/State management (Menu, Gameplay, Pause)
- Player controller + animations
- Camera + map/world layout
- Collision & interaction system
- UI/HUD (health, inventory, debug)
- Audio (SFX/BGM)
- Save/load (simple JSON)

---

## Project Structure
Typical libGDX multi-module layout:
LunarSaga/
assets/ # game assets (textures, atlases, sounds, etc.)
core/ # shared game logic (main gameplay code)
lwjgl3/ # desktop launcher (LWJGL3 backend)
utils/ # helper tools/scripts (optional)

---

## Development Notes
This repo is used as a learning journal for libGDX: each feature is implemented step-by-step and cleaned up over time.
I try to keep commits small and meaningful for easier tracking and future reporting.

---

## Credits
Built with libGDX.
Initial structure inspired by a libGDX tutorial workflow; implementation is gradually modified and expanded.
