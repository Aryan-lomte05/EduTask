package com.edutask.ui;

import com.edutask.service.TaskService;
import com.edutask.events.*;
import com.edutask.model.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;

public class AnalyticsPanel extends JPanel implements TaskEventListener<TaskCompletedEvent> {
    private TaskService taskService;
    private JLabel statsLabel;
    private GraphPanel graphPanel;

    public AnalyticsPanel(TaskService taskService, EventBus eventBus) {
        this.taskService = taskService;
        eventBus.subscribe(TaskCompletedEvent.class, this);

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initializeUI();
        refresh();
    }

    private void initializeUI() {
        statsLabel = new JLabel();
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        statsLabel.setVerticalAlignment(SwingConstants.TOP);

        graphPanel = new GraphPanel();

        JScrollPane scrollPane = new JScrollPane(statsLabel);
        scrollPane.setPreferredSize(new Dimension(0, 150));

        add(scrollPane, BorderLayout.NORTH);
        add(graphPanel, BorderLayout.CENTER);
    }

    public void refresh() {
        updateStats();
        graphPanel.updateData(taskService.getAllTasks());
    }

    private void updateStats() {
        int total = taskService.getAllTasks().size();
        int completed = taskService.getTasksByStatus(Status.COMPLETED).size();
        int todo = taskService.getTasksByStatus(Status.TODO).size();
        int inProgress = taskService.getTasksByStatus(Status.IN_PROGRESS).size();

        double completionRate = total > 0 ? (completed * 100.0 / total) : 0;

        String stats = String.format("""
            <html>
            <h3>📊 Task Analytics</h3>
            <b>Total Tasks:</b> %d<br>
            <b>Completed:</b> %d (%.1f%%)<br>
            <b>To Do:</b> %d<br>
            <b>In Progress:</b> %d<br>
            <b>Streak:</b> %d days 🔥<br>
            </html>
            """, total, completed, completionRate, todo, inProgress, taskService.getStreak());

        statsLabel.setText(stats);
    }

    @Override
    public void onEvent(TaskCompletedEvent event) {
        refresh();
    }

    // Inner class for simple bar chart
    private static class GraphPanel extends JPanel {
        private Map<String, Integer> data = new HashMap<>();

        public void updateData(java.util.List<Task> tasks) {
            data.clear();

            for (Task task : tasks) {
                if (task.getStatus() == Status.COMPLETED) {
                    String key = task.getDisplaySubject();
                    data.put(key, data.getOrDefault(key, 0) + 1);
                }
            }

            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (data.isEmpty()) {
                g.drawString("No completed tasks yet!", 20, getHeight() / 2);
                return;
            }

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int maxCount = data.values().stream().max(Integer::compare).orElse(1);
            int barWidth = Math.min(60, getWidth() / (data.size() + 1));
            int x = 20;

            g2.setFont(new Font("Arial", Font.PLAIN, 10));

            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                int barHeight = (int) ((entry.getValue() / (double) maxCount) * (getHeight() - 60));

                g2.setColor(new Color(100, 150, 255));
                g2.fillRect(x, getHeight() - barHeight - 30, barWidth, barHeight);

                g2.setColor(Color.BLACK);
                g2.drawString(entry.getKey(), x, getHeight() - 15);
                g2.drawString(String.valueOf(entry.getValue()), x + barWidth/2 - 5,
                        getHeight() - barHeight - 35);

                x += barWidth + 10;
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(300, 200);
        }
    }
}
