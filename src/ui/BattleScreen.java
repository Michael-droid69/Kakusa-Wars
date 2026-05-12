package ui;

import core.BattleEngine;
import core.Character;
import core.Item;
import core.Skill;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.imageio.ImageIO;

public class BattleScreen extends JPanel {

    // ── References ──
    private final MainFrame           frame;
    private final List<Character>     party;
    private final List<Item>          inventory;
    private       List<Character>     enemies;

    // ── State ──
    private int     activeCharIndex = 0;   // which party member is acting
    private boolean playerTurn      = true;
    private int     selectedTarget  = 0;   // which enemy is targeted
    private int     enemiesKilledThisWave = 0;

    // ── UI Regions ──
    private JPanel     enemyZone;     // top: enemy sprites + health bars
    private JPanel     partyZone;     // bottom-left: party sprites + bars
    private JTextArea  battleLog;     // bottom-right: scrolling log
    private JPanel     actionPanel;   // buttons: Attack / Skill1 / Skill2 / Ulti / Item / Flee
    private JLabel     waveLabel;

    // ── Sprite animators — one per character, one per enemy ──
    private final Map<String, SpriteAnimator> animators = new LinkedHashMap<>();

    // ── Health bars ──
    private final Map<String, HealthBar> healthBars = new LinkedHashMap<>();

    public BattleScreen(MainFrame frame) {
        this.frame     = frame;
        this.party     = frame.getParty();
        this.inventory = frame.getInventory();
        this.enemies   = BattleEngine.spawnWave(
                            frame.getCurrentArea(), frame.getCurrentWave());

        // Preload background once per area (best effort; missing assets are fine)
        preloadBackgroundForArea(frame.getCurrentArea());

        setLayout(new BorderLayout(8, 8));
        setBackground(new Color(10, 10, 16));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        buildWaveLabel();
        buildEnemyZone();
        buildCenterRegion();
        buildActionPanel();

        log("Wave " + frame.getCurrentWave() + " begins in " +
            frame.getCurrentArea() + "!");
        log("▶  " + party.get(activeCharIndex).getName() + "'s turn.");
    }

    // ─────────────────────────────────────────────────
    // BUILD — WAVE LABEL (top bar)
    // ─────────────────────────────────────────────────
    private void buildWaveLabel() {
        waveLabel = new JLabel(
            "  " + frame.getCurrentArea() + "  —  Wave " + frame.getCurrentWave() + " / 4",
            SwingConstants.LEFT);
        waveLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        waveLabel.setForeground(new Color(200, 170, 80));
        waveLabel.setBackground(new Color(18, 18, 26));
        waveLabel.setOpaque(true);
        waveLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        add(waveLabel, BorderLayout.NORTH);
    }

    // ─────────────────────────────────────────────────
    // BUILD — ENEMY ZONE (shows enemy sprites + HP bars)
    // ─────────────────────────────────────────────────
    private void buildEnemyZone() {
        enemyZone = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        enemyZone.setBackground(new Color(14, 10, 22));
        enemyZone.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        enemyZone.setPreferredSize(new Dimension(0, 220));

        for (int i = 0; i < enemies.size(); i++) {
            Character e = enemies.get(i);
            JPanel slot = new JPanel();
            slot.setLayout(new BoxLayout(slot, BoxLayout.Y_AXIS));
            slot.setBackground(new Color(14, 10, 22));

            SpriteAnimator anim = new SpriteAnimator(e.getSpriteFolder(), 120);
            anim.setAlignmentX(CENTER_ALIGNMENT);
            animators.put("enemy_" + i, anim);

            JLabel nameLabel = new JLabel(e.getName(), SwingConstants.CENTER);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            nameLabel.setForeground(new Color(220, 100, 100));
            nameLabel.setAlignmentX(CENTER_ALIGNMENT);

            HealthBar hb = new HealthBar(e, 160, 18);
            hb.setAlignmentX(CENTER_ALIGNMENT);
            healthBars.put("enemy_" + i, hb);

            slot.add(anim);
            slot.add(Box.createVerticalStrut(4));
            slot.add(nameLabel);
            slot.add(Box.createVerticalStrut(3));
            slot.add(hb);

            // Click enemy to select as target
            final int idx = i;
            slot.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent ev) {
                    if (enemies.get(idx).isAlive()) selectedTarget = idx;
                    log("Targeting: " + enemies.get(idx).getName());
                    highlightTarget(idx);
                }
            });
            enemyZone.add(slot);
        }
        add(enemyZone, BorderLayout.CENTER);
    }

    // ─────────────────────────────────────────────────
    // BUILD — CENTER: party zone (left) + battle log (right)
    // ─────────────────────────────────────────────────
    private void buildCenterRegion() {
        JPanel center = new JPanel(new BorderLayout(8, 0));
        center.setBackground(new Color(10, 10, 16));

        // Party zone — left side
        partyZone = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 8));
        partyZone.setBackground(new Color(12, 18, 28));
        partyZone.setPreferredSize(new Dimension(420, 200));

        for (int i = 0; i < party.size(); i++) {
            Character c = party.get(i);
            JPanel slot = new JPanel();
            slot.setLayout(new BoxLayout(slot, BoxLayout.Y_AXIS));
            slot.setBackground(new Color(12, 18, 28));

            SpriteAnimator anim = new SpriteAnimator(c.getSpriteFolder(), 110);
            anim.setAlignmentX(CENTER_ALIGNMENT);
            animators.put("party_" + i, anim);

            JLabel nameLabel = new JLabel(c.getName(), SwingConstants.CENTER);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            nameLabel.setForeground(new Color(100, 200, 240));
            nameLabel.setAlignmentX(CENTER_ALIGNMENT);

            HealthBar hb = new HealthBar(c, 160, 18);
            hb.setAlignmentX(CENTER_ALIGNMENT);
            healthBars.put("party_" + i, hb);

            // Mana bar — simpler label for now
            JLabel manaLabel = new JLabel(
                "MP: " + c.getMana() + "/" + c.getMaxMana(), SwingConstants.CENTER);
            manaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            manaLabel.setForeground(new Color(80, 130, 220));
            manaLabel.setAlignmentX(CENTER_ALIGNMENT);
            manaLabel.setName("mana_" + i); // used to find and update later

            slot.add(anim);
            slot.add(Box.createVerticalStrut(4));
            slot.add(nameLabel);
            slot.add(hb);
            slot.add(manaLabel);
            partyZone.add(slot);
        }
        center.add(partyZone, BorderLayout.WEST);

        // Battle log — right side
        battleLog = new JTextArea();
        battleLog.setEditable(false);
        battleLog.setBackground(new Color(16, 16, 24));
        battleLog.setForeground(new Color(180, 180, 210));
        battleLog.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        battleLog.setLineWrap(true);
        battleLog.setWrapStyleWord(true);
        battleLog.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(battleLog);
        scroll.setPreferredSize(new Dimension(320, 200));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(40, 40, 60)));
        center.add(scroll, BorderLayout.CENTER);

        add(center, BorderLayout.SOUTH);
    }

    // ─────────────────────────────────────────────────
    // BUILD — ACTION PANEL (the buttons)
    // ─────────────────────────────────────────────────
    private void buildActionPanel() {
        actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        actionPanel.setBackground(new Color(14, 14, 22));
        actionPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
                              new Color(50, 50, 80)));

        Character actor = party.get(activeCharIndex);

        // Attack
        JButton attackBtn = makeActionBtn("⚔ Attack", new Color(100, 30, 30));
        attackBtn.addActionListener(e -> doAttack());
        actionPanel.add(attackBtn);

        // Skill buttons — one per skill, greyed if not enough mana
        for (int i = 0; i < actor.getSkillCount(); i++) {
            Skill skill = actor.getSkill(i);
            boolean canUse = actor.getMana() >= skill.getManaCost();
            JButton btn = makeActionBtn(
                skill.getName() + " (" + skill.getManaCost() + "MP)",
                canUse ? new Color(50, 25, 90) : new Color(30, 30, 40));
            btn.setEnabled(canUse);
            final int idx = i;
            btn.addActionListener(e -> doSkill(idx));
            actionPanel.add(btn);
        }

        // Item
        JButton itemBtn = makeActionBtn("🎒 Item (" + inventory.size() + ")",
                                         new Color(25, 55, 30));
        itemBtn.addActionListener(e -> doItem());
        actionPanel.add(itemBtn);

        // Flee
        JButton fleeBtn = makeActionBtn("💨 Flee (40%)", new Color(40, 40, 20));
        fleeBtn.addActionListener(e -> doFlee());
        actionPanel.add(fleeBtn);

        add(actionPanel, BorderLayout.EAST);
    }

    // ─────────────────────────────────────────────────
    // ACTIONS
    // ─────────────────────────────────────────────────
    private void doAttack() {
        if (!playerTurn) return;
        Character actor  = party.get(activeCharIndex);
        Character target = enemies.get(selectedTarget);
        if (!target.isAlive()) { log("That enemy is already dead. Pick another target."); return; }

        // Play attack animation, THEN deal damage
        SpriteAnimator anim = animators.get("party_" + activeCharIndex);
        if (anim != null) anim.playOnce("attack", () -> {
            BattleEngine.AttackResult result = BattleEngine.playerAttack(actor, target);
            log(result.message);
            updateAllBars();
            if (!target.isAlive()) {
                log(target.getName() + " was defeated!");
                frame.addEnemyKill();
                enemiesKilledThisWave++;
                animators.get("enemy_" + selectedTarget).playOnce("death", () -> {});
            }
            frame.addTurn();
            checkWaveOver();
            if (BattleEngine.isEnemyWaveDefeated(enemies)) return;
            actor.setTaunted(false);
            endPlayerTurn();
        });
        else { // no animator available — apply immediately
            BattleEngine.AttackResult result = BattleEngine.playerAttack(actor, target);
            log(result.message);
            updateAllBars();
            checkWaveOver();
            if (!BattleEngine.isEnemyWaveDefeated(enemies)) endPlayerTurn();
        }
    }

    private void doSkill(int skillIndex) {
        if (!playerTurn) return;
        Character actor  = party.get(activeCharIndex);
        Character target = enemies.get(selectedTarget);
        Skill     skill  = actor.getSkill(skillIndex);

        if (actor.getMana() < skill.getManaCost()) {
            log("Not enough mana for " + skill.getName() + "!");
            return;
        }

        SpriteAnimator anim = animators.get("party_" + activeCharIndex);
        String animName = switch (skillIndex) {
            case 0 -> "skill1";
            case 1 -> "skill2";
            case 2 -> "skill3";
            default -> "attack";
        };

        Runnable afterAnim = () -> {
            if (skill.getType().equals("damage_all")) {
                List<BattleEngine.AttackResult> results =
                    BattleEngine.playerSkillAoe(actor, skillIndex, enemies);
                results.forEach(r -> log(r.message));
            } else {
                BattleEngine.AttackResult result =
                    BattleEngine.playerSkill(actor, skillIndex, target);
                log(result.message);
            }
            updateAllBars();
            checkWaveOver();
            if (!BattleEngine.isEnemyWaveDefeated(enemies)) endPlayerTurn();
        };

        if (anim != null) anim.playOnce(animName, afterAnim);
        else afterAnim.run();
    }

    private void doItem() {
        if (!playerTurn) return;
        if (inventory.isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                "Your bag is empty!", "No Items",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Build pick list
        String[] options = inventory.stream()
            .map(Item::toString).toArray(String[]::new);

        String picked = (String) JOptionPane.showInputDialog(frame,
            "Choose an item:", "Use Item",
            JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (picked == null) return; // cancelled

        int itemIndex = 0;
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(picked)) { itemIndex = i; break; }
        }

        // Choose target character
        String[] partyNames = party.stream()
            .map(Character::getName).toArray(String[]::new);
        String targetName = (String) JOptionPane.showInputDialog(frame,
            "Use on who?", "Select Target",
            JOptionPane.PLAIN_MESSAGE, null, partyNames, partyNames[0]);
        if (targetName == null) return;

        Character target = party.stream()
            .filter(c -> c.getName().equals(targetName))
            .findFirst().orElse(party.get(0));

        try {
            String msg = BattleEngine.useItem(inventory, itemIndex, target);
            log(msg);
            updateAllBars();
            endPlayerTurn();
        } catch (exceptions.EmptyInventoryException ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(),
                "Empty Bag", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void doFlee() {
        if (!playerTurn) return;
        boolean escaped = BattleEngine.attemptFlee(party);
        if (escaped) {
            log("Your party fled the battle!");
            frame.goToAreaSelect();
        } else {
            log("Flee failed! Your party took damage from the enemies!");
            updateAllBars();
            if (BattleEngine.isPartyDefeated(party)) {
                frame.goToGameOver(false);
            } else {
                endPlayerTurn();
            }
        }
    }

    // ─────────────────────────────────────────────────
    // TURN MANAGEMENT
    // ─────────────────────────────────────────────────
    private void endPlayerTurn() {
        playerTurn = false;
        // Delay before enemy acts — feels more like a real game
        Timer delay = new Timer(600, e -> doEnemyTurns());
        delay.setRepeats(false);
        delay.start();
    }

    private void doEnemyTurns() {
        for (Character enemy : enemies) {
            if (!enemy.isAlive()) continue;
            String result = BattleEngine.enemyTurn(enemy, party);

            // Handle taunt prefix
            if (result.startsWith("TAUNT:")) {
                // Format: TAUNT:targetName:logMessage
                String[] parts = result.split(":", 3);
                String msg = parts.length == 3 ? parts[2] : result;
                log(msg);
            } else {
                log(result);
            }
        }

        updateAllBars();

        if (BattleEngine.isPartyDefeated(party)) {
            frame.goToGameOver(false);
            return;
        }

        // End of full round — mana regen
        BattleEngine.endOfTurn(party);
        updateAllBars();

        // Next player character's turn
        advanceActiveCharacter();
        playerTurn = true;
        rebuildActionPanel();
        log("▶  " + party.get(activeCharIndex).getName() + "'s turn.");
    }

    private void advanceActiveCharacter() {
        // Skip dead characters
        int attempts = 0;
        do {
            activeCharIndex = (activeCharIndex + 1) % party.size();
            attempts++;
        } while (!party.get(activeCharIndex).isAlive() && attempts < party.size());
    }

    // ─────────────────────────────────────────────────
    // WAVE END CHECK
    // ─────────────────────────────────────────────────
    private void checkWaveOver() {
        if (!BattleEngine.isEnemyWaveDefeated(enemies)) return;

        int wave = frame.getCurrentWave();
        log("✓ Wave " + wave + " cleared!");

        if (wave >= 4) {
            // All 4 waves done — victory
            log("🏆 All waves defeated! You win!");
            Timer t = new Timer(1500, e -> frame.goToGameOver(true));
            t.setRepeats(false); t.start();
        } else {
            // Save game, go to shop, then next wave
            frame.saveCurrentGame();
            log("Heading to the shop...");
            Timer t = new Timer(1200, e -> {
                frame.nextWave();
                frame.goToShop();
            });
            t.setRepeats(false); t.start();
        }
    }

    // ─────────────────────────────────────────────────
    // BACKGROUND RENDERING (battlefield image)
    // ─────────────────────────────────────────────────
    private static final Map<String, BufferedImage> BACKGROUND_CACHE = new HashMap<>();

    private String backgroundKey() {
        String area = frame.getCurrentArea();
        return switch (area) {
            case "Forest"  -> "assets/background/forest.png";
            case "Dungeon" -> "assets/background/haunted.png";
            case "Volcano" -> "assets/background/ritual.png";
            default        -> "assets/background/forest.png";
        };
    }

    private void preloadBackgroundForArea(String area) {
        String key = switch (area) {
            case "Forest"  -> "assets/background/forest.png";
            case "Dungeon" -> "assets/background/haunted.png";
            case "Volcano" -> "assets/background/ritual.png";
            default        -> "assets/background/forest.png";
        };

        if (BACKGROUND_CACHE.containsKey(key)) return;

        File f = new File(key);
        if (!f.exists()) return;

        try {
            BufferedImage img = ImageIO.read(f);
            if (img != null) BACKGROUND_CACHE.put(key, img);
        } catch (IOException ignored) {
            // ignore: background is optional
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        BufferedImage bg = BACKGROUND_CACHE.get(backgroundKey());
        if (bg == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(bg, 0, 0, getWidth(), getHeight(), null);
        } finally {
            g2.dispose();
        }
    }

    // ─────────────────────────────────────────────────
    // UI HELPERS
    // ─────────────────────────────────────────────────
    private void log(String msg) {
        battleLog.append(msg + "\n");
        battleLog.setCaretPosition(battleLog.getDocument().getLength());
    }

    private void updateAllBars() {
        for (int i = 0; i < party.size(); i++) {
            HealthBar hb = healthBars.get("party_" + i);
            if (hb != null) hb.update(party.get(i));
        }
        for (int i = 0; i < enemies.size(); i++) {
            HealthBar hb = healthBars.get("enemy_" + i);
            if (hb != null) hb.update(enemies.get(i));
        }
        repaint();
    }

    private void rebuildActionPanel() {
        remove(actionPanel);
        buildActionPanel();
        revalidate();
        repaint();
    }

    private void highlightTarget(int idx) {
        // Visual feedback — briefly tint the selected enemy slot
        // You can enhance this with a border or glow later
        repaint();
    }

    private JButton makeActionBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(170, 38));
        return btn;
    }
}
