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
            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            clip = AudioSystem.getClip();
            clip.open(stream);
            
            if (loop) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Audio Error: " + e.getMessage());
        }
    }

    public static void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
    }
}
