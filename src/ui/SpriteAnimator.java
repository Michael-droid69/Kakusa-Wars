package ui;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.Timer;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;


public class SpriteAnimator extends JLabel {

    private final String  baseFolder;
    private final int     frameDelayMs;
    private int           iconSizePx;  // Not final - can be adjusted for Kapre
    private final boolean flipX;

    // The single timer that drives all frame ticks
    private Timer timer;
    
    // ═══════════════════════════════════════════════════════════
    // FRAME SKIPPING OPTIMIZATION
    // ═══════════════════════════════════════════════════════════
    // Load every Nth frame to reduce memory usage
    // 1 = load all frames (no skip)
    // 2 = load every 2nd frame (50% reduction)
    // 3 = load every 3rd frame (66% reduction)
    // 4 = load every 4th frame (75% reduction) - CHUNKY PIXEL ART
    private static final int FRAME_SKIP = 3;  // ← Increased for chunkier feel + better performance

    // Current animation state
    private List<ImageIcon> currentFrames = new ArrayList<>();
    private String          currentAnim   = "";
    private int             frameIndex    = 0;

    // playOnce state — null when not in a one-shot animation
    private int      onceTotal    = 0;      // total frames in the one-shot anim
    private int      onceTicked   = 0;      // how many ticks have fired so far
    private Runnable onceCallback = null;   // runs when the one-shot finishes
    private boolean  stopped      = false;  // true after stop() is called

    // ── Static frame cache shared across all instances ──
    private static final java.util.Map<String, List<ImageIcon>> CACHE
        = new java.util.HashMap<>();
    private static final String SEP = "_";
    
    // Performance optimization: Reuse rendering hints
    private static final java.awt.RenderingHints FAST_HINTS = new java.awt.RenderingHints(
        java.awt.RenderingHints.KEY_RENDERING,
        java.awt.RenderingHints.VALUE_RENDER_SPEED
    );
    
    static {
        FAST_HINTS.put(java.awt.RenderingHints.KEY_ANTIALIASING, 
                      java.awt.RenderingHints.VALUE_ANTIALIAS_OFF);
        FAST_HINTS.put(java.awt.RenderingHints.KEY_INTERPOLATION, 
                      java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        FAST_HINTS.put(java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION, 
                      java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        FAST_HINTS.put(java.awt.RenderingHints.KEY_COLOR_RENDERING, 
                      java.awt.RenderingHints.VALUE_COLOR_RENDER_SPEED);
    }

    // ─────────────────────────────────────────────────
    // CONSTRUCTORS
    // ─────────────────────────────────────────────────
    public SpriteAnimator(String baseFolder, int frameDelayMs, int iconSizePx, boolean flipX) {
        this.baseFolder   = baseFolder;
        this.frameDelayMs = frameDelayMs;
        this.iconSizePx   = iconSizePx;
        this.flipX        = flipX;

        setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER);
        setPreferredSize(new Dimension(iconSizePx, iconSizePx));
        setMinimumSize(new Dimension(iconSizePx, iconSizePx));
        setMaximumSize(new Dimension(iconSizePx, iconSizePx));

        // Load idle and start the single timer
        loadAndSet("idle");
        startTimer();
    }

    public SpriteAnimator(String baseFolder, int frameDelayMs, int iconSizePx) {
        this(baseFolder, frameDelayMs, iconSizePx, false);
    }

    // ─────────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────────

    /**
     * Switch to a looping animation (e.g. "idle").
     * No-op if already playing that animation.
     */
    public void play(String animName) {
        if (stopped) return;
        if (animName.equals(currentAnim)) return;
        // Cancel any active one-shot
        onceCallback = null;
        onceTotal    = 0;
        onceTicked   = 0;
        loadAndSet(animName);
    }

    /**
     * Play an animation exactly once, then return to idle.
     * onComplete fires after the last frame.
     * The animation's cache entry is cleared when it finishes.
     */
    public void playOnce(String animName, Runnable onComplete) {
        playOnce(animName, onComplete, false);
    }
    
    /**
     * Play an animation exactly once with optional size scaling.
     * scaleUp = true makes the sprite 50% bigger (for skills)
     */
    public void playOnce(String animName, Runnable onComplete, boolean scaleUp) {
        if (stopped) { onComplete.run(); return; }

        // Store original size
        Dimension originalSize = getPreferredSize();
        
        // Scale up for skills
        if (scaleUp) {
            int newSize = (int)(iconSizePx * 1.5);
            setPreferredSize(new Dimension(newSize, newSize));
            setMinimumSize(new Dimension(newSize, newSize));
            setMaximumSize(new Dimension(newSize, newSize));
        }

        List<ImageIcon> frames = loadFrames(animName);
        if (frames.isEmpty()) { 
            // Restore size if scaled
            if (scaleUp) {
                setPreferredSize(originalSize);
                setMinimumSize(originalSize);
                setMaximumSize(originalSize);
            }
            onComplete.run(); 
            return; 
        }

        // Cancel any previous one-shot
        onceCallback = null;

        currentFrames = frames;
        currentAnim   = animName;
        frameIndex    = 0;
        setIcon(currentFrames.get(0));

        // Set up one-shot tracking — the main timer will count ticks
        onceTotal    = frames.size();
        onceTicked   = 0;
        onceCallback = () -> {
            // Restore original size if it was scaled
            if (scaleUp) {
                setPreferredSize(originalSize);
                setMinimumSize(originalSize);
                setMaximumSize(originalSize);
            }
            
            // Clear this animation from cache — it's done, free the memory
            clearCacheFor(baseFolder, animName, flipX);
            // Drop the frames reference so GC can collect them immediately
            currentFrames = new ArrayList<>();
            currentAnim = "";
            // Suggest garbage collection after animation completes
            System.gc();
            onComplete.run();
        };
    }

    /**
     * Stop all animation immediately.
     * Clears the current frames from memory and cancels the timer.
     * Call this when a character dies or a wave ends.
     */
    public void stop() {
        stopped = true;
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        // Drop frame references so GC can collect the ImageIcons immediately
        currentFrames.clear();
        currentFrames = null;
        onceCallback  = null;
        currentAnim   = "";
        
        // Clear this character's entire cache entry
        clearCacheFor(baseFolder, currentAnim, flipX);
        clearCacheFor(baseFolder, "idle",      flipX);
        clearCacheFor(baseFolder, "attack",    flipX);
        clearCacheFor(baseFolder, "skill1",    flipX);
        clearCacheFor(baseFolder, "skill2",    flipX);
        clearCacheFor(baseFolder, "skill3",    flipX);
        clearCacheFor(baseFolder, "death",     flipX);
        
        // Clear the icon to free the image
        setIcon(null);
    }

    // ── Single shared tombstone image — loaded once, used by everyone ──
    private static ImageIcon DEAD_ICON = null;
    private static boolean   DEAD_ICON_LOADED = false;

    private static ImageIcon getDeadIcon(int sizePx) {
        if (!DEAD_ICON_LOADED) {
            DEAD_ICON_LOADED = true;
            try {
                BufferedImage img = ImageIO.read(new File("assets/sprites/dead.png"));
                if (img != null) {
                    Image scaled = img.getScaledInstance(sizePx, sizePx, Image.SCALE_SMOOTH);
                    DEAD_ICON = new ImageIcon(scaled);
                }
            } catch (IOException ignored) {}
        }
        return DEAD_ICON;
    }

    /**
     * Called when a character or enemy dies.
     * 1. Kills the timer — no more ticking, ever.
     * 2. Wipes ALL cached frames for this character from the static map.
     * 3. Shows the shared dead.png tombstone as a static image.
     */
    public void showDead() {
        // Stop the timer immediately
        stopped = true;
        if (timer != null) {
            timer.stop();
            timer = null;
        }
        onceCallback  = null;
        
        // Clear current frames immediately
        if (currentFrames != null) {
            currentFrames.clear();
            currentFrames = null;
        }

        // Wipe every cache entry that belongs to this character/enemy
        List<String> toRemove = new ArrayList<>();
        for (String key : CACHE.keySet()) {
            if (key.startsWith(baseFolder)) toRemove.add(key);
        }
        toRemove.forEach(CACHE::remove);

        // Show the shared tombstone image
        ImageIcon deadIcon = getDeadIcon(iconSizePx);
        setIcon(deadIcon);   // null is fine too — slot just goes blank
        
        // Suggest garbage collection after death
        System.gc();
        repaint();
    }

    /**
     * Called when a character is revived.
     * 1. Restarts the animator
     * 2. Loads idle animation
     * 3. Starts the timer again
     */
    public void revive() {
        // Reset stopped flag
        stopped = false;
        
        // Clear the dead icon
        setIcon(null);
        
        // Reload idle animation
        loadAndSet("idle");
        
        // Restart the timer if it was stopped
        if (timer == null) {
            startTimer();
        }
        
        repaint();
    }

    // ─────────────────────────────────────────────────
    // STATIC CACHE MANAGEMENT
    // ─────────────────────────────────────────────────

    /** Remove one specific animation from the cache. */
    public static void clearCacheFor(String baseFolder, String animName, boolean flipX) {
        CACHE.remove(baseFolder + animName + SEP + flipX);
    }

    /** Wipe the entire cache — call between waves. */
    public static void clearAllCache() {
        CACHE.clear();
        // Also clear the dead icon to free memory
        DEAD_ICON = null;
        DEAD_ICON_LOADED = false;
        // Suggest garbage collection after clearing all cache
        System.gc();
    }

    // ─────────────────────────────────────────────────
    // INTERNAL
    // ─────────────────────────────────────────────────

    private void startTimer() {
        timer = new Timer(frameDelayMs, e -> tick());
        timer.start();
    }

    /**
     * Called every frameDelayMs by the single timer.
     * Handles both looping and one-shot animations.
     */
    private void tick() {
        if (stopped || currentFrames == null || currentFrames.isEmpty()) return;

        if (onceCallback != null) {
            // One-shot mode: advance frame, check if done
            onceTicked++;
            if (onceTicked >= onceTotal) {
                // Animation finished
                Runnable cb = onceCallback;
                onceCallback = null;
                onceTotal    = 0;
                onceTicked   = 0;
                // Return to idle before firing callback
                loadAndSet("idle");
                cb.run();
            } else {
                // Advance to next frame of the one-shot
                frameIndex = onceTicked % currentFrames.size();
                setIcon(currentFrames.get(frameIndex));
            }
        } else {
            // Looping mode
            frameIndex = (frameIndex + 1) % currentFrames.size();
            setIcon(currentFrames.get(frameIndex));
        }
    }

    /** Load frames for animName into currentFrames and reset index. */
    private void loadAndSet(String animName) {
        List<ImageIcon> frames = loadFrames(animName);
        if (frames.isEmpty()) return;
        currentFrames = frames;
        currentAnim   = animName;
        frameIndex    = 0;
        setIcon(currentFrames.get(0));
    }

    /** Load frames from disk (or cache). Returns empty list if folder missing. */
    private List<ImageIcon> loadFrames(String animName) {
        String key = baseFolder + animName + SEP + flipX;
        
        // Only use cache for idle animations - all others load fresh and clear after use
        if (animName.equals("idle") && CACHE.containsKey(key)) {
            return CACHE.get(key);
        }

        List<ImageIcon> frames = new ArrayList<>();
        
        // ═══════════════════════════════════════════════════════════
        // TRY SPRITESHEET FIRST (character_animation.png format)
        // ═══════════════════════════════════════════════════════════
        String characterName = extractCharacterName(baseFolder);
        
        // Special case: horse and witch use attack spritesheets
        String sheetName = characterName + "_" + animName + ".png";
        File spritesheetFile = new File(baseFolder + sheetName);
        
        if (spritesheetFile.exists()) {
            // Load spritesheet and extract frames
            frames = loadFromSpritesheet(spritesheetFile);
            if (!frames.isEmpty()) {
                // Cache idle animations
                if (animName.equals("idle")) {
                    CACHE.put(key, frames);
                }
                return frames;
            }
        }
        
        // ═══════════════════════════════════════════════════════════
        // FALLBACK: Try individual frame files (old system)
        // ═══════════════════════════════════════════════════════════
        File folder = new File(baseFolder + animName + "/");
        if (!folder.exists() || !folder.isDirectory()) return frames;

        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (files == null || files.length == 0) return frames;
        Arrays.sort(files);

        // ═══════════════════════════════════════════════════════════
        // FRAME SKIPPING: Load every Nth frame to reduce memory
        // ═══════════════════════════════════════════════════════════
        for (int i = 0; i < files.length; i += FRAME_SKIP) {
            File f = files[i];
            try {
                BufferedImage img = ImageIO.read(f);
                if (img == null) continue;

                int origW = img.getWidth(), origH = img.getHeight();
                int scaledW, scaledH;
                if (origW >= origH) {
                    scaledW = iconSizePx;
                    scaledH = (int)((double) origH / origW * iconSizePx);
                } else {
                    scaledH = iconSizePx;
                    scaledW = (int)((double) origW / origH * iconSizePx);
                }
                scaledW = Math.max(scaledW, 10);
                scaledH = Math.max(scaledH, 10);

                // OPTIMIZATION: Use SCALE_FAST for real-time performance
                // SCALE_SMOOTH is too slow for animations - SCALE_FAST is 3-5x faster
                Image scaled = img.getScaledInstance(scaledW, scaledH, Image.SCALE_FAST);

                if (flipX) {
                    // OPTIMIZATION: Use TYPE_INT_ARGB_PRE for faster alpha blending
                    BufferedImage flipped = new BufferedImage(scaledW, scaledH,
                        BufferedImage.TYPE_INT_ARGB_PRE);
                    java.awt.Graphics2D g2 = flipped.createGraphics();
                    
                    // Apply fast rendering hints
                    g2.setRenderingHints(FAST_HINTS);
                    
                    g2.drawImage(scaled, 0, 0, scaledW, scaledH, scaledW, 0, 0, scaledH, null);
                    g2.dispose();
                    frames.add(new ImageIcon(flipped));
                } else {
                    frames.add(new ImageIcon(scaled));
                }
                
                // OPTIMIZATION: Flush original image to free native memory immediately
                img.flush();
                
            } catch (IOException e) {
                System.out.println("Frame load error: " + f.getName());
            }
        }

        // Only cache idle animations - attack/skill animations are cleared after use
        if (animName.equals("idle")) {
            CACHE.put(key, frames);
        }
        return frames;
    }
    
    /**
     * Extract character name from baseFolder path.
     * E.g., "assets/sprites/arthur/" → "arthur"
     */
    private String extractCharacterName(String path) {
        String cleaned = path.replace("\\", "/");
        if (cleaned.endsWith("/")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        int lastSlash = cleaned.lastIndexOf('/');
        if (lastSlash >= 0) {
            return cleaned.substring(lastSlash + 1);
        }
        return cleaned;
    }
    
    /**
     * Load frames from a spritesheet (6×6 grid format).
     * Assumes 36 frames arranged in 6 columns × 6 rows.
     */
    private List<ImageIcon> loadFromSpritesheet(File spritesheetFile) {
        List<ImageIcon> frames = new ArrayList<>();
        
        try {
            BufferedImage sheet = ImageIO.read(spritesheetFile);
            if (sheet == null) return frames;
            
            // Calculate frame dimensions (assuming 6×6 grid)
            int cols = 6;
            int rows = 6;
            int frameWidth = sheet.getWidth() / cols;
            int frameHeight = sheet.getHeight() / rows;
            
            // Extract frames with FRAME_SKIP
            int totalFrames = cols * rows;
            for (int i = 0; i < totalFrames; i += FRAME_SKIP) {
                int row = i / cols;
                int col = i % cols;
                
                int x = col * frameWidth;
                int y = row * frameHeight;
                
                BufferedImage frameImg = sheet.getSubimage(x, y, frameWidth, frameHeight);
                
                // Scale to iconSizePx
                int origW = frameImg.getWidth(), origH = frameImg.getHeight();
                int scaledW, scaledH;
                if (origW >= origH) {
                    scaledW = iconSizePx;
                    scaledH = (int)((double) origH / origW * iconSizePx);
                } else {
                    scaledH = iconSizePx;
                    scaledW = (int)((double) origW / origH * iconSizePx);
                }
                scaledW = Math.max(scaledW, 10);
                scaledH = Math.max(scaledH, 10);
                
                Image scaled = frameImg.getScaledInstance(scaledW, scaledH, Image.SCALE_FAST);
                
                if (flipX) {
                    BufferedImage flipped = new BufferedImage(scaledW, scaledH,
                        BufferedImage.TYPE_INT_ARGB_PRE);
                    java.awt.Graphics2D g2 = flipped.createGraphics();
                    g2.setRenderingHints(FAST_HINTS);
                    g2.drawImage(scaled, 0, 0, scaledW, scaledH, scaledW, 0, 0, scaledH, null);
                    g2.dispose();
                    frames.add(new ImageIcon(flipped));
                } else {
                    frames.add(new ImageIcon(scaled));
                }
            }
            
            // Flush sheet to free memory
            sheet.flush();
            
        } catch (IOException e) {
            System.out.println("Spritesheet load error: " + spritesheetFile.getName());
        }
        
        return frames;
    }
}
