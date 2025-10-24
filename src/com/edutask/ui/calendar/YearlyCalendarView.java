package com.edutask.ui.calendar;

import com.edutask.model.*;
import com.edutask.service.TaskService;
import com.edutask.ui.themes.PremiumTheme;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class YearlyCalendarView extends JPanel {
    private TaskService taskService;
    private CalendarViewPanel parent;
    private int currentYear;

    public YearlyCalendarView(TaskService taskService, CalendarViewPanel parent) {
        this.taskService = taskService;
        this.parent = parent;
        this.currentYear = LocalDate.now().getYear();

        setLayout(new GridLayout(3, 4, 15, 15));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        createMonthPanels();
    }

    private void createMonthPanels() {
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        for (int month = 1; month <= 12; month++) {
            add(createMonthPanel(month, months[month - 1]));
        }
    }

    private JPanel createMonthPanel(int month, String monthName) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(new Color(255, 250, 240));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PremiumTheme.CORK_DARK, 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Month name
        JLabel nameLabel = new JLabel(monthName, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(PremiumTheme.TEXT_PRIMARY);

        // Task counts
        Map<String, Integer> counts = getMonthTaskCounts(currentYear, month);

        JPanel statsPanel = new JPanel(new GridLayout(3, 1, 2, 2));
        statsPanel.setOpaque(false);

        JLabel totalLabel = new JLabel("Total: " + counts.get("total"), SwingConstants.CENTER);
        totalLabel.setFont(PremiumTheme.FONT_SMALL);

        JLabel completedLabel = new JLabel("Done: " + counts.get("completed"), SwingConstants.CENTER);
        completedLabel.setFont(PremiumTheme.FONT_SMALL);
        completedLabel.setForeground(new Color(50, 150, 50));

        JLabel pendingLabel = new JLabel("Pending: " + counts.get("pending"), SwingConstants.CENTER);
        pendingLabel.setFont(PremiumTheme.FONT_SMALL);
        pendingLabel.setForeground(new Color(200, 100, 50));

        statsPanel.add(totalLabel);
        statsPanel.add(completedLabel);
        statsPanel.add(pendingLabel);

        panel.add(nameLabel, BorderLayout.NORTH);
        panel.add(statsPanel, BorderLayout.CENTER);

        // Click to view month
        final int monthNum = month;
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                parent.switchToMonth(currentYear, monthNum);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                panel.setBackground(new Color(255, 245, 220));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                panel.setBackground(new Color(255, 250, 240));
            }
        });

        return panel;
    }

    private Map<String, Integer> getMonthTaskCounts(int year, int month) {
        List<Task> allTasks = taskService.getAllTasks();

        int total = 0, completed = 0, pending = 0;

        for (Task task : allTasks) {
            LocalDate dueDate = task.getDueDate();
            if (dueDate.getYear() == year && dueDate.getMonthValue() == month) {
                total++;
                if (task.getStatus() == Status.COMPLETED) {
                    completed++;
                } else {
                    pending++;
                }
            }
        }

        Map<String, Integer> counts = new HashMap<>();
        counts.put("total", total);
        counts.put("completed", completed);
        counts.put("pending", pending);
        return counts;
    }

    public void refresh() {
        removeAll();
        createMonthPanels();
        revalidate();
        repaint();
    }
}
