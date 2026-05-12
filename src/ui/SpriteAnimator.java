package ui;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.Timer;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Color;

import javax.imageio.ImageIO;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpriteAnimator extends JLabel {

    private final String baseFolder;
    private List<ImageIcon> currentFrames = new ArrayList<>();
    private String currentAnim = "";
    private int frameIndex = 0;
    private Timer timer;

    // baseFolder example: "assets/sprites/arthur/"
    // frameDelay: how fast frames switch in milliseconds, 100 is good
    public SpriteAnimator(String baseFolder, int frameDelay) {
        this.baseFolder = baseFolder;
        setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER);
        play("idle"); // start on idle by default

        timer = new Timer(frameDelay, e -> nextFrame());
        timer.start();
    }

    // Call this to switch animation
    // animName must match the subfolder name exactly: "idle", "attack", "skill1", "death"
    public void play(String animName) {
        if (animName.equals(currentAnim)) return; // already playing it
        List<ImageIcon> frames = loadFrames(animName);
        if (frames.isEmpty()) return; // folder not found or empty, do nothing
        currentFrames = frames;
        currentAnim = animName;
        frameIndex = 0;
        setIcon(currentFrames.get(0));
    }

    // Play an animation once, then go back to idle automatically
    // onComplete runs after the last frame — use this to trigger damage calculation
    public void playOnce(String animName, Runnable onComplete) {
        List<ImageIcon> frames = loadFrames(animName);
        if (frames.isEmpty()) { onComplete.run(); return; }

        currentFrames = frames;
        currentAnim = animName;
        frameIndex = 0;
        setIcon(currentFrames.get(0));

        // total time = number of frames × frame delay (we use 100ms per frame here)
        int totalTime = frames.size() * 100;
        Timer once = new Timer(totalTime, e -> {
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

        Arrays.sort(files); // sorts by filename so 00 comes before 01

        for (File f : files) {
            try {
                BufferedImage img = ImageIO.read(f);
                // Change 200, 200 to whatever size fits your battle screen
                Image scaled = img.getScaledInstance(200, 200, Image.SCALE_SMOOTH);
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
