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

        setLayout(new BorderLayout());
        setOpaque(false);

        createYearView();
    }

    private void createYearView() {
        removeAll();

        // Header with year selector
        JPanel header = new JPanel(new BorderLayout(10, 10));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        JButton prevYear = new JButton("<< " + (currentYear - 1));
        JButton nextYear = new JButton((currentYear + 1) + " >>");

        PremiumTheme.styleButton(prevYear);
        PremiumTheme.styleButton(nextYear);

        prevYear.addActionListener(e -> {
            currentYear--;
            refresh();
        });

        nextYear.addActionListener(e -> {
            currentYear++;
            refresh();
        });

        JLabel yearLabel = new JLabel(String.valueOf(currentYear), SwingConstants.CENTER);
        yearLabel.setFont(new Font("Arial", Font.BOLD, 24));
        yearLabel.setForeground(PremiumTheme.TEXT_PRIMARY);

        header.add(prevYear, BorderLayout.WEST);
        header.add(yearLabel, BorderLayout.CENTER);
        header.add(nextYear, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // 12 months grid
        JPanel monthsGrid = new JPanel(new GridLayout(3, 4, 20, 20));
        monthsGrid.setOpaque(false);
        monthsGrid.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        createMonthPanels(monthsGrid);

        add(monthsGrid, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private void createMonthPanels(JPanel container) {
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        Color[] seasonColors = {
                new Color(200, 220, 255), new Color(200, 220, 255), new Color(220, 255, 220), // Win, Win, Spr
                new Color(220, 255, 220), new Color(255, 255, 200), new Color(255, 255, 200), // Spr, Sum, Sum
                new Color(255, 240, 200), new Color(255, 240, 200), new Color(255, 220, 180), // Sum, Sum, Fall
                new Color(255, 220, 180), new Color(220, 230, 255), new Color(220, 230, 255)  // Fall, Win, Win
        };

        for (int month = 1; month <= 12; month++) {
            container.add(createMonthPanel(month, months[month - 1], seasonColors[month - 1]));
        }
    }

    private JPanel createMonthPanel(int month, String monthName, Color bgColor) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PremiumTheme.CORK_DARK, 3, true),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Month name
        JLabel nameLabel = new JLabel(monthName, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(new Color(60, 60, 60));

        // Task counts
        Map<String, Integer> counts = getMonthTaskCounts(currentYear, month);

        JPanel statsPanel = new JPanel(new GridLayout(4, 1, 3, 3));
        statsPanel.setOpaque(false);

        JLabel totalLabel = new JLabel("Total: " + counts.get("total"), SwingConstants.CENTER);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalLabel.setForeground(new Color(50, 50, 50));

        JLabel completedLabel = new JLabel("Completed: " + counts.get("completed"), SwingConstants.CENTER);
        completedLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        completedLabel.setForeground(new Color(50, 150, 50));

        JLabel inProgressLabel = new JLabel("In Progress: " + counts.get("inProgress"), SwingConstants.CENTER);
        inProgressLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        inProgressLabel.setForeground(new Color(255, 150, 0));

        JLabel pendingLabel = new JLabel("Pending: " + counts.get("pending"), SwingConstants.CENTER);
        pendingLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        pendingLabel.setForeground(new Color(200, 100, 50));

        statsPanel.add(totalLabel);
        statsPanel.add(completedLabel);
        statsPanel.add(inProgressLabel);
        statsPanel.add(pendingLabel);

        panel.add(nameLabel, BorderLayout.NORTH);
        panel.add(statsPanel, BorderLayout.CENTER);

        // Click to view month details
        final int monthNum = month;
        panel.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = bgColor;

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                parent.switchToMonth(currentYear, monthNum);
            }

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                panel.setBackground(originalColor.brighter());
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(100, 150, 255), 4, true),
                        BorderFactory.createEmptyBorder(11, 11, 11, 11)
                ));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                panel.setBackground(originalColor);
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PremiumTheme.CORK_DARK, 3, true),
                        BorderFactory.createEmptyBorder(12, 12, 12, 12)
                ));
            }
        });

        return panel;
    }

    private Map<String, Integer> getMonthTaskCounts(int year, int month) {
        List<Task> allTasks = taskService.getAllTasks();

        int total = 0, completed = 0, inProgress = 0, pending = 0;

        for (Task task : allTasks) {
            LocalDate dueDate = task.getDueDate();
            if (dueDate.getYear() == year && dueDate.getMonthValue() == month) {
                total++;
                switch (task.getStatus()) {
                    case COMPLETED -> completed++;
                    case IN_PROGRESS -> inProgress++;
                    default -> pending++;
                }
            }
        }

        Map<String, Integer> counts = new HashMap<>();
        counts.put("total", total);
        counts.put("completed", completed);
        counts.put("inProgress", inProgress);
        counts.put("pending", pending);
        return counts;
    }

    public void refresh() {
        createYearView();
    }
}
