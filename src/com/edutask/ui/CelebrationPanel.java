package com.edutask.ui;

import com.edutask.model.Task;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class CelebrationPanel extends JPanel {
    private Task task;
    private List<Particle> particles = new ArrayList<>();
    private javax.swing.Timer animationTimer;  // ← SPECIFY javax.swing.Timer
    private String[] quotes = {
            "Great job! Keep it up! 🎉",
            "You're on fire! 🔥",
            "Awesome work! ⭐",
            "One step closer to your goals! 🎯",
            "Excellence in action! 💪"
    };

    public CelebrationPanel(Task task) {
        this.task = task;

        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 250));

        // Create particles
        Random rand = new Random();
        for (int i = 0; i < 50; i++) {
            particles.add(new Particle(
                    rand.nextInt(400),
                    rand.nextInt(300),
                    new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256))
            ));
        }

        // Add message
        JPanel messagePanel = new JPanel();
        messagePanel.setOpaque(false);
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));

        String quote = quotes[rand.nextInt(quotes.length)];

        JLabel titleLabel = new JLabel("Task Completed!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel taskLabel = new JLabel(task.getTitle());
        taskLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        taskLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel quoteLabel = new JLabel(quote);
        quoteLabel.setFont(new Font("Arial", Font.ITALIC, 18));
        quoteLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        messagePanel.add(Box.createVerticalGlue());
        messagePanel.add(titleLabel);
        messagePanel.add(Box.createVerticalStrut(15));
        messagePanel.add(taskLabel);
        messagePanel.add(Box.createVerticalStrut(15));
        messagePanel.add(quoteLabel);
        messagePanel.add(Box.createVerticalGlue());

        add(messagePanel, BorderLayout.CENTER);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> SwingUtilities.getWindowAncestor(this).dispose());

        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.add(closeBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // Start com.edutask.animation
        animationTimer = new javax.swing.Timer(30, e -> {  // ← SPECIFY javax.swing.Timer
            for (Particle p : particles) {
                p.update();
            }
            repaint();
        });
        animationTimer.start();

        // Stop after 5 seconds
        javax.swing.Timer stopTimer = new javax.swing.Timer(5000, e -> animationTimer.stop());  // ← SPECIFY
        stopTimer.setRepeats(false);
        stopTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Particle p : particles) {
            p.draw(g2);
        }
    }

    private static class Particle {
        int x, y;
        double vx, vy;
        Color color;
        int size;

        public Particle(int x, int y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.size = 5 + new Random().nextInt(5);

            Random rand = new Random();
            this.vx = (rand.nextDouble() - 0.5) * 4;
            this.vy = -rand.nextDouble() * 5;
        }

        public void update() {
            x += vx;
            y += vy;
            vy += 0.2; // Gravity

            if (y > 300) {
                vy = -vy * 0.6; // Bounce
            }
        }

        public void draw(Graphics2D g) {
            g.setColor(color);
            g.fillOval(x, y, size, size);
        }
    }
}
