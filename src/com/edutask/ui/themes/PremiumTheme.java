package com.edutask.ui.themes;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class PremiumTheme {

    // Cork Board Background Colors
    public static final Color CORK_BACKGROUND = new Color(205, 170, 125);
    public static final Color CORK_DARK = new Color(180, 145, 100);
    public static final Color CORK_LIGHT = new Color(220, 185, 140);

    // Sticky Note Colors (pastel palette)
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

    // Apply cork board background to component
    public static void applyCorkBackground(JComponent component) {
        component.setBackground(CORK_BACKGROUND);
        component.setOpaque(true);
    }

    // Get sticky note color by priority
    public static Color getStickyColorByPriority(int priority) {
        return switch (priority) {
            case 5 -> STICKY_PINK;      // Highest priority - pink (urgent)
            case 4 -> STICKY_ORANGE;    // High - orange
            case 3 -> STICKY_YELLOW;    // Medium - yellow (classic)
            case 2 -> STICKY_GREEN;     // Low - green
            default -> STICKY_BLUE;     // Lowest - blue
        };
    }

    // Get category color
    public static Color getCategoryColor(String category) {
        if (category.equalsIgnoreCase("STUDY")) {
            return STICKY_BLUE;
        } else {
            return STICKY_GREEN;
        }
    }

    // Create textured cork panel
    public static JPanel createCorkPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw cork texture with random spots
                Random random = new Random(42); // Fixed seed for consistency
                for (int i = 0; i < 200; i++) {
                    int x = random.nextInt(getWidth());
                    int y = random.nextInt(getHeight());
                    int size = random.nextInt(3) + 1;

                    g2.setColor(random.nextBoolean() ? CORK_DARK : CORK_LIGHT);
                    g2.fillOval(x, y, size, size);
                }
            }
        };
        panel.setBackground(CORK_BACKGROUND);
        return panel;
    }

    // Apply premium button style
    public static void styleButton(JButton button) {
        button.setFont(FONT_BODY);
        button.setBackground(new Color(230, 230, 230));
        button.setForeground(TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(200, 200, 200));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(230, 230, 230));
            }
        });
    }
}
