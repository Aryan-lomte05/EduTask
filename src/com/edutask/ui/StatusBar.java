package com.edutask.ui;

import com.edutask.service.TaskService;
import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class StatusBar extends JPanel {
    private TaskService taskService;
    private JLabel totalLabel;
    private JLabel todayLabel;
    private JLabel completedLabel;
    private JLabel streakLabel;
    private JLabel clockLabel;

    public StatusBar(TaskService taskService) {
        this.taskService = taskService;

        setLayout(new FlowLayout(FlowLayout.LEFT, 15, 5));
        setBorder(BorderFactory.createEtchedBorder());

        totalLabel = new JLabel();
        todayLabel = new JLabel();
        completedLabel = new JLabel();
        streakLabel = new JLabel();
        clockLabel = new JLabel();

        add(totalLabel);
        add(new JSeparator(SwingConstants.VERTICAL));
        add(todayLabel);
        add(new JSeparator(SwingConstants.VERTICAL));
        add(completedLabel);
        add(new JSeparator(SwingConstants.VERTICAL));
        add(streakLabel);
        add(Box.createHorizontalGlue());
        add(clockLabel);

        updateStatus();
        startClock();
    }

    public void updateStatus() {
        int total = taskService.getAllTasks().size();
        int today = taskService.getTodayTasks().size();
        int completed = taskService.getTasksByStatus(com.edutask.model.Status.COMPLETED).size();
        int streak = taskService.getStreak();

        totalLabel.setText("📋 Total: " + total);
        todayLabel.setText("📅 Today: " + today);
        completedLabel.setText("✅ Done: " + completed);
        streakLabel.setText("🔥 Streak: " + streak);
    }

    private void startClock() {
        Timer timer = new Timer(1000, e -> {
            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            clockLabel.setText("🕐 " + time);
        });
        timer.start();
    }
}
