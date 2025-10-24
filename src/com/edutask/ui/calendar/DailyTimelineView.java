package com.edutask.ui.calendar;

import com.edutask.model.*;
import com.edutask.service.TaskService;
import com.edutask.ui.themes.PremiumTheme;
import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class DailyTimelineView extends JPanel {
    private TaskService taskService;
    private CalendarViewPanel parent;
    private LocalDate currentDate;

    public DailyTimelineView(TaskService taskService, CalendarViewPanel parent) {
        this.taskService = taskService;
        this.parent = parent;
        this.currentDate = LocalDate.now();

        setLayout(new BorderLayout());
        setOpaque(false);

        createDailyView();
    }

    public void setDate(int year, int month, int day) {
        this.currentDate = LocalDate.of(year, month, day);
        refresh();
    }

    private void createDailyView() {
        removeAll();

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton prevBtn = new JButton("< Previous Day");
        JButton todayBtn = new JButton("Today");
        JButton nextBtn = new JButton("Next Day >");

        PremiumTheme.styleButton(prevBtn);
        PremiumTheme.styleButton(todayBtn);
        PremiumTheme.styleButton(nextBtn);

        prevBtn.addActionListener(e -> changeDay(-1));
        todayBtn.addActionListener(e -> goToToday());
        nextBtn.addActionListener(e -> changeDay(1));

        JPanel navButtons = new JPanel(new FlowLayout());
        navButtons.setOpaque(false);
        navButtons.add(prevBtn);
        navButtons.add(todayBtn);
        navButtons.add(nextBtn);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
        JLabel titleLabel = new JLabel(currentDate.format(formatter), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

        header.add(navButtons, BorderLayout.WEST);
        header.add(titleLabel, BorderLayout.CENTER);

        // Timeline
        JPanel timeline = createTimeline();
        JScrollPane scrollPane = new JScrollPane(timeline);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JPanel createTimeline() {
        JPanel timeline = new JPanel();
        timeline.setLayout(new BoxLayout(timeline, BoxLayout.Y_AXIS));
        timeline.setOpaque(false);
        timeline.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        List<Task> dayTasks = getDayTasks();

        // Create 24 hour slots
        for (int hour = 0; hour < 24; hour++) {
            timeline.add(createHourSlot(hour, dayTasks));
        }

        return timeline;
    }

    private JPanel createHourSlot(int hour, List<Task> dayTasks) {
        JPanel slot = new JPanel(new BorderLayout(10, 0));
        slot.setOpaque(false);
        slot.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        slot.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        // Hour label
        String timeLabel = String.format("%02d:00", hour);
        JLabel hourLabel = new JLabel(timeLabel);
        hourLabel.setFont(new Font("Arial", Font.BOLD, 14));
        hourLabel.setForeground(PremiumTheme.TEXT_PRIMARY);
        hourLabel.setPreferredSize(new Dimension(60, 0));

        // Task area for this hour
        JPanel taskArea = new JPanel();
        taskArea.setLayout(new BoxLayout(taskArea, BoxLayout.Y_AXIS));
        taskArea.setBackground(Color.WHITE);
        taskArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Highlight current hour
        LocalTime now = LocalTime.now();
        if (currentDate.equals(LocalDate.now()) && now.getHour() == hour) {
            taskArea.setBackground(new Color(255, 255, 200));
        }

        // Add tasks for this hour (we'll display all day tasks in morning slots)
        if (hour >= 8 && hour <= 10) {
            for (Task task : dayTasks) {
                taskArea.add(createTaskCard(task));
            }
        }

        if (taskArea.getComponentCount() == 0) {
            JLabel emptyLabel = new JLabel("No tasks scheduled");
            emptyLabel.setFont(PremiumTheme.FONT_SMALL);
            emptyLabel.setForeground(Color.LIGHT_GRAY);
            taskArea.add(emptyLabel);
        }

        slot.add(hourLabel, BorderLayout.WEST);
        slot.add(taskArea, BorderLayout.CENTER);

        return slot;
    }

    private JPanel createTaskCard(Task task) {
        JPanel card = new JPanel(new BorderLayout(5, 0));
        card.setBackground(PremiumTheme.getStickyColorByPriority(task.getPriority()));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PremiumTheme.CORK_DARK, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Task title
        JLabel titleLabel = new JLabel(task.getTitle());
        titleLabel.setFont(PremiumTheme.FONT_BODY);

        // Status indicator
        String statusText = switch (task.getStatus()) {
            case COMPLETED -> "DONE";
            case IN_PROGRESS -> "WIP";
            default -> "TODO";
        };

        JLabel statusLabel = new JLabel(statusText);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 10));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(task.getStatus() == Status.COMPLETED ?
                new Color(50, 200, 50) : new Color(255, 150, 50));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        card.add(titleLabel, BorderLayout.CENTER);
        card.add(statusLabel, BorderLayout.EAST);

        return card;
    }

    private List<Task> getDayTasks() {
        return taskService.getAllTasks().stream()
                .filter(t -> t.getDueDate().equals(currentDate))
                .sorted(Comparator.comparing(Task::getPriority).reversed())
                .toList();
    }

    private void changeDay(int delta) {
        currentDate = currentDate.plusDays(delta);
        refresh();
    }

    private void goToToday() {
        currentDate = LocalDate.now();
        refresh();
    }

    public void refresh() {
        createDailyView();
    }
}
