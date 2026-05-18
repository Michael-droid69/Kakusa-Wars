# Redesigned Combined Selection Screen

## Overview
The selection screen has been completely redesigned with a carousel-based character selection system that mirrors the area selection layout.

---

## New Layout Structure

```
┌─────────────────────────────────────────────────────────────────────────┐
│              UPPER SECTION: AREA SELECTION (SHORTER - 280px)            │
├──────────────────────────────┬──────────────────────────────────────────┤
│  [Lore Scroll - Smaller]     │     ◀  [Landscape 400x200]  ▶           │
│  ┌────────────────────────┐  │     [Framed & Arrows]                    │
│  │ 🌲 Whispering Woods    │  │                                          │
│  │ [Compact Lore Text]    │  │                                          │
│  └────────────────────────┘  │                                          │
└──────────────────────────────┴──────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────────────────┐
│           LOWER SECTION: CHARACTER SELECTION (TALLER - Expanded)        │
├─────────────────────────────────────────────────────────────────────────┤
│                      ⚔ Select Your 2 Heroes                             │
├──────────────────────┬──────────────────────┬───────────────────────────┤
│                      │                      │  ┌──────────────────┐    │
│  ┌────────────────┐  │  ┌────────────────┐ │  │ Selected Heroes  │    │
│  │                │  │  │ ARTHUR         │ │  ├──────────────────┤    │
│  │                │  │  ├────────────────┤ │  │ ┌──────────────┐ │    │
│  │   [Portrait]   │  │  │ Class: Knight  │ │  │ │   Arthur     │ │    │
│◀ │                │ ▶│  │                │ │  │ │   Knight     │ │    │
│  │   380x280      │  │  │ ═══ BACKSTORY  │ │  │ └──────────────┘ │    │
│  │                │  │  │ A noble knight │ │  │                  │    │
│  │                │  │  │ sworn to...    │ │  │ ┌──────────────┐ │    │
│  └────────────────┘  │  │                │ │  │ │   Star       │ │    │
│  [Framed Portrait]   │  │ ═══ STATS ═══  │ │  │ │   Paladin    │ │    │
│  [Left/Right Arrows] │  │ ❤ Health: 150  │ │  │ └──────────────┘ │    │
│                      │  │ ⚔ Attack: 45   │ │  │                  │    │
│                      │  │ 🛡 Defense: 30 │ │  │                  │    │
│                      │  │                │ │  │ [Begin Adventure]│    │
│                      │  │ ═══ ABILITIES  │ │  └──────────────────┘    │
│                      │  │ • Slash (10MP) │ │                          │
│                      │  │ • Shield (15MP)│ │                          │
│                      │  │                │ │                          │
│                      │  │ [SELECT HERO]  │ │                          │
│                      │  └────────────────┘ │                          │
│      LEFT SIDE       │    CENTER PANEL     │      RIGHT SIDE          │
│   (Portrait Frame)   │  (Details & Lore)   │  (Selected Container)    │
└──────────────────────┴─────────────────────┴──────────────────────────┘
```

---

## Key Changes

### **Upper Section (Area Selection):**
- ✅ **Reduced height** from ~400px to **280px**
- ✅ **Smaller landscape** (400x200 instead of 480x300)
- ✅ **Compact lore text** with smaller fonts
- ✅ **Same functionality** - carousel still works perfectly

### **Lower Section (Character Selection):**
- ✅ **Expanded to fill remaining space** (taller, more breathing room)
- ✅ **Complete redesign** - carousel-based instead of horizontal scroll
- ✅ **Three-panel layout:**
  - Left: Portrait with arrows
  - Center: Details and lore
  - Right: Selected heroes container

---

## New Character Selection Design

### **Left Panel - Portrait Carousel (450px width):**

**Features:**
- 🖼️ **Framed horizontal portrait** (380x280px)
  - Dark frame with borders
  - Displays current hero's portrait
  - Supports horizontal portrait images
- ◀▶ **Navigation arrows** outside frame
  - Left arrow: Previous hero
  - Right arrow: Next hero
  - Cycles through all 6 heroes
- 🎨 **Same aesthetic** as area selection

---

### **Center Panel - Hero Details (400px width):**

**Hero Name:**
- Large, prominent display
- Serif Bold 26px
- Golden color

**Details Scroll Panel:**
- 📜 **Parchment-style background**
- 📖 **Rich information:**
  - Class type
  - Backstory/Lore
  - Combat stats (HP, MP, ATK, DEF, SPD)
  - All abilities with descriptions
- 📏 **Scrollable** for long content

**Select Button:**
- **"SELECT HERO"** when not selected
- **"UNSELECT"** when selected
- Purple theme
- Hover effects
- Located at bottom of details panel

---

### **Right Panel - Selected Heroes Container (180px width):**

**Features:**
- 📋 **"Selected Heroes" title**
- 🎴 **Hero slots** (max 2):
  - Shows hero name
  - Shows hero class
  - Compact card design
  - Appears when selected
  - Disappears when unselected
- 🚀 **"Begin Adventure ▶" button** at bottom
  - Always visible
  - Validates 2 heroes selected
  - Starts the battle

**Visual Design:**
- Dark purple background
- Golden borders
- Compact, organized layout
- Clear visual hierarchy

---

## Hero Backstories (New Content)

### **Arthur - The Noble Knight**
> "A noble knight sworn to protect the realm. Arthur wields his blade with honor and courage, leading his companions through the darkest of battles. His unwavering determination inspires all who fight beside him."

### **Fabby - The Arcane Mage**
> "A mysterious mage who commands the arcane arts with precision. Fabby's magical prowess is matched only by her wisdom, making her an invaluable ally in any quest for knowledge and power."

### **Mohammad - The Exotic Warrior**
> "A skilled warrior from distant lands, Mohammad brings exotic fighting techniques and unmatched discipline. His journey has taken him across countless battlefields, honing his skills to perfection."

### **Star - The Celestial Guardian**
> "A celestial guardian blessed with divine powers. Star's radiant energy heals the wounded and smites the wicked, serving as a beacon of hope in the face of overwhelming darkness."

### **Tapanh - The Shadow Rogue**
> "A cunning rogue who strikes from the shadows. Tapanh's agility and precision make him a deadly force, capable of turning the tide of battle with a single well-placed strike."

### **Van - The Battle Veteran**
> "A battle-hardened veteran who has seen countless wars. Van's experience and tactical mind make him an exceptional leader, capable of adapting to any situation with calm resolve."

---

## Detailed Information Display

### **For Each Hero:**

```
═══ BACKSTORY ═══
[Rich lore text about the hero's background and personality]

═══ COMBAT STATS ═══
❤ Health: XXX HP
✦ Mana: XXX MP
⚔ Attack: XX
🛡 Defense: XX
⚡ Speed: XX

═══ ABILITIES ═══
• Skill Name (XX MP)
  Skill description and effects
• Skill Name (XX MP)
  Skill description and effects
• Ultimate (XX MP)
  Ultimate ability description
```

---

## User Flow

### **Selecting Heroes:**

1. **Browse heroes** with ◀▶ arrows
2. **Read their backstory** and stats
3. **Click "SELECT HERO"** button
4. **Hero appears** in right container
5. **Browse and select** second hero
6. **Click "Begin Adventure"** to start

### **Unselecting Heroes:**

1. **Navigate to selected hero** with arrows
2. **Button shows "UNSELECT"**
3. **Click to remove** from selection
4. **Hero disappears** from right container

---

## Visual Improvements

### **Spacing & Breathing Room:**
- ✅ Upper section compressed (more efficient)
- ✅ Lower section expanded (more comfortable)
- ✅ Proper padding and margins
- ✅ No cramped elements

### **Consistency:**
- ✅ Both sections use **same carousel pattern**
- ✅ Both use **framed images with arrows**
- ✅ Both use **parchment-style info panels**
- ✅ Unified RPG aesthetic

### **Clarity:**
- ✅ **Clear visual hierarchy**
- ✅ **Obvious selection state**
- ✅ **Intuitive navigation**
- ✅ **Immediate feedback**

---

## Technical Features

### **Portrait Support:**
- Loads from `assets/portraits/[name].png`
- Supports **horizontal portraits** (landscape orientation)
- Scales to fit 380x280 frame
- Maintains aspect ratio
- Fallback text if image missing

### **Dynamic Updates:**
- Portrait updates when arrows clicked
- Details update instantly
- Selected container updates in real-time
- Button text changes (SELECT ↔ UNSELECT)

### **Validation:**
- Max 2 heroes enforced
- Clear error messages
- Can't proceed without 2 heroes
- Can unselect and reselect freely

---

## Color Palette

### **Character Section:**
- **Frames:** `rgb(20, 18, 25)` - Dark frame
- **Details panel:** `rgb(35, 30, 25)` - Parchment
- **Selected container:** `rgb(25, 20, 30)` - Dark purple
- **Borders:** Golden browns and purples
- **Text:** Cream and gold tones

### **Buttons:**
- **Select/Unselect:** `rgb(70, 40, 90)` - Purple
- **Hover:** `rgb(90, 60, 110)` - Lighter purple
- **Arrows:** `rgba(40, 35, 30, 200)` - Semi-transparent brown

---

## Result

The new design provides:
- ✅ **Better space utilization** (shorter top, taller bottom)
- ✅ **Consistent carousel pattern** (areas and heroes)
- ✅ **Rich hero information** (backstory, stats, abilities)
- ✅ **Clear selection tracking** (right container)
- ✅ **Intuitive navigation** (arrows for both sections)
- ✅ **Professional RPG aesthetic** (frames, parchment, gold accents)
- ✅ **Horizontal portrait support** (no stretching!)

**Players can now browse heroes like browsing areas - with style and detail!** ⚔️🎨
