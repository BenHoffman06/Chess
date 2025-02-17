package ui;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;

public class SoundManager {
    public static void playCaptureSound() {
        playSound("sounds/Capture.wav");
    }

    public static void playMoveSound() {
        playSound("sounds/Move.wav");
    }

    private static void playSound(String filename) {
        new Thread(() -> {
            try (InputStream soundFile = SoundManager.class.getClassLoader().getResourceAsStream(filename)) {
                if (soundFile == null) return;

                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();
            } catch (Exception e) {
                System.err.println("Error playing sound: " + e.getMessage());
            }
        }).start();
    }
}