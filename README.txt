
╔══════════════════════════════════════════════════════════════════════════════════╗
║              🗡️  KAKUSA WARS — RPG BATTLE SYSTEM                               ║
║              📖  PROJECT GUIDE, OOP CHEAT SHEET & DEFENCE NOTES                ║
╚══════════════════════════════════════════════════════════════════════════════════╝

  This file is a READ-ONLY reference. It does NOT affect compilation or gameplay.
  Use it for your project defence, code walkthroughs, and quick lookups.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  📁  PROJECT STRUCTURE AT A GLANCE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  src/
  ├── Main.java                    ← Entry point. Launches the Swing window.
  ├── core/
  │   ├── Character.java           ← Abstract base class for ALL fighters
  │   ├── BattleEngine.java        ← All combat math, AI, wave spawning, gold
  │   ├── Skill.java               ← Immutable skill data (name, cost, multiplier)
  │   └── Item.java                ← Immutable item data + applyTo() logic
  ├── characters/
  │   ├── Arthur.java              ← Warrior — tank, Iron Skin passive
  │   ├── Fabby.java               ← Support — healer/debuffer, Waterworks passive
  │   ├── Mohammad.java            ← Demolitions — highest ATK, Volatility passive
  │   ├── Star.java                ← Half-Dragon — max HP/DEF, Dragon Hide passive
  │   ├── Tapanh.java              ← Fighter — fastest, Reflex counter passive
  │   └── Van.java                 ← Noble — lucky crits, Old Money mana passive
  ├── enemies/
  │   ├── Bat.java                 ← Weak flyer (Forest boss in wave 4)
  │   ├── Fish.java                ← Slippery brawler (Volcano area)
  │   ├── Grim.java                ← Shadow brute (Dungeon boss in wave 4)
  │   ├── Horse.java               ← Stubborn bruiser (Forest/Volcano)
  │   ├── Kapre.java               ← Watchful bruiser (Forest)
  │   └── Witch.java               ← Hexer (Dungeon)
  ├── ui/
  │   ├── MainFrame.java           ← Central hub — holds ALL shared game state
  │   ├── BattleScreen.java        ← Battle UI — sprites, turns, buttons, log
  │   ├── ShopScreen.java          ← Between-wave shop — buy items with gold
  │   ├── SpriteAnimator.java      ← Loads + caches sprite frames, plays animations
  │   ├── HealthBar.java           ← Custom painted HP bar component
  │   ├── CharacterSelectScreen.java
  │   ├── AreaSelectScreen.java
  │   ├── MainMenuScreen.java
  │   ├── UsernameScreen.java
  │   ├── GameOverScreen.java
  │   └── LeaderboardScreen.java
  ├── io/
  │   ├── SaveManager.java         ← Read/write save_data.txt and players.txt
  │   └── MusicManager.java        ← Plays background .wav audio
  └── exceptions/
      ├── NotEnoughManaException.java
      ├── EmptyInventoryException.java
      └── InvalidSelectionException.java

  assets/
  ├── sprites/   ← Character + enemy animation frames (idle/attack/skill1-3/death)
  ├── audio/     ← Background music .wav files
  ├── background/← Battle backdrop images
  └── portraits/ ← Character portrait images

  saves/
  ├── save_data.txt   ← Active session save (wave, gold, HP, mana, inventory)
  └── players.txt     ← All-time leaderboard records


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  🏛️  THE 4 PILLARS OF OOP — WHERE THEY LIVE & WHY
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  ┌─────────────────────────────────────────────────────────────────────────────┐
  │  1️⃣  ENCAPSULATION                                                          │
  │     "Hide the data. Control access through methods."                        │
  └─────────────────────────────────────────────────────────────────────────────┘

  PRIMARY FILE:  src/core/Character.java
  ALSO IN:       src/core/Skill.java, src/core/Item.java

  HOW:
    • Every field in Character is declared PRIVATE:
        private int hp;
        private int attackPower;
        private boolean isAlive;
        ... (all 13 fields are private)

    • You can only read them through getters:
        getHp(), getAttackPower(), isAlive(), getMana() ...

    • You can only change them through controlled setters or methods:
        takeDamage(int)   → subtracts DEF, clamps at 0, sets isAlive=false
        heal(int)         → clamps at maxHp, can revive
        spendMana(int)    → throws NotEnoughManaException if not enough

    • Skill.java — ALL fields are final + private. No setters at all.
      Once a skill is created, it CANNOT be changed. Pure encapsulation.

    • Item.java — same as Skill. All final fields, getters only.

  WHY IT MATTERS:
    You can never accidentally set hp to -999 from outside the class.
    takeDamage() always enforces the minimum-1-damage rule and the
    isAlive flag. The data is always in a valid state.


  ┌─────────────────────────────────────────────────────────────────────────────┐
  │  2️⃣  INHERITANCE                                                            │
  │     "Share common behaviour. Specialise where needed."                      │
  └─────────────────────────────────────────────────────────────────────────────┘

  PRIMARY FILE:  src/core/Character.java  (the parent)
  CHILD FILES:   src/characters/*.java  AND  src/enemies/*.java  (12 subclasses)

  HOW:
    Character is the abstract base. Every playable hero and every enemy
    extends it:

        public class Arthur   extends Character { ... }
        public class Bat      extends Character { ... }
        public class Mohammad extends Character { ... }
        ... (all 12 classes inherit from Character)

    Inherited for FREE by every subclass (no copy-paste):
        takeDamage(), heal(), restoreMana(), spendMana(),
        regenManaPerTurn(), addSkill(), getSkill(), toString() ...

    Each subclass calls super() in its constructor to set name/HP/mana/ATK/DEF:
        super(name, 250, 100, 50, 25, "assets/sprites/arthur/");
              ↑name  ↑HP  ↑MP ↑ATK ↑DEF  ↑sprite folder

  WHY IT MATTERS:
    Without inheritance you'd have 12 separate classes all duplicating
    takeDamage(), heal(), etc. One bug fix in the base fixes all 12.


  ┌─────────────────────────────────────────────────────────────────────────────┐
  │  3️⃣  POLYMORPHISM                                                           │
  │     "Same method call, different behaviour depending on the object."        │
  └─────────────────────────────────────────────────────────────────────────────┘

  PRIMARY FILE:  src/core/BattleEngine.java  (calls the polymorphic methods)
  DEFINED IN:    src/core/Character.java     (abstract declarations)
  IMPLEMENTED:   Every subclass in characters/ and enemies/

  HOW:
    Character declares three abstract methods — every subclass MUST override:

        public abstract int    attack(Character target);
        public abstract int    useSkill(int skillIndex, Character target);
        public abstract void   applyPassive();

    BattleEngine holds a List<Character> for the party and enemies.
    It calls enemy.attack(target) without knowing if enemy is a Bat,
    a Grim, or a Horse — Java picks the right version at runtime:

        // In BattleEngine.enemyTurn():
        int rawDmg = enemy.attack(target);   // ← could be Bat, Grim, Horse...
                                              //   Java calls the right one

    Arthur.attack()   → 10% miss, 10% crit at 1.8×
    Tapanh.attack()   → 7% miss, 12% crit at 1.9×
    Van.attack()      → 8% miss, high crit window from luckBonus
    Bat.attack()      → simple ATK + random(8)

    Same call. Completely different results.

  ALSO:
    BattleEngine.spawnWave() returns List<Character> — the caller never
    needs to know which specific enemy type it got. It just calls
    .attack(), .getHp(), .isAlive() on whatever is in the list.

  WHY IT MATTERS:
    You can add a brand new enemy class (e.g. Dragon.java) and the
    entire BattleEngine works with it immediately — zero changes needed.


  ┌─────────────────────────────────────────────────────────────────────────────┐
  │  4️⃣  ABSTRACTION                                                            │
  │     "Show only what's necessary. Hide the complexity."                      │
  └─────────────────────────────────────────────────────────────────────────────┘

  PRIMARY FILE:  src/core/Character.java  (abstract class)
  ALSO IN:       src/core/BattleEngine.java, src/core/Skill.java

  HOW:
    Character is declared abstract — you can NEVER do:
        new Character("Bob", 100, 50, 20, 10, "folder/");  // ← compile error

    You must use a concrete subclass. This forces every fighter to have
    a real implementation of attack(), useSkill(), and applyPassive().

    BattleEngine abstracts ALL combat logic away from the UI:
        BattleEngine.playerAttack(actor, target)  → returns AttackResult
        BattleEngine.enemyTurn(enemy, party, wave) → returns log String
        BattleEngine.spawnWave(area, wave)         → returns List<Character>

    BattleScreen just calls these and displays the result. It has NO
    idea how damage is calculated — that's all hidden in BattleEngine.

    Skill.java abstracts skill data — BattleScreen reads skill.getName()
    and skill.getManaCost() to build buttons. It never touches raw numbers.

  WHY IT MATTERS:
    If you want to change how crits work, you only touch BattleEngine.
    BattleScreen doesn't break. The UI and the logic are fully separated.


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  ❓  Q&A DEFENCE GUIDE  — "What handles X?"
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  Q1: What handles DAMAGE CALCULATION?
  ─────────────────────────────────────
  A: Two places work together:

     BattleEngine.playerAttack()  →  rolls miss/hit/crit, calls target.takeDamage()
     Character.takeDamage(int)    →  applies DEF reduction:
                                     effective = Math.max(1, rawDamage - defensePower)
                                     hp = Math.max(0, hp - effective)

     The log always shows the ACTUAL hp lost (hpBefore - target.getHp()),
     so the number in the battle log always matches the health bar drop.


  Q2: What handles the TURN ORDER?
  ──────────────────────────────────
  A: BattleScreen manages the turn loop:

     1. playerTurn = true  → player picks an action
     2. doAttack() or doSkill() runs → calls endPlayerTurn()
     3. endPlayerTurn() → 600ms Timer delay → doEnemyTurns()
     4. doEnemyTurns() → loops all living enemies, calls BattleEngine.enemyTurn()
     5. BattleEngine.endOfTurn(party) → +15 MP regen, clears taunt
     6. advanceActiveCharacter() → cycles to next living party member
     7. playerTurn = true again → repeat

     advanceActiveCharacter() uses modulo to cycle:
         activeCharIndex = (activeCharIndex + 1) % party.size()
     It skips dead characters automatically.


  Q3: What handles ENEMY AI?
  ───────────────────────────
  A: BattleEngine.enemyTurn(enemy, party, wave)

     Rolls RNG.nextInt(10):
       0–5  (60%) → basic attack on a RANDOM living party member
       6–7  (20%) → self-buff: ATK × (1.0 + 0.10 × wave)
       8–9  (20%) → taunt attempt (60% land chance)

     Wave scaling multiplier is applied to all damage:
       wave 1 = 1.0×,  wave 2 = 1.3×,  wave 3 = 1.6×,  wave 4 = 2.2×

     Boss (wave 4) also has a 25% crit chance that doubles damage,
     logged as "💥 BOSS CRIT!"


  Q4: What handles MANA and SKILL UNLOCKING?
  ────────────────────────────────────────────
  A: Characters start at 25% of their max mana (minimum 15 MP).
     This is set in Character.initStartingMana(), called at the end
     of every character constructor.

     Each turn end, BattleEngine.endOfTurn() calls regenManaPerTurn()
     on every living character → +15 MP per turn.

     Skills are locked in the UI (greyed out) when:
         actor.getMana() < skill.getManaCost()

     This means at turn 1 only the cheapest skill is available.
     By turn 3-4 the mid-tier skill unlocks. The ultimate needs ~5 turns.


  Q5: What handles GOLD and LOOTING?
  ─────────────────────────────────────
  A: BattleEngine.rollGoldDrop(enemy, wave) calculates the drop:

         base = 5 + (enemy.getAttackPower() / 4) + (enemy.getMaxHp() / 20)
         base += (wave - 1) * 5          ← wave bonus
         variance = ±40% of base         ← random range
         cap = wave 4 boss: 200g,  others: 80g

     BattleScreen calls this in doAttack() and doSkill() when
     target.isAlive() becomes false after the hit.
     It then calls frame.addGold(gold) and logs:
         "💰 Looted 23 gold from Horse!"

     Gold is stored in MainFrame.gold (int field).
     Spent in ShopScreen via frame.spendGold(cost).
     Saved to saves/save_data.txt as "gold=N" between waves.


  Q6: What handles SAVING and LOADING?
  ──────────────────────────────────────
  A: io/SaveManager.java — all file I/O lives here.

     saveGame()   → writes key=value lines to saves/save_data.txt
                    (username, area, wave, gold, party HP/mana, inventory)

     loadGame()   → reads save_data.txt into a Map<String,String>

     rebuildParty()     → reconstructs Character objects from the map
     rebuildInventory() → reconstructs Item objects from the map

     BattleScreen triggers a save after each wave clear:
         frame.saveCurrentGame()  →  calls SaveManager.saveGame()

     MainFrame.loadSavedGame() is called when the player hits "Continue"
     on the main menu.


  Q7: What handles SPRITE ANIMATIONS?
  ──────────────────────────────────────
  A: ui/SpriteAnimator.java

     On construction it calls play("idle") which loads all PNGs from
     assets/sprites/charactername/idle/ into a List<ImageIcon>.

     A javax.swing.Timer ticks every frameDelayMs milliseconds and
     calls nextFrame() to cycle through the list.

     play(animName)           → switches to a looping animation
     playOnce(animName, cb)   → plays once, then returns to idle,
                                then calls the callback (onDamage runs here)

     All loaded frames are stored in a static CACHE (HashMap) so the
     same PNG files are never read from disk twice.

     After playOnce finishes, the animation's cache entry is cleared
     to free memory. Between waves, clearAllCache() wipes everything.


  Q8: What handles the SHOP?
  ───────────────────────────
  A: ui/ShopScreen.java

     Built with a fixed list of 5 items (SHOP_ITEMS).
     Each item row has a "Buy Xg" button.

     On click:
       1. Checks frame.getGold() >= item.getShopCost()
       2. If yes  → frame.spendGold(cost), adds item copy to frame.getInventory()
       3. If no   → JOptionPane warning "Not enough gold!"
       4. Updates the gold label on screen

     The "Continue to Wave N" button calls frame.goToBattle() which
     creates a new BattleScreen for the next wave.


  Q9: What handles CUSTOM EXCEPTIONS?
  ──────────────────────────────────────
  A: src/exceptions/ — three custom exception classes:

     NotEnoughManaException    → thrown by Character.spendMana(cost)
                                  when mana < cost.
                                  Caught in every useSkill() method.

     EmptyInventoryException   → thrown by BattleEngine.useItem()
                                  when inventory is empty or index invalid.
                                  Caught in BattleScreen.doItem().

     InvalidSelectionException → available for invalid menu/target picks.

     These demonstrate proper exception handling — the game never crashes
     from a bad mana spend or empty bag, it shows a message instead.


  Q10: What handles SCREEN NAVIGATION?
  ──────────────────────────────────────
  A: ui/MainFrame.java using Java's CardLayout.

     MainFrame holds a single JPanel (root) with a CardLayout.
     Every screen is added to root with a unique string key:
         root.add(new BattleScreen(this), "battle_Forest_1")

     Navigation methods swap which card is visible:
         goToMainMenu()      → layout.show(root, "mainmenu")
         goToBattle()        → layout.show(root, "battle_"+area+"_"+wave)
         goToShop()          → layout.show(root, "shop_"+wave)
         goToGameOver(bool)  → layout.show(root, "gameover")

     MainFrame also holds ALL shared game state (gold, party, wave, area)
     so every screen can read/write the same data through frame.getX().


  Q11: What handles WAVE SCALING / DIFFICULTY?
  ──────────────────────────────────────────────
  A: BattleEngine.spawnWave(area, wave)

     Waves 1–3: spawns wave+1 enemies (wave1=2, wave2=3, wave3=4)
     Each enemy's stats are multiplied by a scale factor:
         wave 1 = 1.0×  (base stats)
         wave 2 = 1.4×  (HP, ATK, DEF all ×1.4)
         wave 3 = 1.9×  (HP, ATK, DEF all ×1.9)

     Wave 4 (Boss): ONE enemy with:
         HP  × 5,   ATK × 4,   DEF × 2.5
         + 25% crit chance in enemyTurn() for 2× damage hits

     Enemy damage in enemyTurn() also scales separately via waveScale:
         wave 1 = 1.0×,  wave 2 = 1.3×,  wave 3 = 1.6×,  wave 4 = 2.2×


  Q12: What handles HEALTH BARS?
  ────────────────────────────────
  A: ui/HealthBar.java — a custom JComponent.

     It holds a reference to a Character object.
     paintComponent() reads character.getHp() / character.getMaxHp()
     and draws a filled rectangle proportional to that ratio.

     Color changes automatically:
         > 50% HP  → green
         > 25% HP  → yellow/orange
         ≤ 25% HP  → red

     BattleScreen.updateAllBars() calls hb.update(character) on every
     bar after any action, then calls repaint() to redraw them.


  Q13: What handles PASSIVE ABILITIES?
  ──────────────────────────────────────
  A: Each character class overrides applyPassive() from Character.
     It's called at the END of the constructor, after skills are added.

     Arthur   → setDefensePower(DEF * 1.2)          Iron Skin: +20% DEF
     Star     → setDefensePower(DEF * 1.25),         Dragon Hide: +25% DEF, +15% HP
                setMaxHp(HP * 1.15)
     Mohammad → setAttackPower(ATK * 1.2),           Volatility: +20% ATK, -15% DEF
                setDefensePower(DEF * 0.85)
     Tapanh   → setSpeed(SPD * 1.2)                 Reflex: +20% SPD
     Van      → setMaxMana(MP * 1.25)               Old Money: +25% mana pool
     Fabby    → no stat change on init               Waterworks: checked at turn start


  Q14: What handles MUSIC?
  ─────────────────────────
  A: io/MusicManager.java

     MusicManager.play(filename, loop) loads a .wav from assets/audio/
     using javax.sound.sampled and plays it (looped or once).
     MusicManager.stop() stops whatever is currently playing.

     Called from MainFrame:
         goToMainMenu()  → MusicManager.play("menu_theme.wav", true)
         goToBattle()    → MusicManager.stop()  (battle has its own music)


  Q15: What handles the LEADERBOARD?
  ─────────────────────────────────────
  A: io/SaveManager.java + ui/LeaderboardScreen.java

     SaveManager.registerOrUpdatePlayer() writes/updates a line in
     players.txt in the format:
         username,highestWave,totalKills,totalTurns,lastArea

     If the username already exists, it keeps the HIGHER wave count.

     SaveManager.loadLeaderboard() reads all lines, sorts by wave
     descending, and returns List<String[]>.

     LeaderboardScreen reads this list and renders it as a table.
     Triggered from the main menu "Leaderboard" button.


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  🔗  CONNECTION MAP — "If I tap/click X, what happens?"
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  🖱️  Player clicks "Basic Attack" button
      └─ BattleScreen.doAttack()
          └─ BattleEngine.playerAttack(actor, target)
              └─ rolls RNG → miss / hit / crit
              └─ target.takeDamage(raw)  →  hp drops (DEF subtracted)
          └─ log(result.message)         →  battle log shows ACTUAL damage
          └─ updateAllBars()             →  health bars repaint
          └─ if enemy dead → rollGoldDrop() → frame.addGold() → log gold
          └─ endPlayerTurn()             →  600ms delay → enemy acts


  🖱️  Player clicks a Skill button (e.g. "Slash")
      └─ BattleScreen.doSkill(skillIndex)
          └─ checks actor.getMana() >= skill.getManaCost()
          └─ approachAndAttack() → slides sprite toward enemy
          └─ SpriteAnimator.playOnce("skill1", onDamage)
              └─ animation plays → onDamage.run() fires
          └─ BattleEngine.playerSkill(actor, index, target)
              └─ actor.useSkill() → spendMana() → target.takeDamage()
          └─ log actual damage → updateAllBars()
          └─ if enemy dead → gold loot → log
          └─ endPlayerTurn()


  🖱️  Player clicks an enemy sprite / card
      └─ selectedTarget = that enemy's index
      └─ highlightEnemyCard(idx)   →  gold border on selected card
      └─ updateDetailPanel(enemy)  →  right sidebar shows enemy stats
      └─ log("Targeting: EnemyName")


  🖱️  Player clicks a party card (bottom bar)
      └─ activeCharIndex = that character's index
      └─ highlightActiveChar()     →  gold border on active card
      └─ rebuildActionPanel()      →  left skill panel rebuilds for new character
      └─ log("Switched to CharName's turn.")


  🖱️  Player clicks "Shop" button (top bar)
      └─ frame.goToShop()
          └─ new ShopScreen(frame) added to CardLayout
          └─ layout.show(root, "shop_N")


  🖱️  Player clicks "Buy" in the shop
      └─ checks frame.getGold() >= item.getShopCost()
      └─ frame.spendGold(cost)     →  gold field decreases
      └─ frame.getInventory().add(new Item(...))
      └─ goldLabel updates on screen
      └─ JOptionPane: "ItemName added to your bag!"


  🖱️  Player clicks "Continue to Wave N" in shop
      └─ frame.goToBattle()
          └─ MusicManager.stop()
          └─ new BattleScreen(frame) created
              └─ BattleEngine.spawnWave(area, wave) → new scaled enemies
          └─ layout.show(root, "battle_Area_N")


  ⚔️  Enemy turn fires (after player acts)
      └─ BattleScreen.doEnemyTurns()
          └─ for each living enemy:
              └─ BattleEngine.enemyTurn(enemy, party, wave)
                  └─ picks RANDOM living party member as target
                  └─ applies waveScale multiplier to damage
                  └─ boss wave: 25% chance for 2× crit
              └─ log(result)
          └─ updateAllBars()
          └─ if party all dead → frame.goToGameOver(false)
          └─ BattleEngine.endOfTurn(party) → +15 MP, clear taunt
          └─ advanceActiveCharacter()
          └─ playerTurn = true → rebuildActionPanel()


  🏆  All enemies in wave 4 defeated
      └─ BattleScreen.checkWaveOver()
          └─ wave >= 4 → log "All waves defeated! You win!"
          └─ SaveManager.registerOrUpdatePlayer() → updates leaderboard
          └─ SaveManager.clearSaveData()           → wipes active save
          └─ Timer 1500ms → frame.goToGameOver(true)


  ✅  Waves 1–3 cleared
      └─ BattleScreen.checkWaveOver()
          └─ all animators stopped + cleared
          └─ SpriteAnimator.clearAllCache()  → frees all sprite memory
          └─ frame.saveCurrentGame()         → writes save_data.txt
          └─ Timer 1200ms → frame.nextWave() → frame.goToShop()


  💀  A character dies (HP reaches 0)
      └─ Character.takeDamage() sets isAlive = false
      └─ BattleEngine.isPartyDefeated() checks all party members
      └─ if all dead → frame.goToGameOver(false)
      └─ advanceActiveCharacter() skips dead members in turn rotation
      └─ party card name label turns grey in buildPartyCard()


━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  📋  KEY FUNCTIONS CHEAT SHEET
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  Character.java
  ──────────────
  takeDamage(int raw)          Subtracts DEF, clamps at 0, sets isAlive=false if 0
  heal(int amount)             Adds HP up to maxHp, can revive dead characters
  spendMana(int cost)          Deducts mana, throws NotEnoughManaException if short
  restoreMana(int amount)      Adds mana up to maxMana
  regenManaPerTurn()           +15 MP, called every turn end
  initStartingMana()           Sets mana to 25% of max at game start
  addSkill(Skill)              Adds a skill to the character's skill list
  getSkill(int index)          Returns the Skill at that index

  BattleEngine.java
  ─────────────────
  playerAttack(attacker, target)          Miss/hit/crit roll, returns AttackResult
  playerSkill(attacker, index, target)    Single-target skill, returns AttackResult
  playerSkillAoe(attacker, index, enemies) AoE skill, returns List<AttackResult>
  enemyTurn(enemy, party, wave)           Enemy AI decision + scaled damage, returns log String
  endOfTurn(party)                        +15 MP regen + clear taunt for all living
  spawnWave(area, wave)                   Returns scaled List<Character> for that wave
  rollGoldDrop(enemy, wave)               Returns random gold amount (capped 80g/200g)
  isPartyDefeated(party)                  true if all party members are dead
  isEnemyWaveDefeated(enemies)            true if all enemies are dead
  attemptFlee(party)                      40% escape, on fail party takes 15% max HP
  useItem(inventory, index, target)       Removes item, applies effect, returns log String

  SpriteAnimator.java
  ───────────────────
  play(animName)                          Switch to looping animation (e.g. "idle")
  playOnce(animName, onComplete)          Play once → idle → run callback, clears cache
  stop()                                  Stops the internal Timer (used on death/wave end)
  clearCacheFor(folder, anim, flip)       Removes one animation from the static cache
  clearAllCache()                         Wipes entire frame cache (called between waves)

  SaveManager.java
  ────────────────
  saveGame(...)                           Writes full game state to saves/save_data.txt
  loadGame()                              Reads save_data.txt → Map<String,String>
  rebuildParty(data)                      Reconstructs List<Character> from save map
  rebuildInventory(data)                  Reconstructs List<Item> from save map
  registerOrUpdatePlayer(...)             Writes/updates leaderboard entry in players.txt
  loadLeaderboard()                       Returns sorted List<String[]> from players.txt
  hasSaveData()                           true if a valid save file exists
  clearSaveData()                         Deletes save_data.txt (called on game over/win)

  MainFrame.java
  ──────────────
  addGold(int)                            Increases gold counter
  spendGold(int)                          Decreases gold (min 0)
  nextWave()                              currentWave++
  goToBattle()                            Creates new BattleScreen, shows it
  goToShop()                              Creates new ShopScreen, shows it
  goToGameOver(boolean victory)           Creates GameOverScreen, shows it
  saveCurrentGame()                       Delegates to SaveManager.saveGame()
  loadSavedGame()                         Delegates to SaveManager.loadGame() + rebuild

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  🎮  CHARACTER STATS QUICK REFERENCE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  Name       HP    MP    ATK   DEF   Role          Passive
  ─────────────────────────────────────────────────────────────────────────────
  Arthur     250   100   50    30    Warrior        Iron Skin (+20% DEF)
  Star       345   90    45    25    Half-Dragon    Dragon Hide (+25% DEF, +15% HP)
  Mohammad   210   100   78    25    Demolitions    Volatility (+20% ATK, -15% DEF)
  Tapanh     190   110   40    45    Fighter        Reflex (+20% SPD, 30% counter)
  Van        200   175   35    28    Noble          Old Money (+25% mana pool)
  Fabby      140   130   15    20    Support        Waterworks (+10 MP/turn < 40% HP)

  (HP/ATK/DEF shown after passives are applied)

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  🛒  SHOP ITEMS REFERENCE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  Item              Effect                    Cost
  ──────────────────────────────────────────────────
  Health Potion     Restore 80 HP             50g
  Mana Elixir       Restore 60 MP             40g
  Revive Scroll     Revive fallen ally 50 HP  150g
  Elixir of Power   ATK +15 permanently       100g
  Iron Tonic        DEF +10 permanently       80g

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  ⚠️  COMMON DEFENCE QUESTIONS — QUICK ANSWERS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  "Why is Character abstract?"
  → Because a generic "Character" with no class makes no sense in the game.
    Every fighter must be a specific type (Arthur, Bat, etc.) with its own
    attack() and useSkill() behaviour. abstract enforces this at compile time.

  "Why use a List<Character> instead of separate variables?"
  → Flexibility. BattleEngine can loop over any number of party members or
    enemies without knowing how many there are. Adding a 3rd party member
    requires zero changes to BattleEngine.

  "Why does takeDamage() subtract DEF instead of the caller doing it?"
  → Encapsulation. The damage formula lives in ONE place. If you change
    how DEF works, you change one method and every attacker in the game
    automatically uses the new formula.

  "Why does spendMana() throw an exception instead of returning false?"
  → Exceptions are for exceptional situations — running out of mana mid-skill
    is unexpected and should be handled explicitly. It forces every caller to
    acknowledge the failure case rather than silently ignoring a return value.

  "Why is the CACHE in SpriteAnimator static?"
  → So ALL SpriteAnimator instances share one cache. If Arthur's idle frames
    are loaded once, every animator showing Arthur reuses the same ImageIcon
    objects. Without static, each animator would reload from disk separately.

  "Why does BattleScreen not calculate damage itself?"
  → Separation of concerns. BattleScreen is a VIEW — it shows things.
    BattleEngine is the MODEL — it calculates things. Keeping them separate
    means you could swap the UI entirely without touching combat logic.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  📝  NOTES
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  • This file is plain text. It does NOT affect compilation or gameplay.
  • The bin/ folder contains compiled .class files — never edit those directly.
  • saves/ folder is excluded from git (.gitignore) — it's runtime data only.
  • To rebuild from source:  javac -cp src -d bin <all .java files>
  • To run:                  java -cp bin Main

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
