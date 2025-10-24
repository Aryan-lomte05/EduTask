package com.edutask.ui;

import com.edutask.model.Task;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReflectionDialog extends JDialog {
    private Task task;
    private JTextArea reflectionArea;

    public ReflectionDialog(Frame parent, Task task) {
        super(parent, "Reflection - " + task.getTitle(), true);

        this.task = task;

        initializeUI();

        setSize(500, 400);
        setLocationRelativeTo(parent);
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        JLabel promptLabel = new JLabel("<html><h3>🌟 Take a moment to reflect!</h3>" +
                "What did you learn or accomplish with this task?</html>");
        topPanel.add(promptLabel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // Text area
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 15));

        reflectionArea = new JTextArea(10, 40);
        reflectionArea.setLineWrap(true);
        reflectionArea.setWrapStyleWord(true);
        reflectionArea.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(reflectionArea);

        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton saveBtn = new JButton("💾 Save Note");
        saveBtn.addActionListener(e -> saveReflection());

        JButton skipBtn = new JButton("Skip");
        skipBtn.addActionListener(e -> dispose());

        buttonPanel.add(saveBtn);
        buttonPanel.add(skipBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void saveReflection() {
        String reflection = reflectionArea.getText().trim();

        if (reflection.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please write something before saving!");
            return;
        }

        try {
            Path notesDir = Paths.get("data/notes");
            Files.createDirectories(notesDir);

            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String filename = String.format("%s_%s.txt", timestamp,
                    task.getTitle().replaceAll("[^a-zA-Z0-9]", "_"));

            Path notePath = notesDir.resolve(filename);

            String content = String.format("""
                Task: %s
                Date: %s
                
                Reflection:
                %s
                """, task.getTitle(), LocalDateTime.now(), reflection);

            Files.writeString(notePath, content);

            JOptionPane.showMessageDialog(this,
                    "Reflection saved successfully!\nFile: " + filename,
                    "Saved", JOptionPane.INFORMATION_MESSAGE);

            dispose();

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error saving reflection: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
