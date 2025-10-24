package com.edutask.audio;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;

public class SoundManager {
    private static SoundManager instance;
    private boolean soundEnabled = true;

    private SoundManager() {
        // Singleton
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    public void playClick() {
        playTone(800, 30);
    }

    public void playSuccess() {
        new Thread(() -> {
            playTone(600, 80);
            sleep(50);
            playTone(800, 80);
            sleep(50);
            playTone(1000, 120);
        }).start();
    }

    public void playError() {
        new Thread(() -> {
            playTone(400, 100);
            sleep(80);
            playTone(350, 120);
        }).start();
    }

    public void playAdd() {
        new Thread(() -> {
            playTone(700, 60);
            sleep(40);
            playTone(900, 80);
        }).start();
    }

    public void playDelete() {
        new Thread(() -> {
            playTone(600, 60);
            sleep(40);
            playTone(400, 100);
        }).start();
    }

    public void playComplete() {
        new Thread(() -> {
            playTone(800, 70);
            sleep(50);
            playTone(1000, 70);
            sleep(50);
            playTone(1200, 150);
        }).start();
    }

    private void playTone(int frequency, int duration) {
        if (!soundEnabled) return;

        try {
            int sampleRate = 22050;
            int numSamples = duration * sampleRate / 1000;
            byte[] buffer = new byte[numSamples * 2];

            for (int i = 0; i < numSamples; i++) {
                double angle = 2.0 * Math.PI * i * frequency / sampleRate;
                short sample = (short) (Math.sin(angle) * 32767 * 0.3); // 30% volume

                buffer[i * 2] = (byte) (sample & 0xFF);
                buffer[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
            }

            AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
            ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            AudioInputStream ais = new AudioInputStream(bais, format, numSamples);

            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clip.start();

            // Auto-close after playing
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });

        } catch (Exception e) {
            // Silently fail if sound doesn't work
        }
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }
}
