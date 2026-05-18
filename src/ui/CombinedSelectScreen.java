package ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Combined Area + Character Selection Screen
 * Upper: Area selection with landscape carousel (left/right arrows)
 * Lower: Character selection with horizontal scroll
 */
public class CombinedSelectScreen extends JPanel {

    private final List<core.Character> selectedHeroes = new ArrayList<>();
    private final List<JPanel> heroCards = new ArrayList<>();
    
    // Character pool - single instance to avoid comparison issues
    private final List<core.Character> heroPool = List.of(
        new characters.Arthur("Arthur"),
        new characters.Fabby("Fabby"),
        new characters.Mohammad("Mohammad"),
        new characters.Star("Star"),
        new characters.Tapanh("Tapanh"),
        new characters.Van("Van")
    );
    
    // Area data
    private static class AreaData {
        String name;
        String displayName;
        String lore;
        String difficulty;
        String landscapePath;
        Color accentColor;
        
        AreaData(String name, String displayName, String lore, String difficulty, 
                 String landscapePath, Color accentColor) {
            this.name = name;
            this.displayName = displayName;
            this.lore = lore;
            this.difficulty = difficulty;
            this.landscapePath = landscapePath;
            this.accentColor = accentColor;
        }
    }
    
    private final List<AreaData> areas = List.of(
        new AreaData("Forest", "🌲 Whispering Woods",
            "Deep within the ancient forest, twisted creatures lurk beneath the canopy. " +
            "Goblins and trolls have claimed these woods as their domain, terrorizing nearby villages. " +
            "Legends speak of a great dragon that slumbers in the heart of the forest, " +
            "guarding treasures beyond imagination. The trees themselves seem to whisper warnings " +
            "to those brave—or foolish—enough to venture forth.\n\n" +
            "Enemies: Goblins, Trolls, Forest Beasts\n" +
            "Boss: Ancient Forest Dragon\n\n" +
            "Difficulty: ★☆☆ EASY\n" +
            "Recommended for beginners seeking adventure.",
            "EASY",
            "assets/landscapes/forest_bg.png",
            new Color(100, 200, 100)),
            
        new AreaData("Academia", "🏚 Cursed Academia",
            "Once a prestigious academy of magic, now a haunted ruin where dark forces reign. " +
            "Skeletons of fallen students wander the halls, and corrupted mages practice forbidden arts. " +
            "The Dark Mage Lord has claimed the grand library as his throne room, " +
            "commanding legions of undead and wielding powers that defy the natural order. " +
            "Only the bravest heroes dare to challenge the darkness that has consumed this place.\n\n" +
            "Enemies: Skeleton Warriors, Dark Mages, Cursed Spirits\n" +
            "Boss: Dark Mage Lord\n\n" +
            "Difficulty: ★★☆ MEDIUM\n" +
            "For experienced adventurers ready for a challenge.",
            "MEDIUM",
            "assets/landscapes/academia_bg.png",
            new Color(160, 100, 220)),
            
        new AreaData("Dungeon", "🌋 Infernal Depths",
            "In the deepest chambers beneath the volcano, where molten rock flows like rivers, " +
            "the Lava Titans have awakened from their ancient slumber. These colossal beings of fire and stone " +
            "guard the path to the Titan King's throne—a creature of pure elemental fury. " +
            "The heat alone is enough to melt steel, and the very ground trembles with each step of these giants. " +
            "Only the most powerful heroes can hope to survive the infernal depths.\n\n" +
            "Enemies: Lava Elementals, Fire Golems, Magma Serpents\n" +
            "Boss: Lava Titan King\n\n" +
            "Difficulty: ★★★ HARD\n" +
            "For master warriors seeking ultimate glory.",
            "HARD",
            "assets/landscapes/dungeon_bg.png",
            new Color(255, 100, 50))
    );
    
    private int currentAreaIndex = 0;
    private JLabel landscapeLabel;
    private JTextArea loreTextArea;
    private JLabel areaNameLabel;
    
    private int currentHeroIndex = 0;
    private JLabel heroPortraitLabel;
    private JTextArea heroDetailsArea;
    private JLabel heroNameLabel;
    private JPanel selectedHeroesPanel;
    private JButton selectButton; // Store reference to the select button
    
    public CombinedSelectScreen(MainFrame frame) {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(12, 10, 18));
        
        // ── UPPER SECTION: Area Selection (shorter) ──
        JPanel upperPanel = buildUpperPanel(frame);
        add(upperPanel, BorderLayout.NORTH);
        
        // ── LOWER SECTION: Character Selection (taller) ──
        JPanel lowerPanel = buildLowerPanel(frame);
        add(lowerPanel, BorderLayout.CENTER);
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // UPPER PANEL: Area Selection with Landscape Carousel (SHORTER)
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel buildUpperPanel(MainFrame frame) {
        JPanel panel = new JPanel(new BorderLayout(20, 0));
        panel.setBackground(new Color(12, 10, 18));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 15, 40));
        panel.setPreferredSize(new Dimension(0, 280)); // Shorter height
        
        // ── LEFT: Lore Scroll Panel ──
        JPanel lorePanel = buildLorePanel();
        panel.add(lorePanel, BorderLayout.CENTER);
        
        // ── RIGHT: Landscape with Arrows ──
        JPanel landscapePanel = buildLandscapePanel(frame);
        panel.add(landscapePanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel buildLorePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(450, 0));
        
        // Title
        areaNameLabel = new JLabel(areas.get(currentAreaIndex).displayName, SwingConstants.CENTER);
        areaNameLabel.setFont(new Font("Serif", Font.BOLD, 22));
        areaNameLabel.setForeground(areas.get(currentAreaIndex).accentColor);
        
        // Scroll-like background for lore text
        JPanel scrollPanel = new JPanel(new BorderLayout());
        scrollPanel.setBackground(new Color(35, 30, 25));
        scrollPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 80, 50), 2),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 50, 35), 1),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
            )
        ));
        
        loreTextArea = new JTextArea(areas.get(currentAreaIndex).lore);
        loreTextArea.setFont(new Font("Serif", Font.PLAIN, 12));
        loreTextArea.setForeground(new Color(220, 210, 190));
        loreTextArea.setBackground(new Color(35, 30, 25));
        loreTextArea.setLineWrap(true);
        loreTextArea.setWrapStyleWord(true);
        loreTextArea.setEditable(false);
        loreTextArea.setFocusable(false);
        
        JScrollPane scrollPane = new JScrollPane(loreTextArea);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        scrollPanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(areaNameLabel, BorderLayout.NORTH);
        panel.add(scrollPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel buildLandscapePanel(MainFrame frame) {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(480, 0));
        
        // ── Landscape Frame ──
        JPanel framePanel = new JPanel(new BorderLayout());
        framePanel.setBackground(new Color(20, 18, 25));
        framePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 70, 60), 3),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
        landscapeLabel = new JLabel();
        landscapeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        landscapeLabel.setVerticalAlignment(SwingConstants.CENTER);
        landscapeLabel.setPreferredSize(new Dimension(400, 200)); // Smaller
        updateLandscapeImage();
        
        framePanel.add(landscapeLabel, BorderLayout.CENTER);
        
        // ── Arrow Buttons ──
        JPanel arrowPanel = new JPanel(new BorderLayout());
        arrowPanel.setOpaque(false);
        
        JButton leftArrow = createArrowButton("◀", true, frame, 200);
        JButton rightArrow = createArrowButton("▶", false, frame, 200);
        
        arrowPanel.add(leftArrow, BorderLayout.WEST);
        arrowPanel.add(framePanel, BorderLayout.CENTER);
        arrowPanel.add(rightArrow, BorderLayout.EAST);
        
        panel.add(arrowPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JButton createArrowButton(String text, boolean isLeft, MainFrame frame, int height) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Serif", Font.BOLD, 28));
        btn.setForeground(new Color(220, 200, 150));
        btn.setBackground(new Color(40, 35, 30, 200));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 85, 60), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(50, height));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(60, 50, 40, 240));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(40, 35, 30, 200));
            }
        });
        
        btn.addActionListener(e -> {
            if (isLeft) {
                currentAreaIndex = (currentAreaIndex - 1 + areas.size()) % areas.size();
            } else {
                currentAreaIndex = (currentAreaIndex + 1) % areas.size();
            }
            updateAreaDisplay();
        });
        
        return btn;
    }
    
    private void updateLandscapeImage() {
        try {
            File imgFile = new File(areas.get(currentAreaIndex).landscapePath);
            if (imgFile.exists()) {
                BufferedImage img = ImageIO.read(imgFile);
                Image scaled = img.getScaledInstance(400, 200, Image.SCALE_SMOOTH);
                landscapeLabel.setIcon(new ImageIcon(scaled));
            } else {
                landscapeLabel.setIcon(null);
                landscapeLabel.setText("Image not found");
            }
        } catch (IOException e) {
            landscapeLabel.setIcon(null);
            landscapeLabel.setText("Error loading image");
        }
    }
    
    private void updateAreaDisplay() {
        AreaData area = areas.get(currentAreaIndex);
        areaNameLabel.setText(area.displayName);
        areaNameLabel.setForeground(area.accentColor);
        loreTextArea.setText(area.lore);
        updateLandscapeImage();
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // LOWER PANEL: Character Selection with Carousel (TALLER)
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel buildLowerPanel(MainFrame frame) {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(new Color(12, 10, 18));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(60, 50, 70)),
            BorderFactory.createEmptyBorder(20, 40, 25, 40)
        ));
        
        // Title
        JLabel heroTitle = new JLabel("⚔ Select Your 2 Heroes", SwingConstants.CENTER);
        heroTitle.setFont(new Font("Serif", Font.BOLD, 24));
        heroTitle.setForeground(new Color(240, 200, 120));
        heroTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout(20, 0));
        contentPanel.setOpaque(false);
        
        // ── LEFT: Hero Portrait with Arrows ──
        JPanel portraitPanel = buildHeroPortraitPanel(frame);
        contentPanel.add(portraitPanel, BorderLayout.WEST);
        
        // ── CENTER: Hero Details ──
        JPanel detailsPanel = buildHeroDetailsPanel(frame);
        contentPanel.add(detailsPanel, BorderLayout.CENTER);
        
        // ── RIGHT: Selected Heroes Container ──
        JPanel selectedPanel = buildSelectedHeroesPanel(frame);
        contentPanel.add(selectedPanel, BorderLayout.EAST);
        
        panel.add(heroTitle, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel buildHeroPortraitPanel(MainFrame frame) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(450, 0));
        
        // ── Portrait Frame ──
        JPanel framePanel = new JPanel(new BorderLayout());
        framePanel.setBackground(new Color(20, 18, 25));
        framePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 70, 60), 3),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        heroPortraitLabel = new JLabel();
        heroPortraitLabel.setHorizontalAlignment(SwingConstants.CENTER);
        heroPortraitLabel.setVerticalAlignment(SwingConstants.CENTER);
        heroPortraitLabel.setPreferredSize(new Dimension(380, 280));
        updateHeroPortrait();
        
        framePanel.add(heroPortraitLabel, BorderLayout.CENTER);
        
        // ── Arrow Buttons ──
        JPanel arrowPanel = new JPanel(new BorderLayout());
        arrowPanel.setOpaque(false);
        
        JButton leftArrow = createHeroArrowButton("◀", true, frame, 280);
        JButton rightArrow = createHeroArrowButton("▶", false, frame, 280);
        
        arrowPanel.add(leftArrow, BorderLayout.WEST);
        arrowPanel.add(framePanel, BorderLayout.CENTER);
        arrowPanel.add(rightArrow, BorderLayout.EAST);
        
        panel.add(arrowPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JButton createHeroArrowButton(String text, boolean isLeft, MainFrame frame, int height) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Serif", Font.BOLD, 28));
        btn.setForeground(new Color(220, 200, 150));
        btn.setBackground(new Color(40, 35, 30, 200));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 85, 60), 2),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(50, height));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(60, 50, 40, 240));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(40, 35, 30, 200));
            }
        });
        
        btn.addActionListener(e -> {
            if (isLeft) {
                currentHeroIndex = (currentHeroIndex - 1 + heroPool.size()) % heroPool.size();
            } else {
                currentHeroIndex = (currentHeroIndex + 1) % heroPool.size();
            }
            updateHeroDisplay();
        });
        
        return btn;
    }
    
    private JPanel buildHeroDetailsPanel(MainFrame frame) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(400, 0));
        
        // Hero name
        heroNameLabel = new JLabel("", SwingConstants.CENTER);
        heroNameLabel.setFont(new Font("Serif", Font.BOLD, 26));
        heroNameLabel.setForeground(new Color(255, 220, 150));
        
        // Details scroll panel
        JPanel scrollPanel = new JPanel(new BorderLayout());
        scrollPanel.setBackground(new Color(35, 30, 25));
        scrollPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 80, 50), 2),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 50, 35), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
            )
        ));
        
        heroDetailsArea = new JTextArea();
        heroDetailsArea.setFont(new Font("Serif", Font.PLAIN, 13));
        heroDetailsArea.setForeground(new Color(220, 210, 190));
        heroDetailsArea.setBackground(new Color(35, 30, 25));
        heroDetailsArea.setLineWrap(true);
        heroDetailsArea.setWrapStyleWord(true);
        heroDetailsArea.setEditable(false);
        heroDetailsArea.setFocusable(false);
        
        JScrollPane scrollPane = new JScrollPane(heroDetailsArea);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        scrollPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Select button
        selectButton = new JButton("SELECT HERO");
        selectButton.setFont(new Font("Serif", Font.BOLD, 14));
        selectButton.setBackground(new Color(70, 40, 90));
        selectButton.setForeground(new Color(255, 240, 200));
        selectButton.setFocusPainted(false);
        selectButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(130, 90, 160), 2),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        selectButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        selectButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                selectButton.setBackground(new Color(90, 60, 110));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                selectButton.setBackground(new Color(70, 40, 90));
            }
        });
        
        selectButton.addActionListener(e -> {
            core.Character currentHero = heroPool.get(currentHeroIndex);
            
            if (selectedHeroes.contains(currentHero)) {
                // Unselect the hero
                selectedHeroes.remove(currentHero);
            } else if (selectedHeroes.size() < 2) {
                // Select the hero
                selectedHeroes.add(currentHero);
            } else {
                // Already have 2 heroes selected
                JOptionPane.showMessageDialog(frame,
                    "You can only select 2 heroes. Deselect one first.",
                    "Max 2 Heroes", JOptionPane.INFORMATION_MESSAGE);
            }
            updateSelectedHeroesDisplay();
            updateSelectButtonState();
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(selectButton);
        
        panel.add(heroNameLabel, BorderLayout.NORTH);
        panel.add(scrollPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Initialize display
        updateHeroDisplay();
        
        return panel;
    }
    
    private JPanel buildSelectedHeroesPanel(MainFrame frame) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(25, 20, 30));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 80, 120), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setPreferredSize(new Dimension(180, 0));
        
        JLabel title = new JLabel("Selected Heroes");
        title.setFont(new Font("Serif", Font.BOLD, 14));
        title.setForeground(new Color(220, 200, 150));
        title.setAlignmentX(CENTER_ALIGNMENT);
        
        panel.add(title);
        panel.add(Box.createVerticalStrut(15));
        
        selectedHeroesPanel = new JPanel();
        selectedHeroesPanel.setLayout(new BoxLayout(selectedHeroesPanel, BoxLayout.Y_AXIS));
        selectedHeroesPanel.setOpaque(false);
        
        panel.add(selectedHeroesPanel);
        panel.add(Box.createVerticalGlue());
        
        // Begin Adventure button
        JButton beginBtn = new JButton("Begin Adventure ▶");
        beginBtn.setFont(new Font("Serif", Font.BOLD, 14));
        beginBtn.setBackground(new Color(70, 40, 90));
        beginBtn.setForeground(new Color(255, 240, 200));
        beginBtn.setFocusPainted(false);
        beginBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(130, 90, 160), 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        beginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        beginBtn.setAlignmentX(CENTER_ALIGNMENT);
        
        beginBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                beginBtn.setBackground(new Color(90, 60, 110));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                beginBtn.setBackground(new Color(70, 40, 90));
            }
        });
        
        beginBtn.addActionListener(e -> {
            if (selectedHeroes.size() != 2) {
                JOptionPane.showMessageDialog(frame,
                    "You must select exactly 2 heroes!",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            frame.setParty(new ArrayList<>(selectedHeroes));
            frame.setCurrentArea(areas.get(currentAreaIndex).name);
            frame.setCurrentWave(1);
            frame.goToBattle();
        });
        
        panel.add(Box.createVerticalStrut(15));
        panel.add(beginBtn);
        
        return panel;
    }
    
    private void updateHeroPortrait() {
        try {
            String heroName = heroPool.get(currentHeroIndex).getName().toLowerCase();
            File imgFile = new File("assets/portraits/" + heroName + ".png");
            if (imgFile.exists()) {
                BufferedImage img = ImageIO.read(imgFile);
                // Horizontal portrait - fit to frame
                Image scaled = img.getScaledInstance(380, 280, Image.SCALE_SMOOTH);
                heroPortraitLabel.setIcon(new ImageIcon(scaled));
            } else {
                heroPortraitLabel.setIcon(null);
                heroPortraitLabel.setText("Portrait not found");
            }
        } catch (IOException e) {
            heroPortraitLabel.setIcon(null);
            heroPortraitLabel.setText("Error loading portrait");
        }
    }
    
    private void updateHeroDisplay() {
        core.Character hero = heroPool.get(currentHeroIndex);
        
        // Update portrait
        updateHeroPortrait();
        
        // Update name
        heroNameLabel.setText(hero.getName());
        
        // Update details with lore and stats
        String details = generateHeroDetails(hero);
        heroDetailsArea.setText(details);
        
        // Update select button state
        updateSelectButtonState();
    }
    
    private String generateHeroDetails(core.Character hero) {
        StringBuilder sb = new StringBuilder();
        
        // Class and description
        sb.append("Class: ").append(hero.getClassDescription().split("\n")[0]).append("\n\n");
        
        // Lore/Backstory
        sb.append("═══ BACKSTORY ═══\n");
        String backstory = getHeroBackstory(hero.getName());
        sb.append(backstory).append("\n\n");
        
        // Stats
        sb.append("═══ COMBAT STATS ═══\n");
        sb.append("❤ Health: ").append(hero.getMaxHp()).append(" HP\n");
        sb.append("✦ Mana: ").append(hero.getMaxMana()).append(" MP\n");
        sb.append("⚔ Attack: ").append(hero.getAttackPower()).append("\n");
        sb.append("🛡 Defense: ").append(hero.getDefensePower()).append("\n");
        sb.append("⚡ Speed: ").append(hero.getSpeed()).append("\n\n");
        
        // Skills
        sb.append("═══ ABILITIES ═══\n");
        for (int i = 0; i < hero.getSkillCount(); i++) {
            core.Skill skill = hero.getSkill(i);
            sb.append("• ").append(skill.getName()).append(" (").append(skill.getManaCost()).append(" MP)\n");
            sb.append("  ").append(skill.getDescription()).append("\n");
        }
        
        return sb.toString();
    }
    
    private String getHeroBackstory(String name) {
        return switch (name) {
            case "Arthur" -> "A noble knight sworn to protect the realm. Arthur wields his blade with honor and courage, " +
                           "leading his companions through the darkest of battles. His unwavering determination inspires all who fight beside him.";
            case "Fabby" -> "A mysterious mage who commands the arcane arts with precision. Fabby's magical prowess is matched only by " +
                          "her wisdom, making her an invaluable ally in any quest for knowledge and power.";
            case "Mohammad" -> "A skilled warrior from distant lands, Mohammad brings exotic fighting techniques and unmatched discipline. " +
                             "His journey has taken him across countless battlefields, honing his skills to perfection.";
            case "Star" -> "A celestial guardian blessed with divine powers. Star's radiant energy heals the wounded and smites the wicked, " +
                         "serving as a beacon of hope in the face of overwhelming darkness.";
            case "Tapanh" -> "A cunning rogue who strikes from the shadows. Tapanh's agility and precision make him a deadly force, " +
                           "capable of turning the tide of battle with a single well-placed strike.";
            case "Van" -> "A battle-hardened veteran who has seen countless wars. Van's experience and tactical mind make him an exceptional " +
                        "leader, capable of adapting to any situation with calm resolve.";
            default -> "A brave hero ready for adventure.";
        };
    }
    
    private void updateSelectButtonState() {
        core.Character currentHero = heroPool.get(currentHeroIndex);
        if (selectedHeroes.contains(currentHero)) {
            selectButton.setText("UNSELECT");
        } else {
            selectButton.setText("SELECT HERO");
        }
    }
    
    private void updateSelectedHeroesDisplay() {
        selectedHeroesPanel.removeAll();
        
        for (core.Character hero : selectedHeroes) {
            JPanel heroSlot = new JPanel();
            heroSlot.setLayout(new BoxLayout(heroSlot, BoxLayout.Y_AXIS));
            heroSlot.setBackground(new Color(40, 35, 45));
            heroSlot.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(120, 100, 140), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            heroSlot.setMaximumSize(new Dimension(150, 60));
            
            JLabel nameLabel = new JLabel(hero.getName());
            nameLabel.setFont(new Font("Serif", Font.BOLD, 14));
            nameLabel.setForeground(new Color(255, 235, 180));
            nameLabel.setAlignmentX(CENTER_ALIGNMENT);
            
            JLabel classLabel = new JLabel(hero.getClassDescription().split("\n")[0]);
            classLabel.setFont(new Font("Serif", Font.PLAIN, 11));
            classLabel.setForeground(new Color(200, 185, 150));
            classLabel.setAlignmentX(CENTER_ALIGNMENT);
            
            heroSlot.add(nameLabel);
            heroSlot.add(Box.createVerticalStrut(3));
            heroSlot.add(classLabel);
            
            selectedHeroesPanel.add(heroSlot);
            selectedHeroesPanel.add(Box.createVerticalStrut(10));
        }
        
        selectedHeroesPanel.revalidate();
        selectedHeroesPanel.repaint();
    }
    
    private BufferedImage loadPortrait(String characterName) {
        String path = "assets/portraits/" + characterName.toLowerCase() + ".png";
        try {
            File f = new File(path);
            if (f.exists()) return ImageIO.read(f);
        } catch (IOException ignored) {}
        return null;
    }
}
