# Combined Area + Character Selection Screen

## Overview
The character selection and area selection have been merged into a single, streamlined screen for faster gameplay flow.

---

## Layout Structure

```
┌─────────────────────────────────────────────────────────────────────┐
│                    UPPER SECTION: AREA SELECTION                    │
├──────────────────────────────┬──────────────────────────────────────┤
│                              │                                      │
│  ┌────────────────────────┐  │    ┌──────────────────────────┐    │
│  │  🌲 Whispering Woods   │  │    │                          │    │
│  ├────────────────────────┤  │    │                          │    │
│  │ ┌──────────────────┐   │  │ ◀  │   [Landscape Image]      │  ▶ │
│  │ │ Deep within the  │   │  │    │                          │    │
│  │ │ ancient forest,  │   │  │    │      480 x 300           │    │
│  │ │ twisted creatures│   │  │    │                          │    │
│  │ │ lurk beneath...  │   │  │    └──────────────────────────┘    │
│  │ │                  │   │  │         [Framed Landscape]          │
│  │ │ Enemies: Goblins │   │  │      [Left/Right Arrows]            │
│  │ │ Boss: Dragon     │   │  │                                      │
│  │ │                  │   │  │                                      │
│  │ │ Difficulty: EASY │   │  │                                      │
│  │ └──────────────────┘   │  │                                      │
│  │   [Scroll-like Lore]   │  │                                      │
│  └────────────────────────┘  │                                      │
│         LEFT SIDE            │           RIGHT SIDE                 │
├──────────────────────────────┴──────────────────────────────────────┤
│                   LOWER SECTION: CHARACTER SELECTION                │
├─────────────────────────────────────────────────────────────────────┤
│                    ⚔ Select Your 2 Heroes                           │
│  ┌────┐  ┌────┐  ┌────┐  ┌────┐  ┌────┐  ┌────┐                  │
│  │ ✓  │  │    │  │    │  │ ✓  │  │    │  │    │                  │
│  │Art │  │Fab │  │Moh │  │Sta │  │Tap │  │Van │  [Horizontal     │
│  │hur │  │by  │  │ammad│  │r   │  │anh │  │    │   Scroll]       │
│  └────┘  └────┘  └────┘  └────┘  └────┘  └────┘                  │
│                                                                     │
│                    [Begin Adventure ▶]                              │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Features

### **Upper Section: Area Selection**

#### Left Side - Lore Scroll (500px width):
- **Area name** with color-coded title
- **Scroll-like panel** with parchment aesthetic
  - Brown/tan background
  - Multiple border layers for depth
  - Scrollable text area
- **Rich lore text** for each area:
  - Atmospheric description
  - Enemy types
  - Boss information
  - Difficulty rating with stars
  - Recommendations

#### Right Side - Landscape Carousel (550px width):
- **Framed landscape image** (480x300px)
  - Dark frame with borders
  - Displays current area's landscape
- **Navigation arrows** (◀ ▶)
  - Positioned outside the frame
  - Left arrow: Previous area
  - Right arrow: Next area
  - Hover effects for feedback
- **Carousel behavior**:
  - Cycles through 3 areas
  - Wraps around (Forest → Academia → Dungeon → Forest)
  - Updates lore text when changed

---

### **Lower Section: Character Selection**

#### Hero Cards (180x180px each):
- **Compact size** - fits screen without stretching
- **Portrait backgrounds** from assets/portraits/
- **Bottom gradient overlay** for name visibility
- **Character name** centered at bottom
- **Selection indicator**:
  - Normal: Thin gray border
  - Selected: Thick golden border + ✓ badge
- **Click to select/deselect**
- **Maximum 2 heroes** enforced

#### Horizontal Scroll:
- All 6 heroes in a row
- Smooth horizontal scrolling
- No vertical scroll needed
- Proper spacing between cards

#### Confirm Button:
- **"Begin Adventure ▶"** button
- Purple theme matching game aesthetic
- Hover effect
- Validates 2 heroes selected
- Starts battle with selected area + heroes

---

## Area Data

### 🌲 **Whispering Woods** (Forest)
**Difficulty:** ★☆☆ EASY  
**Color:** Green (#64C864)

**Lore:**
> Deep within the ancient forest, twisted creatures lurk beneath the canopy. Goblins and trolls have claimed these woods as their domain, terrorizing nearby villages. Legends speak of a great dragon that slumbers in the heart of the forest, guarding treasures beyond imagination.

**Enemies:** Goblins, Trolls, Forest Beasts  
**Boss:** Ancient Forest Dragon  
**Recommended:** For beginners seeking adventure

---

### 🏚 **Cursed Academia** (Academia)
**Difficulty:** ★★☆ MEDIUM  
**Color:** Purple (#A064DC)

**Lore:**
> Once a prestigious academy of magic, now a haunted ruin where dark forces reign. Skeletons of fallen students wander the halls, and corrupted mages practice forbidden arts. The Dark Mage Lord has claimed the grand library as his throne room, commanding legions of undead.

**Enemies:** Skeleton Warriors, Dark Mages, Cursed Spirits  
**Boss:** Dark Mage Lord  
**Recommended:** For experienced adventurers ready for a challenge

---

### 🌋 **Infernal Depths** (Dungeon)
**Difficulty:** ★★★ HARD  
**Color:** Red/Orange (#FF6432)

**Lore:**
> In the deepest chambers beneath the volcano, where molten rock flows like rivers, the Lava Titans have awakened from their ancient slumber. These colossal beings of fire and stone guard the path to the Titan King's throne—a creature of pure elemental fury.

**Enemies:** Lava Elementals, Fire Golems, Magma Serpents  
**Boss:** Lava Titan King  
**Recommended:** For master warriors seeking ultimate glory

---

## Design Elements

### Color Palette:

**Background:**
- Main: `rgb(12, 10, 18)` - Dark purple-black
- Lore scroll: `rgb(35, 30, 25)` - Parchment brown
- Frame: `rgb(20, 18, 25)` - Dark frame

**Borders:**
- Scroll: `rgb(100, 80, 50)` - Golden brown
- Frame: `rgb(80, 70, 60)` - Dark wood
- Separator: `rgb(60, 50, 70)` - Purple-gray

**Text:**
- Area names: Color-coded per area
- Lore text: `rgb(220, 210, 190)` - Cream
- Hero title: `rgb(240, 200, 120)` - Gold

**Buttons:**
- Arrows: `rgba(40, 35, 30, 200)` - Semi-transparent brown
- Confirm: `rgb(70, 40, 90)` - Purple

---

## Typography

- **Area names:** Serif Bold 28px
- **Lore text:** Serif Plain 14px
- **Hero title:** Serif Bold 20px
- **Hero names:** Serif Bold 15px
- **Arrows:** Serif Bold 32px
- **Confirm button:** Serif Bold 16px

---

## User Experience Improvements

### Before (2 Separate Screens):
1. ❌ Select characters → Click confirm
2. ❌ Wait for screen transition
3. ❌ Select area → Click area
4. ❌ Start battle

### After (Combined Screen):
1. ✅ Browse areas with arrows (instant)
2. ✅ Read lore while browsing
3. ✅ Select 2 heroes (same screen)
4. ✅ Click "Begin Adventure" → Start!

**Result:** Faster, more intuitive, less clicking!

---

## Interactive Elements

### Area Carousel:
- **Left/Right arrows** cycle through areas
- **Instant updates** to lore and landscape
- **Smooth transitions** (no loading)
- **Hover feedback** on arrows

### Hero Selection:
- **Click to select/deselect**
- **Visual feedback** (border + badge)
- **Max 2 enforcement** with popup
- **Horizontal scroll** for all heroes

### Validation:
- Must select exactly 2 heroes
- Clear error messages
- Can't proceed without valid selection

---

## Technical Implementation

### Key Components:

1. **AreaData Class**
   - Stores area information
   - Name, lore, difficulty, image path, color

2. **Carousel System**
   - `currentAreaIndex` tracks position
   - Arrow buttons cycle through areas
   - `updateAreaDisplay()` refreshes UI

3. **Hero Selection**
   - `selectedHeroes` list (max 2)
   - Visual feedback on cards
   - Validation before proceeding

4. **Layout**
   - `BorderLayout` for main structure
   - Upper: Area selection (CENTER)
   - Lower: Hero selection (SOUTH, 280px height)

---

## Integration

### MainFrame Updates:
- `goToCharacterSelect()` → redirects to combined screen
- `goToAreaSelect()` → redirects to combined screen
- `goToCombinedSelect()` → new method for combined screen

### Backward Compatibility:
- Old methods still work (redirect to new screen)
- No changes needed in other parts of code
- Seamless integration

---

## Result

Players now have a **streamlined, immersive selection experience**:
- ✅ Browse areas with rich lore
- ✅ See beautiful landscape images
- ✅ Select heroes on the same screen
- ✅ Faster gameplay flow
- ✅ More engaging presentation
- ✅ Professional RPG aesthetic

**One screen, complete selection, ready for adventure!** ⚔️🗺️
