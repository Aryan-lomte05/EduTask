package com.edutask.ui;

import com.edutask.service.TaskService;
import com.edutask.events.*;
import com.edutask.model.*;
import com.edutask.ui.themes.PremiumTheme;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class AnalyticsPanel extends JPanel implements TaskEventListener<TaskCompletedEvent> {
    private TaskService taskService;
    private JPanel statsPanel;
    private PremiumChartPanel chartPanel;

    public AnalyticsPanel(TaskService taskService, EventBus eventBus) {
        this.taskService = taskService;
        eventBus.subscribe(TaskCompletedEvent.class, this);

        setLayout(new BorderLayout(15, 15));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initializeUI();
        refresh();
    }

    private void initializeUI() {
        // Stats cards at top
        statsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        statsPanel.setOpaque(false);
        statsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        // Chart panel
        chartPanel = new PremiumChartPanel();

        add(statsPanel, BorderLayout.NORTH);
        add(chartPanel, BorderLayout.CENTER);
    }

    public void refresh() {
        updateStats();
        chartPanel.updateData(taskService.getAllTasks());
    }

    private void updateStats() {
        statsPanel.removeAll();

        int total = taskService.getAllTasks().size();
        int completed = taskService.getTasksByStatus(Status.COMPLETED).size();
        int inProgress = taskService.getTasksByStatus(Status.IN_PROGRESS).size();
        int todo = taskService.getTasksByStatus(Status.TODO).size();

        double completionRate = total > 0 ? (completed * 100.0 / total) : 0;
        int streak = taskService.getStreak();

        statsPanel.add(createStatCard("Total Tasks", String.valueOf(total),
                new Color(100, 150, 255), "All tasks in system"));
        statsPanel.add(createStatCard("Completed",
                String.format("%d (%.1f%%)", completed, completionRate),
                new Color(50, 200, 100), "Finished tasks"));
        statsPanel.add(createStatCard("In Progress", String.valueOf(inProgress),
                new Color(255, 180, 50), "Currently working on"));
        statsPanel.add(createStatCard("Streak", streak + " days",
                new Color(255, 100, 100), "Consecutive days"));

        statsPanel.revalidate();
        statsPanel.repaint();
    }

    private JPanel createStatCard(String label, String value, Color color, String tooltip) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(color);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 2, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setToolTipText(tooltip);

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(Color.WHITE);

        JLabel labelLabel = new JLabel(label, SwingConstants.CENTER);
        labelLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        labelLabel.setForeground(Color.WHITE);

        card.add(valueLabel, BorderLayout.CENTER);
        card.add(labelLabel, BorderLayout.SOUTH);

        return card;
    }

    @Override
    public void onEvent(TaskCompletedEvent event) {
        refresh();
    }

    // Premium chart panel
    private static class PremiumChartPanel extends JPanel {
        private Map<String, Integer> data = new HashMap<>();
        private Map<String, Integer> completedData = new HashMap<>();

        public PremiumChartPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(0, 300));
            setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(PremiumTheme.CORK_DARK, 2),
                    "Task Distribution by Subject", 0, 0,
                    new Font("Arial", Font.BOLD, 14), PremiumTheme.TEXT_PRIMARY
            ));
        }

        public void updateData(List<Task> tasks) {
            data.clear();
            completedData.clear();

            for (Task task : tasks) {
                String key = task.getDisplaySubject();
                data.put(key, data.getOrDefault(key, 0) + 1);

                if (task.getStatus() == Status.COMPLETED) {
                    completedData.put(key, completedData.getOrDefault(key, 0) + 1);
                }
            }

            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (data.isEmpty()) {
                g.setFont(new Font("Arial", Font.PLAIN, 14));
                g.setColor(Color.GRAY);
                g.drawString("No data to display yet!", getWidth() / 2 - 80, getHeight() / 2);
                return;
            }

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            List<Map.Entry<String, Integer>> sortedData = data.entrySet().stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .limit(8)  // Show only top 8
                    .toList();

            int maxCount = data.values().stream().max(Integer::compare).orElse(1);
            int barWidth = Math.min(80, (getWidth() - 60) / (data.size() + 1));
            int x = 30;
            int chartHeight = getHeight() - 80;

            Color[] colors = {
                    new Color(100, 150, 255),
                    new Color(255, 180, 50),
                    new Color(100, 200, 100),
                    new Color(255, 100, 100),
                    new Color(150, 100, 255)
            };

            int colorIndex = 0;
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                int totalCount = entry.getValue();
                int completedCount = completedData.getOrDefault(entry.getKey(), 0);

                int totalHeight = (int) ((totalCount / (double) maxCount) * chartHeight);
                int completedHeight = (int) ((completedCount / (double) maxCount) * chartHeight);

                // Total bar (lighter)
                g2.setColor(colors[colorIndex % colors.length]);
                g2.fillRect(x, getHeight() - totalHeight - 50, barWidth, totalHeight);

                // Completed overlay (darker)
                g2.setColor(colors[colorIndex % colors.length].darker());
                g2.fillRect(x, getHeight() - completedHeight - 50, barWidth, completedHeight);

                // Border
                g2.setColor(Color.DARK_GRAY);
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(x, getHeight() - totalHeight - 50, barWidth, totalHeight);

                // Count labels
                g2.setFont(new Font("Arial", Font.BOLD, 11));
                String countText = totalCount + "/" + completedCount;
                g2.drawString(countText, x + barWidth/2 - 15, getHeight() - totalHeight - 55);

                // Subject labels
                g2.setFont(new Font("Arial", Font.PLAIN, 10));
                g2.drawString(truncate(entry.getKey(), 10), x, getHeight() - 30);

                x += barWidth + 15;
                colorIndex++;
            }

            // Legend
            g2.setFont(new Font("Arial", Font.PLAIN, 11));
            g2.drawString("Total / Completed", 10, 20);
        }

        private String truncate(String text, int maxLen) {
            if (text.length() <= maxLen) return text;
            return text.substring(0, maxLen - 2) + "..";
        }
    }
}
