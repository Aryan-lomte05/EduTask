package com.edutask.ui;

import com.edutask.model.*;
import com.edutask.service.TaskService;
import com.edutask.ui.themes.PremiumTheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class TaskDetailsDialog extends JDialog {
    private final Task task;
    private final TaskService taskService;
    private final MainFrame mainFrame;

    public TaskDetailsDialog(MainFrame mainFrame, Task task, TaskService taskService) {
        super(mainFrame, "Task Details", true);
        this.mainFrame = mainFrame;
        this.task = task;
        this.taskService = taskService;

        setLayout(new BorderLayout(0, 0));
        setSize(550, 750);  // Taller dialog
        setLocationRelativeTo(mainFrame);
        setResizable(true);

        JScrollPane bodyScroll = new JScrollPane(createBody());
        bodyScroll.setBorder(null);
        bodyScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(createHeader(), BorderLayout.NORTH);
        add(bodyScroll, BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        getContentPane().setBackground(Color.WHITE);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(PremiumTheme.getStickyColorByPriority(task.getPriority()));
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 3, 0, PremiumTheme.CORK_DARK),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(task.getTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(PremiumTheme.TEXT_PRIMARY);

        String stars = "*".repeat(task.getPriority()) + ".".repeat(5 - task.getPriority());
        JLabel priorityLabel = new JLabel(stars);
        priorityLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        priorityLabel.setForeground(new Color(255, 193, 7));

        header.add(titleLabel, BorderLayout.CENTER);
        header.add(priorityLabel, BorderLayout.EAST);

        return header;
    }

    private JPanel createBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(20, 25, 20, 25));

        body.add(createInfoRow("Category", task.getCategory().getDisplay(),
                new Color(52, 152, 219), Color.WHITE));
        body.add(Box.createVerticalStrut(12));

        Color statusColor = switch (task.getStatus()) {
            case TODO -> new Color(149, 165, 166);
            case IN_PROGRESS -> new Color(241, 196, 15);
            case COMPLETED -> new Color(46, 204, 113);
        };
        body.add(createInfoRow("Status", task.getStatus().getDisplay(), statusColor, Color.WHITE));
        body.add(Box.createVerticalStrut(12));

        if (task instanceof StudyTask st) {
            body.add(createInfoRow("Subject", st.getSubject(),
                    new Color(155, 89, 182), Color.WHITE));
            body.add(Box.createVerticalStrut(12));
            body.add(createInfoRow("Topic", st.getTopic(),
                    new Color(142, 68, 173), Color.WHITE));
            body.add(Box.createVerticalStrut(12));
        } else if (task instanceof PersonalTask pt) {
            body.add(createInfoRow("Tag", pt.getTag(),
                    new Color(230, 126, 34), Color.WHITE));
            body.add(Box.createVerticalStrut(12));
        }

        String dueDateStr = task.getDueDate().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"));

        String dueTimeStr = "12:00 PM";
        try {
            if (task.getDueTime() != null) {
                dueTimeStr = task.getDueTime().format(DateTimeFormatter.ofPattern("hh:mm a"));
            }
        } catch (Exception ignored) {}

        body.add(createInfoRow("Due Date", dueDateStr,
                new Color(231, 76, 60), Color.WHITE));
        body.add(Box.createVerticalStrut(12));
        body.add(createInfoRow("Due Time", dueTimeStr,
                new Color(192, 57, 43), Color.WHITE));
        body.add(Box.createVerticalStrut(12));

        if (task.getDetails() != null && !task.getDetails().isEmpty()) {
            JPanel detailsPanel = new JPanel(new BorderLayout());
            detailsPanel.setBackground(new Color(236, 240, 241));
            detailsPanel.setBorder(new CompoundBorder(
                    new LineBorder(new Color(189, 195, 199), 1, true),
                    new EmptyBorder(12, 12, 12, 12)
            ));

            JLabel detailsLabel = new JLabel("Details:");
            detailsLabel.setFont(new Font("Arial", Font.BOLD, 12));
            detailsLabel.setForeground(PremiumTheme.TEXT_SECONDARY);

            JTextArea detailsArea = new JTextArea(task.getDetails());
            detailsArea.setFont(PremiumTheme.FONT_BODY);
            detailsArea.setLineWrap(true);
            detailsArea.setWrapStyleWord(true);
            detailsArea.setEditable(false);
            detailsArea.setOpaque(false);
            detailsArea.setBorder(new EmptyBorder(8, 0, 0, 0));

            detailsPanel.add(detailsLabel, BorderLayout.NORTH);
            detailsPanel.add(detailsArea, BorderLayout.CENTER);

            body.add(detailsPanel);
            body.add(Box.createVerticalStrut(15));
        }

        JPanel timestampPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        timestampPanel.setOpaque(false);
        timestampPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel createdLabel = new JLabel("Created: " +
                task.getCreated().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")));
        createdLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        createdLabel.setForeground(PremiumTheme.TEXT_SECONDARY);

        JLabel modifiedLabel = new JLabel("Modified: " +
                task.getModified().format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")));
        modifiedLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        modifiedLabel.setForeground(PremiumTheme.TEXT_SECONDARY);

        timestampPanel.add(createdLabel);
        timestampPanel.add(modifiedLabel);
        body.add(timestampPanel);

        return body;
    }

    private JPanel createInfoRow(String label, String value, Color bgColor, Color textColor) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setMaximumSize(new Dimension(500, 40));
        row.setOpaque(false);

        JLabel labelComp = new JLabel(label + ":");
        labelComp.setFont(new Font("Arial", Font.BOLD, 11));
        labelComp.setForeground(PremiumTheme.TEXT_SECONDARY);
        labelComp.setPreferredSize(new Dimension(80, 40));

        JPanel valueBadge = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 9));
        valueBadge.setBackground(bgColor);
        valueBadge.setBorder(new CompoundBorder(
                new LineBorder(bgColor.darker(), 1, true),
                new EmptyBorder(0, 12, 0, 12)
        ));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 12));
        valueLabel.setForeground(textColor);
        valueBadge.add(valueLabel);

        row.add(labelComp, BorderLayout.WEST);
        row.add(valueBadge, BorderLayout.CENTER);

        return row;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        footer.setBackground(new Color(250, 250, 250));
        footer.setBorder(new MatteBorder(2, 0, 0, 0, new Color(220, 220, 220)));
        footer.setPreferredSize(new Dimension(550, 85));

        JButton editBtn = createActionButton("EDIT", new Color(41, 128, 185), e -> {
            dispose();
            if (mainFrame != null && mainFrame.taskFormPanel != null) {
                mainFrame.taskFormPanel.loadTask(task);
            }
        });

        JButton completeBtn = createActionButton(
                task.getStatus() == Status.COMPLETED ? "UNDO" : "COMPLETE",
                new Color(39, 174, 96), e -> {
                    try {
                        if (task.getStatus() == Status.COMPLETED) {
                            task.setStatus(Status.TODO);
                            taskService.updateTask(task);
                        } else {
                            taskService.completeTask(task.getId());
                        }
                        if (mainFrame != null) mainFrame.refreshAll();
                        dispose();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                    }
                });

        JButton deleteBtn = createActionButton("DELETE", new Color(192, 57, 43), e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete this task permanently?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    taskService.deleteTask(task.getId());
                    if (mainFrame != null) mainFrame.refreshAll();
                    dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });

        JButton closeBtn = createActionButton("CLOSE", new Color(127, 140, 141), e -> dispose());

        footer.add(editBtn);
        footer.add(completeBtn);
        footer.add(deleteBtn);
        footer.add(closeBtn);

        return footer;
    }

    private JButton createActionButton(String text, Color bgColor, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);

        // Prevent Nimbus / system LAF from dulling colors
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setBorder(new CompoundBorder(
                new LineBorder(bgColor.darker(), 2, true),
                new EmptyBorder(10, 20, 10, 20)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect with visible contrast
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });

        btn.addActionListener(action);
        return btn;
    }

}
