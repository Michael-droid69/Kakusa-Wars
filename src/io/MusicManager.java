package io;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class MusicManager {
    private static Clip clip;

    public static void play(String fileName, boolean loop) {
        // Stop current music if it's already playing to avoid overlapping
        stop();

        try {
            File file = new File("assets/audio/" + fileName);
            // Silently skip if the file doesn't exist — audio is non-critical
            if (!file.exists()) return;

            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            clip = AudioSystem.getClip();
            clip.open(stream);
            
            if (loop) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            // Audio failure is non-fatal — game continues without music
        }
    }

    public static void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
    }
}
