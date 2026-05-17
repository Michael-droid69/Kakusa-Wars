package ui;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.Timer;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.awt.Dimension;

import javax.imageio.ImageIO;

public class SpriteAnimator extends JLabel {

    private final String baseFolder;
    private List<ImageIcon> currentFrames = new ArrayList<>();
    private String currentAnim = "";
    private int frameIndex = 0;
    private final int frameDelayMs;
    private final int iconSizePx;
    private Timer timer;
    private final boolean flipX;
    private static final java.util.Map<String, List<ImageIcon>> CACHE = new java.util.HashMap<>();
    private static final String CACHE_KEY_SEP = "_";

    public SpriteAnimator(String baseFolder, int frameDelayMs, int iconSizePx, boolean flipX) {
        this.baseFolder = baseFolder;
        this.frameDelayMs = frameDelayMs;
        this.iconSizePx = iconSizePx;
        this.flipX = flipX;
        

        setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER);
        setPreferredSize(new Dimension(iconSizePx, iconSizePx));
        setMinimumSize(new Dimension(iconSizePx, iconSizePx));
        setMaximumSize(new Dimension(iconSizePx, iconSizePx));

        play("idle"); // start on idle by default

        timer = new Timer(frameDelayMs, e -> nextFrame());
        timer.start();
    }
    public SpriteAnimator(String baseFolder, int frameDelayMs, int iconSizePx) {
    this(baseFolder, frameDelayMs, iconSizePx, false);  // ← default: no flip
}

    // Call this to switch animation
    // animName must match the subfolder name exactly: "idle", "attack", "skill1", "skill2", "skill3", "death"
    public void play(String animName) {
        if (animName.equals(currentAnim)) return;
        List<ImageIcon> frames = loadFrames(animName);
        if (frames.isEmpty()) return;

        currentFrames = frames;
        currentAnim = animName;
        frameIndex = 0;
        setIcon(currentFrames.get(0));
    }

    // Play an animation once, then go back to idle automatically.
    // onComplete runs after the last frame.
    // Cache for the one-shot animation is cleared after it finishes
    // so transient animations (attack, skill1-3, death) don't sit in memory.
    public void playOnce(String animName, Runnable onComplete) {
        List<ImageIcon> frames = loadFrames(animName);
        if (frames.isEmpty()) {
            onComplete.run();
            return;
        }

        currentFrames = frames;
        currentAnim = animName;
        frameIndex = 0;
        setIcon(currentFrames.get(0));

        int totalTimeMs = frames.size() * frameDelayMs;
        Timer once = new Timer(totalTimeMs, e -> {
            // Clear this one-shot animation from cache — it won't be needed
            // again until the next time it's explicitly triggered, so there's
            // no point keeping all those ImageIcon frames in memory.
            clearCacheFor(baseFolder, animName, flipX);
            play("idle");
            onComplete.run();
        });
        once.setRepeats(false);
        once.start();
    }

    private void nextFrame() {
        if (currentFrames.isEmpty()) return;
        frameIndex = (frameIndex + 1) % currentFrames.size();
        setIcon(currentFrames.get(frameIndex));
    }

    // Reads all PNGs from assets/sprites/arthur/idle/ sorted by name
    private List<ImageIcon> loadFrames(String animName) {
    String cacheKey = baseFolder + animName + CACHE_KEY_SEP + flipX;
    
    // Check if we already loaded this animation before
    if (CACHE.containsKey(cacheKey)) {
        return CACHE.get(cacheKey);
    }

    List<ImageIcon> frames = new ArrayList<>();
    File folder = new File(baseFolder + animName + "/");
    if (!folder.exists() || !folder.isDirectory()) return frames;

    File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
    if (files == null) return frames;
    Arrays.sort(files);

    for (File f : files) {
        try {
            BufferedImage img = ImageIO.read(f);
            if (img == null) continue;

            int origW = img.getWidth();
            int origH = img.getHeight();
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
                BufferedImage flipped = new BufferedImage(scaledW, scaledH, BufferedImage.TYPE_INT_ARGB);
                java.awt.Graphics2D g2 = flipped.createGraphics();
                // FIX: Corrected your flipping coordinates here
                g2.drawImage(scaled, 0, 0, scaledW, scaledH, scaledW, 0, 0, scaledH, null);
                g2.dispose();
                frames.add(new ImageIcon(flipped));
            } else {
                frames.add(new ImageIcon(scaled));
            }

        } catch (IOException e) {
            System.out.println("Error loading: " + f.getName());
        }
    }
    
    // Save to cache so we never have to read these files again!
    CACHE.put(cacheKey, frames);
    return frames;
}

    public void stop() {
        if (timer != null) timer.stop();
    }

    // Clears loaded frame cache for a specific animator so it can't accumulate between skill animations.
    // This is intentionally narrow: only clears the (baseFolder + animName + flipX) entry.
    public static void clearCacheFor(String baseFolder, String animName, boolean flipX) {
        String cacheKey = baseFolder + animName + CACHE_KEY_SEP + flipX;
        CACHE.remove(cacheKey);
    }

    // Wipes the entire frame cache. Call this between waves so sprites
    // from the previous wave don't sit in memory during the next one.
    public static void clearAllCache() {
        CACHE.clear();
    }
}
