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
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
    private final List<JPanel> enemySlots = new ArrayList<>();


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

    // ── Detail panel widgets for enemy info and active hero stats ──
    private JLabel detailName;
    private JLabel detailHp;
    private JLabel detailAtk;
    private JLabel detailDef;
    private JLabel detailStatus;
    private HealthBar detailHpBar;

    // Store references so we can highlight the active character card
    private final List<JPanel> charCards  = new ArrayList<>();
    private final List<JPanel> enemyCards = new ArrayList<>();

    public BattleScreen(MainFrame frame) {
        this.frame     = frame;
        this.party     = frame.getParty();
        this.inventory = frame.getInventory();
        this.enemies   = BattleEngine.spawnWave(
                            frame.getCurrentArea(), frame.getCurrentWave());

        preloadBackgroundForArea(frame.getCurrentArea());

        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(8, 6, 14));

        // ── Wire the 5 regions in order ──
        add(buildTopBar(),          BorderLayout.NORTH);   // wave + HUD + flee/shop/inv
        add(buildSkillPanel(),      BorderLayout.WEST);    // vertical skill buttons
        add(buildBattlefieldPanel(),BorderLayout.CENTER);  // enemy sprites + battle log
        add(buildDetailPanel(),     BorderLayout.EAST);    // enemy detail + active hero info
        add(buildBottomBar(),       BorderLayout.SOUTH);   // party cards + enemy cards

        log("Wave " + frame.getCurrentWave() + " begins in " + frame.getCurrentArea() + "!");
        log("▶  " + party.get(activeCharIndex).getName() + "'s turn.");

        // Show first enemy detail on load
        if (!enemies.isEmpty()) updateDetailPanel(enemies.get(0));
    }

    // ─────────────────────────────────────────────────
    // BUILD — TOP BAR (wave info, party/enemy avatars, quick actions)
    // ─────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(0, 0));
        bar.setBackground(new Color(6, 5, 12));
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 48, 15)),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));

        // LEFT — wave info
        waveLabel = new JLabel(
            frame.getCurrentArea() + "  —  Wave " + frame.getCurrentWave() + " / 4");
        waveLabel.setFont(new Font("Serif", Font.BOLD, 14));
        waveLabel.setForeground(new Color(201, 148, 58));

        // CENTER — simple party avatar row (placeholder icons)
        JPanel avatarRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        avatarRow.setOpaque(false);
        for (Character c : party) {
            JLabel av = new JLabel(c.getName().substring(0, 1), SwingConstants.CENTER);
            av.setFont(new Font("Serif", Font.BOLD, 12));
            av.setForeground(new Color(200, 190, 150));
            av.setPreferredSize(new Dimension(32, 32));
            av.setBackground(new Color(30, 25, 45));
            av.setOpaque(true);
            av.setBorder(BorderFactory.createLineBorder(new Color(80, 65, 25), 1));
            avatarRow.add(av);
        }
        JLabel vs = new JLabel("VS");
        vs.setFont(new Font("Serif", Font.BOLD, 10));
        vs.setForeground(new Color(80, 70, 100));
        avatarRow.add(vs);
        for (Character e : enemies) {
            JLabel av = new JLabel(e.getName().substring(0, 1), SwingConstants.CENTER);
            av.setFont(new Font("Serif", Font.BOLD, 12));
            av.setForeground(new Color(200, 100, 100));
            av.setPreferredSize(new Dimension(32, 32));
            av.setBackground(new Color(35, 15, 15));
            av.setOpaque(true);
            av.setBorder(BorderFactory.createLineBorder(new Color(100, 40, 40), 1));
            avatarRow.add(av);
        }

        // RIGHT — gold display + action buttons
        JPanel rightRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightRow.setOpaque(false);

        JLabel goldLabel = new JLabel("🪙 " + frame.getGold());
        goldLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
        goldLabel.setForeground(new Color(201, 148, 58));
        rightRow.add(goldLabel);

        rightRow.add(makeTopBtn("Flee",      new Color(184,50,50),   new Color(220,100,100)));
        rightRow.add(makeTopBtn("Shop",      new Color(100,70,10),   new Color(201,148,58)));
        rightRow.add(makeTopBtn("Inventory", new Color(30, 50,120),  new Color(100,150,240)));

        bar.add(waveLabel, BorderLayout.WEST);
        bar.add(avatarRow, BorderLayout.CENTER);
        bar.add(rightRow,  BorderLayout.EAST);
        return bar;
    }

    private JButton makeTopBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Serif", Font.BOLD, 11));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(85, 28));
        btn.addActionListener(e -> {
            switch (text) {
                case "Flee"      -> doFlee();
                case "Shop"      -> frame.goToShop();
                case "Inventory" -> doItem();
            }
        });
        return btn;
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

            // Smaller + faster sprites on battle screen
            SpriteAnimator anim = new SpriteAnimator(e.getSpriteFolder(), 33, 95);
            anim.setAlignmentX(CENTER_ALIGNMENT);
            animators.put("enemy_" + i, anim);

            JLabel nameLabel = new JLabel(e.getName(), SwingConstants.CENTER);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            nameLabel.setForeground(new Color(220, 100, 100));
            nameLabel.setAlignmentX(CENTER_ALIGNMENT);

            HealthBar hb = new HealthBar(e, 160, 18);
            hb.setAlignmentX(CENTER_ALIGNMENT);
            healthBars.put("enemy_" + i, hb);

            slot.add(hb);                            // ← health bar drawn first = visually on top
slot.add(Box.createVerticalStrut(3));    // ← gap between bar and sprite (increase = more space)
slot.add(anim);                          // ← sprite below the bar
slot.add(Box.createVerticalStrut(4));    // ← gap at the bottom
slot.add(nameLabel);

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

    // ── CENTER: hero and enemy battlefield with battle log beneath ──
   private JPanel buildBattlefieldPanel() {

    JPanel panel = new JPanel(new BorderLayout(0, 12)) {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            BufferedImage bg = BACKGROUND_CACHE.get(backgroundKey());
            if (bg == null) return;
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                int pw = getWidth(), ph = getHeight();
                if (pw <= 0 || ph <= 0) return;
                int bw = bg.getWidth(), bh = bg.getHeight();
                if (bw <= 0 || bh <= 0) return;
                double scale = Math.max((double) pw / bw, (double) ph / bh);
                int dw = (int) Math.round(bw * scale);
                int dh = (int) Math.round(bh * scale);
                g2.drawImage(bg, (pw - dw) / 2, (ph - dh) / 2, dw, dh, null);
            } finally { g2.dispose(); }
        }
    };

    
    panel.setOpaque(false);
    panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 10, 14));

    // ── STAGGERED BATTLEFIELD — null layout so we control every slot's position ──
    JPanel battlefield = new JPanel(null);   // null = absolute positioning
    battlefield.setOpaque(false);
    battlefield.setPreferredSize(new Dimension(0, 340)); // ← height of battlefield area in px

    // ── HERO SIDE (left half of battlefield) ──
    //    Hero slots positioned by setBounds(x, y, width, height)
    //    x    = pixels from LEFT edge of the battlefield panel
    //    y    = pixels from TOP  edge — HIGHER y = lower on screen (further = smaller y)
    //    w/h  = size of the slot including sprite + bar

    for (int i = 0; i < party.size(); i++) {
        Character c = party.get(i);

        // Sprite sizes: front hero bigger, back hero smaller
        int spriteSize = (i == 0) ? 240 : 200;

        // Slot bounds for each position
        // Hero 0 = front-right, Hero 1 = back-left (behind and up)
        int slotW = spriteSize + 40;
        int slotH = spriteSize + 60; // ← +60 leaves room for the health bar above

        int slotX, slotY;
        if (i == 0) {
            slotX = 120;  // ← pixels from left — increase to push hero 1 right
            slotY = 140;  // ← pixels from top  — increase to push hero 1 DOWN (more foreground)
        } else {
            slotX = 20;   // ← hero 2 further left (behind hero 1)
            slotY = 130;   // ← hero 2 higher up on screen (more background/depth)
        }

        JPanel slot = buildCombatSlot(c, "party_" + i, i, new Color(15, 15, 30), spriteSize);
        slot.setBounds(slotX, slotY, slotW, slotH);
        battlefield.add(slot);
    }

    // ── ENEMY SIDE (right half of battlefield) ──
    //    Enemy 0 = front-left of enemy group
    //    Enemy 1 = back-right of enemy group
    for (int i = 0; i < enemies.size(); i++) {
        Character e = enemies.get(i);

        int spriteSize = (i == 0) ? 240 : 200;
        int slotW = spriteSize + 40;
        int slotH = spriteSize + 60;

        // X offset: enemies start from the center-right of the battlefield
        // The panel itself is the CENTER region; assume ~700px wide after sidebars
        // Adjust baseX if enemies appear too far left or right
        int baseX = 380; // ← increase = push all enemies right; decrease = push left

        int slotX, slotY;
        if (i == 0) {
            slotX = baseX + 80;  // ← enemy 1 front — further right
            slotY = 140;          // ← enemy 1 lower on screen (foreground)
        } else {
            slotX = baseX;        // ← enemy 2 back — to the left of enemy 1
            slotY = 130;           // ← enemy 2 higher (background)
        }

        JPanel slot = buildCombatSlot(e, "enemy_" + i, i, new Color(35, 10, 10), spriteSize);
        slot.setBounds(slotX, slotY, slotW, slotH);
        battlefield.add(slot);
        enemySlots.add(slot);

        // ── ENEMY HEALTH BARS ──

    }

    panel.add(battlefield, BorderLayout.CENTER);
    return panel;
}
private JPanel getEnemySlot(int index) {
    if (index < 0 || index >= enemySlots.size()) return null;
    return enemySlots.get(index);
}

    private JPanel buildCombatSlot(Character c, String key, int index,
                                Color tint, int spriteSize) {

    // Outer panel uses absolute (null) layout so we can layer bar ON TOP of sprite
    JPanel slot = new JPanel(null);
    slot.setOpaque(false);

    // ── 1. Sprite animator fills the whole slot ──
    SpriteAnimator anim = new SpriteAnimator(c.getSpriteFolder(), 33, spriteSize);
    anim.setBounds(0, 0, spriteSize, spriteSize);   // full slot width and height
    animators.put(key, anim);
    slot.add(anim);

    // ── 2. Name label — sits just above the health bar ──
    JLabel nameLabel = new JLabel(c.getName(), SwingConstants.CENTER);
    nameLabel.setFont(new Font("Serif", Font.BOLD, 11));
    nameLabel.setForeground(new Color(235, 235, 240));
    int nameLabelH = 16;
    int barH       = 14;   // ← height of the health bar in pixels; increase for a taller bar
    int barY       = 4;    // ← pixels from the TOP of the slot — 4 = almost at the very top of the sprite head
                           //   increase this to push the bar lower (e.g. 20 = inside the head region)
                           //   decrease to 0 to touch the absolute top edge
    int barW = spriteSize; // bar is exactly as wide as the sprite — no misalignment

    nameLabel.setBounds(0, barY - nameLabelH - 1, barW, nameLabelH);
    slot.add(nameLabel);

    // ── 3. Health bar — layered over the sprite, at the top ──
    HealthBar hb = new HealthBar(c, barW, barH);
    hb.setBounds(0, barY, barW, barH);  // x=0, y=barY (top of sprite), w=full, h=barH
    healthBars.put(key, hb);
    slot.add(hb);

    // ── 4. Enemy click handler ──
    if (key.startsWith("enemy_")) {
        final int idx = index;
        slot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        slot.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent ev) {
                if (!enemies.get(idx).isAlive()) return;
                selectedTarget = idx;
                highlightEnemyCard(idx);
                updateDetailPanel(enemies.get(idx));
                log("Targeting: " + enemies.get(idx).getName());
            }
        });
    }

    return slot;
}

/**
 * Slides a slot (identified by its animator key) toward a target X position,
 * plays the animation, deals damage via onDamage, then slides back.
 *
 * key        = "party_0" or "party_1"
 * targetSlot = the enemy's JPanel slot (so we know where to slide to)
 * animName   = "attack", "skill1", "skill2", "skill3"
 * onDamage   = runs after the approach + animation, before sliding back
 */
private void approachAndAttack(String key, JPanel targetSlot,
                                String animName, Runnable onDamage) {

    SpriteAnimator anim = animators.get(key);
    // Find the attacker's slot panel — it's the anim's parent
    JPanel attackerSlot = (anim != null) ? (JPanel) anim.getParent() : null;
    if (attackerSlot == null) { onDamage.run(); return; }

    // Record the attacker's starting position
    int startX = attackerSlot.getX();
    int startY = attackerSlot.getY();

    // Calculate where the enemy target is — we want to stop just beside it
    // targetSlot.getX() is in the battlefield panel's coordinate space
    int targetX = targetSlot.getX();

    // Decide approach direction: heroes approach right, enemies approach left
    // Heroes are on the left side (startX < 300), enemies on the right
    boolean heroIsAttacker = key.startsWith("party_");
    int approachX = heroIsAttacker
        ? targetX - attackerSlot.getWidth() - 10   // stop just left of the enemy
        : targetX + targetSlot.getWidth() + 10;     // enemy stops just right of the hero

    // ── Step 1: Slide toward target ──
    int stepPx    = 18;   // ← pixels per frame of movement — increase for faster stride
    int stepMs    = 16;   // ← ms between position updates (~60fps) — decrease for smoother
    int[] currentX = { startX };

    Timer slideIn = new Timer(stepMs, null);
    slideIn.addActionListener(e -> {
        int dx = approachX - currentX[0];
        if (Math.abs(dx) <= stepPx) {
            // Reached target — snap to position
            currentX[0] = approachX;
            attackerSlot.setLocation(approachX, startY);
            slideIn.stop();

            // ── Step 2: Play the attack animation ──
            if (anim != null) {
                anim.playOnce(animName, () -> {
                    onDamage.run();
                    // ── Step 3: Slide back to original position ──
                    Timer slideOut = new Timer(stepMs, null);
                    int[] cx = { approachX };
                    slideOut.addActionListener(ev -> {
                        int ddx = startX - cx[0];
                        if (Math.abs(ddx) <= stepPx) {
                            attackerSlot.setLocation(startX, startY);
                            slideOut.stop();
                        } else {
                            cx[0] += (ddx > 0) ? stepPx : -stepPx;
                            attackerSlot.setLocation(cx[0], startY);
                        }
                    });
                    slideOut.start();
                });
            } else {
                onDamage.run();
            }
        } else {
            currentX[0] += (dx > 0) ? stepPx : -stepPx;
            attackerSlot.setLocation(currentX[0], startY);
        }
    });
    slideIn.start();
}

    // ── EAST: enemy detail + active character mini stats ──
    private JPanel buildDetailPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(8, 6, 14));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(40, 30, 10)),
            BorderFactory.createEmptyBorder(14, 12, 14, 12)
        ));
        panel.setPreferredSize(new Dimension(200, 0));

        // ONLY show Active Hero + Log (no Enemy Detail section)
        JLabel sec2 = new JLabel("Active Hero");
        sec2.setFont(new Font("Serif", Font.BOLD, 12));
        sec2.setForeground(new Color(201, 148, 58));
        sec2.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(sec2);
        panel.add(Box.createVerticalStrut(6));

        // Active hero mini stats
        if (!party.isEmpty()) updateActiveHeroDetail(panel);

        // Log / result text INSIDE the right sidebar
        battleLog = new JTextArea();
        battleLog.setEditable(false);
        battleLog.setBackground(new Color(8, 8, 14, 220));
        battleLog.setForeground(new Color(200, 200, 220));
        battleLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        battleLog.setLineWrap(true);
        battleLog.setWrapStyleWord(true);
        battleLog.setOpaque(true);
        battleLog.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane logScroll = new JScrollPane(battleLog);
        logScroll.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 70)));
        logScroll.getViewport().setBackground(new Color(8, 8, 14, 220));
        logScroll.setPreferredSize(new Dimension(0, 260));

        panel.add(Box.createVerticalStrut(10));
        panel.add(logScroll);

        return panel;
    }

    private JLabel makeDetailLabel(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Serif", Font.BOLD, 13));
        l.setForeground(color);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JLabel makeDetailRow(String label, String value) {
        JLabel l = new JLabel(label + ":  " + value);
        l.setFont(new Font("Monospaced", Font.PLAIN, 10));
        l.setForeground(new Color(140, 135, 175));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    // Called when player clicks an enemy — refreshes right panel text
    private void updateDetailPanel(Character enemy) {
        if (detailName   == null) return;
        detailName.setText(enemy.getName());
        detailHp  .setText("HP:  " + enemy.getHp() + " / " + enemy.getMaxHp());
        detailAtk .setText("ATK: " + enemy.getAttackPower());
        detailDef .setText("DEF: " + enemy.getDefensePower());
        detailStatus.setText("Status: " + (enemy.isTaunted() ? "Taunted" : "Normal"));
        detailHpBar.update(enemy);
        repaint();
    }

    private void updateActiveHeroDetail(JPanel container) {
        Character actor = party.get(activeCharIndex);
        JLabel nameL = new JLabel(actor.getName());
        nameL.setFont(new Font("Serif", Font.BOLD, 12));
        nameL.setForeground(new Color(180, 200, 240));
        nameL.setAlignmentX(LEFT_ALIGNMENT);
        container.add(nameL);
        HealthBar heroHp = new HealthBar(actor, 160, 10);
        heroHp.setAlignmentX(LEFT_ALIGNMENT);
        container.add(heroHp);
    }

    private JPanel buildSkillPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(8, 6, 16));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(50, 40, 10)),
            BorderFactory.createEmptyBorder(14, 12, 14, 12)
        ));
        panel.setPreferredSize(new Dimension(200, 0));

        Character actor = party.get(activeCharIndex);

        // Panel title — shows which character's skills these are
        JLabel title = new JLabel("Skills — " + actor.getName());
        title.setFont(new Font("Serif", Font.BOLD, 12));
        title.setForeground(new Color(201, 148, 58));
        title.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(10));

        // Skill buttons — 3 skills in vertical order
        String[] icons  = {"★", "✦", "⚡"};
        Color[]  colors = {
            new Color(184,  50,  50),   // skill 1 — red
            new Color( 60, 100, 200),   // skill 2 — blue
            new Color(140,  50, 200)    // skill 3 / ulti — purple
        };

        for (int i = 0; i < actor.getSkillCount(); i++) {
            Skill  skill  = actor.getSkill(i);
            boolean canUse = actor.getMana() >= skill.getManaCost();
            Color   col    = colors[Math.min(i, colors.length - 1)];

            JPanel skillBtn = buildSkillButton(skill, icons[i], col, canUse, i);
            panel.add(skillBtn);
            panel.add(Box.createVerticalStrut(6));
        }

        // Basic attack at bottom
        panel.add(buildBasicAttackButton());
        panel.add(Box.createVerticalGlue());

        // Mana bar at very bottom
        panel.add(buildManaDisplay(actor));

        actionPanel = panel; // keep the reference so rebuildActionPanel() still works
        return panel;
    }

    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout(0, 0));
        bar.setBackground(new Color(5, 4, 10));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(50, 38, 10)));

        // LEFT half — party character cards
        JPanel partySection = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        partySection.setBackground(new Color(5, 4, 10));
        charCards.clear();

        for (int i = 0; i < party.size(); i++) {
            Character c = party.get(i);
            JPanel card = buildPartyCard(c, i);
            charCards.add(card);
            partySection.add(card);

            // Sprite animator for this character (bottom zone)
            // Smaller + faster sprites on bottom party strip
            SpriteAnimator anim = new SpriteAnimator(c.getSpriteFolder(), 33, 70);
animators.put("party_card_" + i, anim);

            // Health bar
            HealthBar hb = new HealthBar(c, 130, 10);
            healthBars.put("party_" + i, hb);
        }
        if (!charCards.isEmpty()) highlightActiveChar();

        // RIGHT half — enemy cards
        JPanel enemySection = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        enemySection.setBackground(new Color(10, 4, 4));
        enemySection.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(60, 20, 20)));
        enemyCards.clear();

        for (int i = 0; i < enemies.size(); i++) {
            Character e = enemies.get(i);
            JPanel card = buildEnemyCard(e, i);
            enemyCards.add(card);
            enemySection.add(card);
        }
        if (!enemyCards.isEmpty()) highlightEnemyCard(0);

        bar.add(partySection, BorderLayout.WEST);
        bar.add(enemySection, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildPartyCard(Character c, int charIndex) {
        JPanel card = new JPanel(new BorderLayout(8, 0));
        card.setBackground(new Color(14, 12, 22));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 40, 80), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        card.setPreferredSize(new Dimension(180, 70));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel portrait = new JLabel(c.getName().substring(0, 1), SwingConstants.CENTER);
        portrait.setFont(new Font("Serif", Font.BOLD, 20));
        portrait.setForeground(new Color(180, 160, 220));
        portrait.setBackground(new Color(25, 20, 40));
        portrait.setOpaque(true);
        portrait.setBorder(BorderFactory.createLineBorder(new Color(70, 55, 110), 1));
        portrait.setPreferredSize(new Dimension(44, 44));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(c.getName());
        nameLabel.setFont(new Font("Serif", Font.BOLD, 11));
        nameLabel.setForeground(c.isAlive() ? new Color(200, 180, 240) : new Color(80,70,90));

        JPanel hpRow = new JPanel(new BorderLayout(4,0));
        hpRow.setOpaque(false);
        JLabel hpIcon = new JLabel("♥");
        hpIcon.setFont(new Font("Serif", Font.PLAIN, 9));
        hpIcon.setForeground(new Color(70, 160, 80));
        JPanel hpTrack = new JPanel(){
            @Override protected void paintComponent(java.awt.Graphics g){
                super.paintComponent(g);
                g.setColor(new Color(20,20,30));
                g.fillRoundRect(0,2,getWidth(),getHeight()-4,3,3);
                double ratio = (double)c.getHp()/c.getMaxHp();
                Color fc = ratio > .5 ? new Color(50,180,70) : ratio > .25 ? new Color(180,150,20) : new Color(180,40,40);
                g.setColor(fc);
                g.fillRoundRect(0,2,(int)(getWidth()*ratio),getHeight()-4,3,3);
            }
        };
        hpTrack.setPreferredSize(new Dimension(0, 8));
        hpTrack.setOpaque(false);
        JLabel hpNum = new JLabel(c.getHp()+"/"+c.getMaxHp());
        hpNum.setFont(new Font("Monospaced", Font.PLAIN, 8));
        hpNum.setForeground(new Color(100,100,120));
        hpNum.setPreferredSize(new Dimension(55,10));
        hpRow.add(hpIcon,  BorderLayout.WEST);
        hpRow.add(hpTrack, BorderLayout.CENTER);
        hpRow.add(hpNum,   BorderLayout.EAST);

        JPanel mpRow = new JPanel(new BorderLayout(4,0));
        mpRow.setOpaque(false);
        JLabel mpIcon = new JLabel("◆");
        mpIcon.setFont(new Font("Serif", Font.PLAIN, 9));
        mpIcon.setForeground(new Color(50, 100, 200));
        JPanel mpTrack = new JPanel(){
            @Override protected void paintComponent(java.awt.Graphics g){
                super.paintComponent(g);
                g.setColor(new Color(20,20,30));
                g.fillRoundRect(0,2,getWidth(),getHeight()-4,3,3);
                g.setColor(new Color(40,90,190));
                g.fillRoundRect(0,2,(int)(getWidth()*(double)c.getMana()/c.getMaxMana()),getHeight()-4,3,3);
            }
        };
        mpTrack.setPreferredSize(new Dimension(0, 8));
        mpTrack.setOpaque(false);
        JLabel mpNum = new JLabel(c.getMana()+"/"+c.getMaxMana());
        mpNum.setFont(new Font("Monospaced", Font.PLAIN, 8));
        mpNum.setForeground(new Color(80,100,140));
        mpNum.setPreferredSize(new Dimension(55,10));
        mpRow.add(mpIcon,  BorderLayout.WEST);
        mpRow.add(mpTrack, BorderLayout.CENTER);
        mpRow.add(mpNum,   BorderLayout.EAST);

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(hpRow);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(mpRow);

        card.add(portrait,  BorderLayout.WEST);
        card.add(infoPanel, BorderLayout.CENTER);

        if (c.isAlive()) {
            card.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    activeCharIndex = charIndex;
                    highlightActiveChar();
                    rebuildActionPanel();
                    log("▶  Switched to " + c.getName() + "'s turn.");
                }
            });
        } else {
            card.setBackground(new Color(10, 8, 14));
        }
        return card;
    }

    private JPanel buildEnemyCard(Character e, int enemyIndex) {
        JPanel card = new JPanel(new BorderLayout(6,0));
        card.setBackground(new Color(18, 8, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 30, 30), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        card.setPreferredSize(new Dimension(155, 70));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(e.getName());
        nameLabel.setFont(new Font("Serif", Font.BOLD, 11));
        nameLabel.setForeground(new Color(220, 90, 90));

        JPanel hpRow = new JPanel(new BorderLayout(4,0));
        hpRow.setOpaque(false);
        JLabel hpTxt = new JLabel("HP  " + e.getHp() + "/" + e.getMaxHp());
        hpTxt.setFont(new Font("Monospaced", Font.PLAIN, 9));
        hpTxt.setForeground(new Color(120, 100, 100));
        HealthBar ehb = new HealthBar(e, 130, 8);
        healthBars.put("enemy_" + enemyIndex, ehb);
        hpRow.add(hpTxt, BorderLayout.NORTH);
        hpRow.add(ehb,   BorderLayout.CENTER);

        JLabel stats = new JLabel("ATK " + e.getAttackPower() + "  DEF " + e.getDefensePower());
        stats.setFont(new Font("Monospaced", Font.PLAIN, 8));
        stats.setForeground(new Color(100, 85, 85));

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(hpRow);
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(stats);

        card.add(infoPanel, BorderLayout.CENTER);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent ev) {
                if (!e.isAlive()) return;
                selectedTarget = enemyIndex;
                highlightEnemyCard(enemyIndex);
                updateDetailPanel(e);
                log("Targeting: " + e.getName());
            }
        });
        return card;
    }

    private void highlightActiveChar() {
        for (int i = 0; i < charCards.size(); i++) {
            JPanel card = charCards.get(i);
            if (i == activeCharIndex) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(180, 140, 30), 2),
                    BorderFactory.createEmptyBorder(7, 9, 7, 9)
                ));
                card.setBackground(new Color(22, 18, 10));
            } else {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(50, 40, 80), 1),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
                card.setBackground(new Color(14, 12, 22));
            }
        }
        repaint();
    }

    private void highlightEnemyCard(int idx) {
        for (int i = 0; i < enemyCards.size(); i++) {
            JPanel card = enemyCards.get(i);
            if (i == idx) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 160, 30), 2),
                    BorderFactory.createEmptyBorder(7, 9, 7, 9)
                ));
                card.setBackground(new Color(26, 18, 8));
            } else {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(80, 30, 30), 1),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
                card.setBackground(new Color(18, 8, 8));
            }
        }
        repaint();
    }

    // Builds one skill button row
    private JPanel buildSkillButton(Skill skill, String icon, Color col,
                                     boolean canUse, int skillIndex) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(withAlpha(col, canUse ? 30 : 10));
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(withAlpha(col, canUse ? 100 : 40), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        row.setCursor(canUse
            ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            : Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Serif", Font.BOLD, 16));
        iconLabel.setForeground(canUse ? new Color(col.getRed(), col.getGreen()+60, col.getBlue())
                                       : new Color(80, 80, 80));
        iconLabel.setPreferredSize(new Dimension(24, 24));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        JLabel nameLabel = new JLabel(skill.getName());
        nameLabel.setFont(new Font("Serif", Font.BOLD, 11));
        nameLabel.setForeground(canUse ? new Color(210, 200, 230) : new Color(80, 80, 80));
        JLabel mpLabel = new JLabel(skill.getManaCost() + " MP");
        mpLabel.setFont(new Font("Monospaced", Font.PLAIN, 9));
        mpLabel.setForeground(new Color(100, 100, 140));
        info.add(nameLabel);
        info.add(mpLabel);

        row.add(iconLabel, BorderLayout.WEST);
        row.add(info,      BorderLayout.CENTER);

        if (canUse) {
            final int idx = skillIndex;
            row.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) { doSkill(idx); }
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    row.setBackground(withAlpha(col, 55));
                    row.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(withAlpha(col, 180), 1),
                        BorderFactory.createEmptyBorder(8, 10, 8, 10)));
                }
                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    row.setBackground(withAlpha(col, 30));
                    row.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(withAlpha(col, 100), 1),
                        BorderFactory.createEmptyBorder(8, 10, 8, 10)));
                }
            });
        }
        return row;
    }

    private JPanel buildBasicAttackButton() {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(new Color(40, 30, 10));
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 75, 20), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel icon = new JLabel("⚔");
        icon.setFont(new Font("Serif", Font.BOLD, 16));
        icon.setForeground(new Color(201, 148, 58));
        icon.setPreferredSize(new Dimension(24, 24));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        JLabel name = new JLabel("Basic Attack");
        name.setFont(new Font("Serif", Font.BOLD, 11));
        name.setForeground(new Color(220, 190, 100));
        JLabel sub = new JLabel("Free · standard");
        sub.setFont(new Font("Monospaced", Font.PLAIN, 9));
        sub.setForeground(new Color(120, 100, 60));
        info.add(name);
        info.add(sub);

        row.add(icon, BorderLayout.WEST);
        row.add(info, BorderLayout.CENTER);
        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { doAttack(); }
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                row.setBackground(new Color(70, 50, 15));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                row.setBackground(new Color(40, 30, 10));
            }
        });
        return row;
    }

    private JPanel buildManaDisplay(Character actor) {
        JPanel mp = new JPanel();
        mp.setLayout(new BoxLayout(mp, BoxLayout.Y_AXIS));
        mp.setOpaque(false);
        mp.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(30, 25, 50)),
            BorderFactory.createEmptyBorder(8, 0, 0, 0)
        ));
        mp.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lbl = new JLabel("MP: " + actor.getMana() + " / " + actor.getMaxMana());
        lbl.setFont(new Font("Monospaced", Font.PLAIN, 10));
        lbl.setForeground(new Color(80, 120, 210));
        mp.add(lbl);
        // You can add a visual bar here later using HealthBar adapted for mana
        return mp;
    }

    // ─────────────────────────────────────────────────
    // ACTIONS
    // ─────────────────────────────────────────────────
    private void doAttack() {
    if (!playerTurn) return;
    Character actor  = party.get(activeCharIndex);
    Character target = enemies.get(selectedTarget);
    if (!target.isAlive()) { log("That enemy is dead. Pick another."); return; }

    // Get the enemy's slot so we know where to approach
    JPanel targetSlot = getEnemySlot(selectedTarget);

    Runnable onDamage = () -> {
        BattleEngine.AttackResult result = BattleEngine.playerAttack(actor, target);
        log(result.message);
        updateAllBars();
        if (!target.isAlive()) {
            log(target.getName() + " was defeated!");
            frame.addEnemyKill();
            SpriteAnimator deathAnim = animators.get("enemy_" + selectedTarget);
            if (deathAnim != null) deathAnim.playOnce("death", () -> {});
        }
        frame.addTurn();
        checkWaveOver();
        if (!BattleEngine.isEnemyWaveDefeated(enemies)) endPlayerTurn();
    };

    if (targetSlot != null) {
        approachAndAttack("party_" + activeCharIndex, targetSlot, "attack", onDamage);
    } else {
        // No slot reference — fall back to play-in-place
        SpriteAnimator anim = animators.get("party_" + activeCharIndex);
        if (anim != null) anim.playOnce("attack", onDamage);
        else onDamage.run();
    }
}

    private void doSkill(int skillIndex) {
    if (!playerTurn) return;
    Character actor  = party.get(activeCharIndex);
    Character target = enemies.get(selectedTarget);
    Skill     skill  = actor.getSkill(skillIndex);

    if (actor.getMana() < skill.getManaCost()) {
        log("Not enough mana for " + skill.getName() + "!"); return;
    }

    String animName = switch (skillIndex) {
        case 0 -> "skill1";
        case 1 -> "skill2";
        case 2 -> "skill3";
        default -> "attack";
    };

    Runnable onDamage = () -> {
        if (skill.getType().equals("damage_all")) {
            BattleEngine.playerSkillAoe(actor, skillIndex, enemies)
                .forEach(r -> log(r.message));
        } else {
            log(BattleEngine.playerSkill(actor, skillIndex, target).message);
        }
        updateAllBars();
        checkWaveOver();
        if (!BattleEngine.isEnemyWaveDefeated(enemies)) endPlayerTurn();
    };

    // skill2 (index 1) is often ranged (Multi Slash flies out) — skip approach for AOE
    boolean isMelee = !skill.getType().equals("damage_all");
    JPanel targetSlot = getEnemySlot(selectedTarget);

    if (isMelee && targetSlot != null) {
        approachAndAttack("party_" + activeCharIndex, targetSlot, animName, onDamage);
    } else {
        SpriteAnimator anim = animators.get("party_" + activeCharIndex);
        if (anim != null) anim.playOnce(animName, onDamage);
        else onDamage.run();
    }
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
    log("🏆 All waves defeated! You win!");

    // ADD this block before the Timer:
    try {
        io.SaveManager.registerOrUpdatePlayer(
            frame.getUsername(),
            frame.getCurrentWave(),     // highest wave reached
            frame.getEnemiesKilled(),
            frame.getTurnsTotal(),
            frame.getCurrentArea()
        );
        io.SaveManager.clearSaveData(); // ← session over: wipe the active save
    } catch (java.io.IOException e) {
        System.out.println("Could not update leaderboard: " + e.getMessage());
    }

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
            case "Academia" -> "assets/background/haunted.png";
            case "Dungeon" -> "assets/background/ritual.png";
            default        -> "assets/background/forest.png";
        };
    }

    private void preloadBackgroundForArea(String area) {
        String key = switch (area) {
            case "Forest"  -> "assets/background/forest.png";
            case "Academia" -> "assets/background/haunted.png";
            case "Dungeon" -> "assets/background/ritual.png";
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
        // Background is now rendered only in the center battlefield panel
        super.paintComponent(g);
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
        actionPanel = buildSkillPanel();
        add(actionPanel, BorderLayout.WEST);  // WEST not EAST
        highlightActiveChar();
        revalidate();
        repaint();
    }

    private void highlightTarget(int idx) {
        // Visual feedback — briefly tint the selected enemy slot
        // You can enhance this with a border or glow later
        repaint();
    }

    // Utility — Color with custom alpha
    private Color withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
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
