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

            // ── Scale to iconSizePx on the LONGEST side, preserve aspect ratio ──
            // This prevents skill frames from squishing the character when the
            // canvas is wider than it is tall (e.g. a wide attack swing frame)
            int origW = img.getWidth();
            int origH = img.getHeight();

            int scaledW, scaledH;
            if (origW >= origH) {
                // Wider frame (e.g. attack swing) — fit width, scale height proportionally
                scaledW = iconSizePx;
                scaledH = (int)((double) origH / origW * iconSizePx);
            } else {
                // Taller frame (e.g. idle standing) — fit height, scale width proportionally
                scaledH = iconSizePx;
                scaledW = (int)((double) origW / origH * iconSizePx);
            }

            // Minimum size guard — never go below 10px
            scaledW = Math.max(scaledW, 10);
            scaledH = Math.max(scaledH, 10);

            Image scaled = img.getScaledInstance(scaledW, scaledH, Image.SCALE_SMOOTH);

if (flipX) {
    // Draw the scaled image onto a new canvas, mirrored left-right
    BufferedImage flipped = new BufferedImage(scaledW, scaledH,
                                              BufferedImage.TYPE_INT_ARGB);
    java.awt.Graphics2D g2 = flipped.createGraphics();
    g2.drawImage(scaled,
        scaledW, 0,    // destination top-LEFT = right edge (start drawing from right)
        0,       scaledH,  // destination bottom-RIGHT = left edge (end at left)
        null);
    g2.dispose();
    frames.add(new ImageIcon(flipped));
} else {
    frames.add(new ImageIcon(scaled));
}
            frames.add(new ImageIcon(scaled));

        } catch (IOException e) {
            System.out.println("Could not load sprite: " + f.getName());
        }
    }
    return frames;
}

    public void stop() {
        if (timer != null) timer.stop();
    }
}
