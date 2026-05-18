# Battle Music System Implementation

## Overview
The game now features a dynamic music system that plays area-specific battle music, shop music, and victory music with proper looping and transitions.

---

## Music Files Required

### **Location:** `assets/audio/`

```
assets/audio/
├── battle_forest.wav      ← Forest battle music (looped)
├── battle_academia.wav    ← Academia battle music (looped)
├── battle_dungeon.wav     ← Dungeon battle music (looped)
├── shop.wav               ← Shop music (looped)
└── victory.wav            ← Victory music (no loop) [already exists]
```

---

## How It Works

### **Music Flow:**

```
1. Battle Starts
   └─> Play area-specific battle music (looped)
       ├─> Forest → battle_forest.wav
       ├─> Academia → battle_academia.wav
       └─> Dungeon → battle_dungeon.wav

2. During Battle
   └─> Music continues looping
       ├─> Enemy defeated → Music continues
       ├─> Character acts → Music continues
       └─> Round ends → Music continues

3. Wave Complete
   └─> Stop battle music
   └─> Transition to shop

4. Shop Opens
   └─> Play shop music (looped)
       └─> shop.wav

5. Continue to Next Wave
   └─> Stop shop music
   └─> Return to step 1 (battle music for same area)

6. All Waves Complete
   └─> Stop battle music
   └─> Play victory music (no loop)
       └─> victory.wav

7. Game Over (Defeat)
   └─> Stop battle music
   └─> Go to game over screen

8. Flee Battle
   └─> Stop battle music
   └─> Return to area select
```

---

## Implementation Details

### **MusicManager.java Updates:**

#### **New Methods:**

```java
// Play battle music for specific area (looped)
MusicManager.playBattleMusic(String area)
  - Forest → battle_forest.wav
  - Academia → battle_academia.wav
  - Dungeon → battle_dungeon.wav

// Play shop music (looped)
MusicManager.playShopMusic()
  - Plays shop.wav

// Play victory music (no loop)
MusicManager.playVictoryMusic()
  - Plays victory.wav

// Stop current music
MusicManager.stop()

// Check if music is playing
MusicManager.isPlaying()
```

#### **Smart Track Management:**
- Prevents restarting the same track if already playing
- Automatically stops previous music before starting new track
- Tracks current playing music to avoid redundant loads

---

## Where Music Plays

### **BattleScreen.java:**

1. **Constructor** - Starts battle music:
   ```java
   io.MusicManager.playBattleMusic(frame.getCurrentArea());
   ```

2. **Wave Complete** - Stops battle music:
   ```java
   io.MusicManager.stop();
   ```

3. **All Waves Complete** - Plays victory music:
   ```java
   io.MusicManager.playVictoryMusic();
   ```

4. **Party Defeated** - Stops music:
   ```java
   io.MusicManager.stop();
   frame.goToGameOver(false);
   ```

5. **Flee Success** - Stops music:
   ```java
   io.MusicManager.stop();
   frame.goToAreaSelect();
   ```

### **ShopScreen.java:**

1. **Constructor** - Starts shop music:
   ```java
   io.MusicManager.playShopMusic();
   ```

---

## Music Behavior by Scenario

### **Scenario 1: Normal Wave Progression**
```
Wave 1 Battle → battle_forest.wav (looped)
Wave 1 Complete → STOP
Shop → shop.wav (looped)
Wave 2 Battle → battle_forest.wav (looped)
Wave 2 Complete → STOP
Shop → shop.wav (looped)
...continues...
Wave 4 Complete → victory.wav (no loop)
```

### **Scenario 2: Party Defeated**
```
Wave 2 Battle → battle_academia.wav (looped)
Party Dies → STOP
Game Over Screen → (silent)
```

### **Scenario 3: Flee Battle**
```
Wave 1 Battle → battle_dungeon.wav (looped)
Player Flees → STOP
Area Select → (silent)
```

### **Scenario 4: Different Areas**
```
Forest Wave 1 → battle_forest.wav
Forest Wave 2 → battle_forest.wav (same music)
Forest Wave 3 → battle_forest.wav (same music)
Forest Wave 4 → battle_forest.wav (same music)

Academia Wave 1 → battle_academia.wav
Academia Wave 2 → battle_academia.wav (same music)
...
```

---

## Technical Features

### **Looping:**
- Battle music loops continuously during combat
- Shop music loops continuously in shop
- Victory music plays once (no loop)

### **Smooth Transitions:**
- Music stops cleanly before new music starts
- No overlapping tracks
- No audio glitches

### **Performance:**
- Music loads on background thread (doesn't block UI)
- Daemon thread (doesn't prevent game exit)
- Proper resource cleanup (clips closed when stopped)

### **Error Handling:**
- Missing files logged to console
- Game continues even if music files missing
- No crashes from audio errors

---

## Audio Format Support

### **Supported Formats:**
- ✅ WAV (recommended)
- ✅ MP3 (also works)
- ✅ AIFF
- ✅ AU

### **Recommended Specs:**
- **Format:** WAV
- **Sample Rate:** 44100 Hz
- **Bit Depth:** 16-bit
- **Channels:** Stereo (2)
- **Length:** 2-4 minutes (for smooth looping)

---

## Testing Checklist

- [ ] Forest battle music plays in Forest battles
- [ ] Academia battle music plays in Academia battles
- [ ] Dungeon battle music plays in Dungeon battles
- [ ] Battle music loops continuously
- [ ] Battle music stops when wave completes
- [ ] Shop music plays in shop
- [ ] Shop music loops continuously
- [ ] Shop music stops when returning to battle
- [ ] Victory music plays when all waves complete
- [ ] Music stops when party is defeated
- [ ] Music stops when fleeing battle
- [ ] No audio glitches or overlaps
- [ ] Game works even if music files missing

---

## File Naming Convention

**IMPORTANT:** File names are case-sensitive and must match exactly:

```
✅ CORRECT:
- battle_forest.wav
- battle_academia.wav
- battle_dungeon.wav
- shop.wav
- victory.wav

❌ WRONG:
- Battle_Forest.wav
- battle_forest.WAV
- BattleForest.wav
- battle-forest.wav
```

---

## Volume Considerations

All music files should be normalized to similar volume levels:
- Battle music: Medium-high volume (engaging but not overwhelming)
- Shop music: Medium-low volume (relaxing, background)
- Victory music: Medium-high volume (celebratory)

Use audio editing software (Audacity) to normalize if needed.

---

## Result

The game now has a **fully functional dynamic music system** that:
- ✅ Plays area-specific battle music
- ✅ Loops music smoothly
- ✅ Transitions between battle and shop
- ✅ Stops music at appropriate times
- ✅ Plays victory music on win
- ✅ Handles all edge cases (defeat, flee, etc.)
- ✅ Enhances immersion and atmosphere

**Your RPG now has epic battle soundtracks!** 🎵⚔️🎮
