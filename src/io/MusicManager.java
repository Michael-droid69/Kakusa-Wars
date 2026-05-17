package io;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class MusicManager {
    private static Clip clip;

    public static void play(String fileName, boolean loop) {
        // Stop current music if already playing
        stop();

        // Run on a background thread so audio init doesn't block the UI
        Thread t = new Thread(() -> {
            try {
                File file = new File("assets/audio/" + fileName);
                if (!file.exists()) return;

                AudioInputStream rawStream = AudioSystem.getAudioInputStream(file);

                // Convert to a format Java's Clip can always handle:
                // PCM_SIGNED, 44100 Hz, 16-bit, stereo, little-endian
                AudioFormat baseFormat   = rawStream.getFormat();
                AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate() > 0 ? baseFormat.getSampleRate() : 44100f,
                    16,
                    baseFormat.getChannels() > 0 ? baseFormat.getChannels() : 2,
                    baseFormat.getChannels() > 0 ? baseFormat.getChannels() * 2 : 4,
                    baseFormat.getSampleRate() > 0 ? baseFormat.getSampleRate() : 44100f,
                    false
                );

                AudioInputStream stream = AudioSystem.getAudioInputStream(targetFormat, rawStream);

                clip = AudioSystem.getClip();
                clip.open(stream);

                if (loop) clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();

            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                System.err.println("Audio Error: " + fileName + " — " + e.getMessage());
            }
        });
        t.setDaemon(true); // don't block JVM shutdown
        t.start();
    }

    public static void stop() {
        if (clip != null) {
            if (clip.isRunning()) clip.stop();
            clip.close();
            clip = null;
        }
    }
}
