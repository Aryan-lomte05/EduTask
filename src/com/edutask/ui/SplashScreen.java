package com.edutask.ui;

import javax.swing.*;
import java.awt.*;

public class SplashScreen extends JWindow {
    private JProgressBar progressBar;
    private JLabel statusLabel;

    public SplashScreen() {
        setSize(500, 300);
        setLocationRelativeTo(null);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(41, 128, 185));
        content.setBorder(BorderFactory.createLineBorder(new Color(52, 73, 94), 3));

        // Logo/Title
        JPanel logoPanel = new JPanel(new GridBagLayout());
        logoPanel.setBackground(new Color(41, 128, 185));

        JLabel titleLabel = new JLabel("EduTask Manager");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Your Smart Task Organizer");
        subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        subtitleLabel.setForeground(new Color(236, 240, 241));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 0, 5, 0);
        logoPanel.add(titleLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 10, 0);
        logoPanel.add(subtitleLabel, gbc);

        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(46, 204, 113));
        progressBar.setBackground(new Color(52, 73, 94));
        progressBar.setPreferredSize(new Dimension(400, 25));

        // Status label
        statusLabel = new JLabel("Initializing...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(Color.WHITE);

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBackground(new Color(41, 128, 185));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 30, 50));
        bottomPanel.add(progressBar, BorderLayout.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);

        content.add(logoPanel, BorderLayout.CENTER);
        content.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(content);
        setVisible(true);
    }

    public void setProgress(int value, String status) {
        progressBar.setValue(value);
        statusLabel.setText(status);
    }

    public void close() {
        setVisible(false);
        dispose();
    }

    public static void showSplash(Runnable onComplete) {
        SplashScreen splash = new SplashScreen();

        new Thread(() -> {
            try {
                splash.setProgress(10, "Loading resources...");
                Thread.sleep(300);

                splash.setProgress(30, "Initializing database...");
                Thread.sleep(300);

                splash.setProgress(50, "Loading UI components...");
                Thread.sleep(300);

                splash.setProgress(70, "Setting up services...");
                Thread.sleep(300);

                splash.setProgress(90, "Finalizing...");
                Thread.sleep(200);

                splash.setProgress(100, "Ready!");
                Thread.sleep(200);

                splash.close();
                SwingUtilities.invokeLater(onComplete);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
