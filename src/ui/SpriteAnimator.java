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

/**
 * SpriteAnimator — plays frame-by-frame PNG animations from a folder.
 *
 * Design rules:
 *  • ONE timer only. It drives every frame tick. No secondary timers.
 *  • playOnce() counts frames internally — no separate one-shot Timer.
 *  • stop() halts the timer AND clears currentFrames to free memory.
 *  • Every one-shot animation (attack, skill, death) clears its cache
 *    entry the moment it finishes — frames are never kept after use.
 *  • clearAllCache() wipes everything between waves.
 */
public class SpriteAnimator extends JLabel {

    private final String  baseFolder;
    private final int     frameDelayMs;
    private final int     iconSizePx;
    private final boolean flipX;

    // The single timer that drives all frame ticks
    private Timer timer;

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
        if (stopped) { onComplete.run(); return; }

        List<ImageIcon> frames = loadFrames(animName);
        if (frames.isEmpty()) { onComplete.run(); return; }

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
        File folder = new File(baseFolder + animName + "/");
        if (!folder.exists() || !folder.isDirectory()) return frames;

        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (files == null || files.length == 0) return frames;
        Arrays.sort(files);

        for (File f : files) {
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

                Image scaled = img.getScaledInstance(scaledW, scaledH, Image.SCALE_SMOOTH);

                if (flipX) {
                    BufferedImage flipped = new BufferedImage(scaledW, scaledH,
                        BufferedImage.TYPE_INT_ARGB);
                    java.awt.Graphics2D g2 = flipped.createGraphics();
                    g2.drawImage(scaled, 0, 0, scaledW, scaledH, scaledW, 0, 0, scaledH, null);
                    g2.dispose();
                    frames.add(new ImageIcon(flipped));
                } else {
                    frames.add(new ImageIcon(scaled));
                }
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
}
