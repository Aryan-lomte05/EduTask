package com.edutask.ui;

import com.edutask.model.*;
import com.edutask.service.TaskService;
import com.edutask.ui.themes.PremiumTheme;
import com.edutask.audio.SoundManager;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class TaskDetailsDialog extends JDialog {
    private Task task;
    private TaskService taskService;
    private MainFrame parent;

    public TaskDetailsDialog(MainFrame parent, Task task, TaskService taskService) {
        super(parent, "Task Details", true);
        this.parent = parent;
        this.task = task;
        this.taskService = taskService;

        initializeUI();
        setSize(500, 600);
        setLocationRelativeTo(parent);
    }

    private void initializeUI() {
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(Color.WHITE);
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createContentPanel(), BorderLayout.CENTER);
        add(createActionPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("📋 " + task.getTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(PremiumTheme.TEXT_PRIMARY);

        JLabel statusLabel = new JLabel(getStatusBadge());
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(getStatusColor());
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        panel.add(titleLabel, BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        if (task instanceof StudyTask st) {
            panel.add(createInfoRow("📚 Subject:", st.getSubject()));
            panel.add(Box.createVerticalStrut(10));
            panel.add(createInfoRow("🎯 Topic:", st.getTopic()));
        } else if (task instanceof PersonalTask pt) {
            panel.add(createInfoRow("🏷️ Tag:", pt.getTag()));
        }

        panel.add(Box.createVerticalStrut(10));
        panel.add(createInfoRow("📂 Category:", task.getCategory().getDisplay()));
        panel.add(Box.createVerticalStrut(10));

        // Fixed due date formatting
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        String dueInfo = task.getDueDate().format(formatter);
        panel.add(createInfoRow("📅 Due Date:", dueInfo));
        panel.add(Box.createVerticalStrut(10));

        String priorityStars = "⭐".repeat(task.getPriority());
        panel.add(createInfoRow("⭐ Priority:", priorityStars + " (" + task.getPriority() + "/5)"));
        panel.add(Box.createVerticalStrut(15));

        JLabel detailsLabel = new JLabel("Details:");
        detailsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        detailsLabel.setForeground(PremiumTheme.TEXT_SECONDARY);
        detailsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(detailsLabel);
        panel.add(Box.createVerticalStrut(5));

        JTextArea detailsArea = new JTextArea(task.getDetails());
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setFont(new Font("Arial", Font.PLAIN, 13));
        detailsArea.setBackground(new Color(245, 245, 245));
        detailsArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JScrollPane scrollPane = new JScrollPane(detailsArea);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(scrollPane);

        return panel;
    }

    private JPanel createInfoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Arial", Font.BOLD, 13));
        labelComp.setForeground(PremiumTheme.TEXT_SECONDARY);

        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Arial", Font.PLAIN, 13));
        valueComp.setForeground(PremiumTheme.TEXT_PRIMARY);

        row.add(labelComp, BorderLayout.WEST);
        row.add(valueComp, BorderLayout.CENTER);

        return row;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setOpaque(false);

        JButton editBtn = new JButton("✏️ Edit");
        JButton deleteBtn = new JButton("🗑️ Delete");
        JButton closeBtn = new JButton("Close");

        PremiumTheme.styleButton(editBtn);
        PremiumTheme.styleButton(deleteBtn);
        PremiumTheme.styleButton(closeBtn);

        editBtn.addActionListener(e -> {
            parent.getTaskFormPanel().loadTask(task);
            dispose();
        });

        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete task: " + task.getTitle() + "?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    taskService.deleteTask(task.getId());
                    SoundManager.getInstance().playDelete();
                    parent.refreshAll();
                    dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });

        closeBtn.addActionListener(e -> dispose());

        if (task instanceof StudyTask st && task.getStatus() == Status.COMPLETED) {
            JButton quizBtn = new JButton("🎓 Take Quiz");
            PremiumTheme.styleButton(quizBtn);
            quizBtn.addActionListener(e -> {
                QuizDialog quiz = new QuizDialog(parent, st.getSubject(), st.getTopic(), 15);
                quiz.setVisible(true);
            });
            panel.add(quizBtn);
        }

        panel.add(editBtn);
        panel.add(deleteBtn);
        panel.add(closeBtn);

        return panel;
    }

    private String getStatusBadge() {
        return switch (task.getStatus()) {
            case COMPLETED -> "[DONE]";
            case IN_PROGRESS -> "[IN PROGRESS]";
            default -> "[TO DO]";
        };
    }

    private Color getStatusColor() {
        return switch (task.getStatus()) {
            case COMPLETED -> new Color(50, 180, 50);
            case IN_PROGRESS -> new Color(255, 150, 0);
            default -> new Color(150, 150, 150);
        };
    }
}
