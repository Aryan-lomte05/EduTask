package com.edutask.ui.themes;

import com.edutask.audio.SoundManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

public class PremiumTheme {

    // Cork Board Background Colors
    public static final Color CORK_BACKGROUND = new Color(205, 170, 125);
    public static final Color CORK_DARK = new Color(170, 135, 90);
    public static final Color CORK_LIGHT = new Color(225, 195, 150);

    // Sticky Note Colors
    public static final Color STICKY_YELLOW = new Color(255, 253, 150);
    public static final Color STICKY_PINK = new Color(255, 190, 200);
    public static final Color STICKY_BLUE = new Color(173, 216, 255);
    public static final Color STICKY_GREEN = new Color(194, 255, 194);
    public static final Color STICKY_ORANGE = new Color(255, 214, 165);
    public static final Color STICKY_PURPLE = new Color(220, 190, 255);

    // Priority Colors
    public static final Color PRIORITY_LOW = new Color(100, 200, 100);
    public static final Color PRIORITY_MEDIUM = new Color(255, 200, 80);
    public static final Color PRIORITY_HIGH = new Color(255, 100, 100);

    // Text Colors
    public static final Color TEXT_PRIMARY = new Color(40, 40, 40);
    public static final Color TEXT_SECONDARY = new Color(80, 80, 80);
    public static final Color TEXT_LIGHT = new Color(255, 255, 255);

    // Shadow & Effects
    public static final Color SHADOW = new Color(0, 0, 0, 30);
    public static final Color PIN_METAL = new Color(192, 192, 192);
    public static final Color PIN_HEAD = new Color(220, 50, 50);

    // Fonts
    public static final Font FONT_TITLE = new Font("Comic Sans MS", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("Arial", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Arial", Font.PLAIN, 11);
    public static final Font FONT_HANDWRITING = new Font("Segoe Print", Font.PLAIN, 14);

    // Get sticky note color by priority
    public static Color getStickyColorByPriority(int priority) {
        return switch (priority) {
            case 5 -> STICKY_PINK;
            case 4 -> STICKY_ORANGE;
            case 3 -> STICKY_YELLOW;
            case 2 -> STICKY_GREEN;
            default -> STICKY_BLUE;
        };
    }

    // Create textured cork panel with PREMIUM TEXTURE
    public static JPanel createCorkPanel() {
        JPanel panel = new JPanel() {
            private Image corkTexture = null;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Multi-layer realistic cork texture
                Random random = new Random(42); // Fixed seed for consistency

                // Layer 1: Large dark spots (cork pores)
                g2.setColor(CORK_DARK);
                for (int i = 0; i < 400; i++) {
                    int x = random.nextInt(Math.max(1, getWidth()));
                    int y = random.nextInt(Math.max(1, getHeight()));
                    int size = random.nextInt(5) + 2;
                    g2.fillOval(x, y, size, size);
                }

                // Layer 2: Medium spots
                g2.setColor(new Color(190, 155, 110));
                for (int i = 0; i < 300; i++) {
                    int x = random.nextInt(Math.max(1, getWidth()));
                    int y = random.nextInt(Math.max(1, getHeight()));
                    int size = random.nextInt(3) + 1;
                    g2.fillOval(x, y, size, size);
                }

                // Layer 3: Light highlights
                g2.setColor(CORK_LIGHT);
                for (int i = 0; i < 200; i++) {
                    int x = random.nextInt(Math.max(1, getWidth()));
                    int y = random.nextInt(Math.max(1, getHeight()));
                    int size = random.nextInt(2) + 1;
                    g2.fillOval(x, y, size, size);
                }

                // Layer 4: Wood grain lines
                g2.setColor(new Color(180, 145, 100, 40));
                g2.setStroke(new BasicStroke(1.5f));
                for (int i = 0; i < 60; i++) {
                    int y = random.nextInt(Math.max(1, getHeight()));
                    int length = random.nextInt(120) + 60;
                    int x = random.nextInt(Math.max(1, getWidth() - length));
                    g2.drawLine(x, y, x + length, y);
                }

                // Layer 5: Subtle circular patterns
                g2.setColor(new Color(200, 165, 120, 20));
                for (int i = 0; i < 30; i++) {
                    int x = random.nextInt(Math.max(1, getWidth()));
                    int y = random.nextInt(Math.max(1, getHeight()));
                    int size = random.nextInt(40) + 20;
                    g2.drawOval(x, y, size, size);
                }

                g2.dispose();
            }
        };
        panel.setBackground(CORK_BACKGROUND);
        return panel;
    }

    // Style button with SOUND EFFECTS
    public static void styleButton(JButton button) {
        button.setFont(FONT_BODY);
        button.setBackground(new Color(240, 240, 240));
        button.setForeground(TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Add sound effect on click
        button.addActionListener(e -> SoundManager.getInstance().playClick());

        // Smooth hover animation
        button.addMouseListener(new MouseAdapter() {
            private Timer timer;
            private float brightness = 1.0f;

            @Override
            public void mouseEntered(MouseEvent e) {
                if (timer != null) timer.stop();
                timer = new Timer(20, evt -> {
                    brightness = Math.max(0.85f, brightness - 0.05f);
                    button.setBackground(adjustBrightness(new Color(240, 240, 240), brightness));
                    if (brightness <= 0.85f) {
                        timer.stop();
                    }
                });
                timer.start();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (timer != null) timer.stop();
                timer = new Timer(20, evt -> {
                    brightness = Math.min(1.0f, brightness + 0.05f);
                    button.setBackground(adjustBrightness(new Color(240, 240, 240), brightness));
                    if (brightness >= 1.0f) {
                        timer.stop();
                    }
                });
                timer.start();
            }
        });
    }

    private static Color adjustBrightness(Color color, float factor) {
        int r = Math.min(255, (int) (color.getRed() * factor));
        int g = Math.min(255, (int) (color.getGreen() * factor));
        int b = Math.min(255, (int) (color.getBlue() * factor));
        return new Color(r, g, b);
    }

    // Apply cork background
    public static void applyCorkBackground(JComponent component) {
        component.setBackground(CORK_BACKGROUND);
        component.setOpaque(true);
    }
}
