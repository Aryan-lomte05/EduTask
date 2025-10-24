package com.edutask.ui.calendar;

import com.edutask.model.*;
import com.edutask.service.TaskService;
import com.edutask.ui.themes.PremiumTheme;
import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.util.*;
import java.util.List;

public class WeeklyGanttView extends JPanel {
    private TaskService taskService;
    private CalendarViewPanel parent;
    private int year;
    private int month;
    private int weekNumber;
    private LocalDate weekStart;

    public WeeklyGanttView(TaskService taskService, CalendarViewPanel parent) {
        this.taskService = taskService;
        this.parent = parent;

        LocalDate now = LocalDate.now();
        this.year = now.getYear();
        this.month = now.getMonthValue();
        this.weekNumber = 1;
        calculateWeekStart();

        setLayout(new BorderLayout());
        setOpaque(false);

        createWeeklyView();
    }

    public void setWeek(int year, int month, int weekNumber) {
        this.year = year;
        this.month = month;
        this.weekNumber = weekNumber;
        calculateWeekStart();
        refresh();
    }

    private void calculateWeekStart() {
        LocalDate firstOfMonth = LocalDate.of(year, month, 1);
        weekStart = firstOfMonth.plusWeeks(weekNumber - 1);
        // Adjust to start on Sunday
        while (weekStart.getDayOfWeek() != DayOfWeek.SUNDAY) {
            weekStart = weekStart.minusDays(1);
        }
    }

    private void createWeeklyView() {
        removeAll();

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton prevBtn = new JButton("< Previous");
        JButton nextBtn = new JButton("Next >");
        PremiumTheme.styleButton(prevBtn);
        PremiumTheme.styleButton(nextBtn);

        prevBtn.addActionListener(e -> changeWeek(-1));
        nextBtn.addActionListener(e -> changeWeek(1));

        LocalDate weekEnd = weekStart.plusDays(6);
        JLabel titleLabel = new JLabel(
                String.format("Week: %s - %s",
                        weekStart.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd")),
                        weekEnd.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"))),
                SwingConstants.CENTER
        );
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

        header.add(prevBtn, BorderLayout.WEST);
        header.add(titleLabel, BorderLayout.CENTER);
        header.add(nextBtn, BorderLayout.EAST);

        // Gantt-style grid
        JPanel ganttGrid = createGanttGrid();
        JScrollPane scrollPane = new JScrollPane(ganttGrid);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JPanel createGanttGrid() {
        JPanel grid = new JPanel(new BorderLayout());
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Day headers
        JPanel headerPanel = new JPanel(new GridLayout(1, 8, 2, 2));
        headerPanel.setOpaque(false);

        headerPanel.add(createHeaderCell("Task"));
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            String label = String.format("%s\n%d", days[i], date.getDayOfMonth());
            headerPanel.add(createHeaderCell(label));
        }

        // Task rows
        JPanel taskRows = new JPanel();
        taskRows.setLayout(new BoxLayout(taskRows, BoxLayout.Y_AXIS));
        taskRows.setOpaque(false);

        List<Task> weekTasks = getWeekTasks();

        if (weekTasks.isEmpty()) {
            JLabel emptyLabel = new JLabel("No tasks this week!", SwingConstants.CENTER);
            emptyLabel.setFont(PremiumTheme.FONT_TITLE);
            emptyLabel.setForeground(PremiumTheme.TEXT_SECONDARY);
            taskRows.add(emptyLabel);
        } else {
            for (Task task : weekTasks) {
                taskRows.add(createTaskRow(task));
            }
        }

        grid.add(headerPanel, BorderLayout.NORTH);
        grid.add(taskRows, BorderLayout.CENTER);

        return grid;
    }

    private JPanel createHeaderCell(String text) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBackground(PremiumTheme.CORK_LIGHT);
        cell.setBorder(BorderFactory.createLineBorder(PremiumTheme.CORK_DARK, 1));
        cell.setPreferredSize(new Dimension(120, 50));

        JLabel label = new JLabel("<html>enterer>" + text.replace("\n", "<br>") + "</center></html>");
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        cell.add(label, BorderLayout.CENTER);

        return cell;
    }

    private JPanel createTaskRow(Task task) {
        JPanel row = new JPanel(new GridLayout(1, 8, 2, 2));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        row.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

        // Task name cell
        JPanel nameCell = new JPanel(new BorderLayout());
        nameCell.setBackground(Color.WHITE);
        nameCell.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JLabel nameLabel = new JLabel("<html>" + truncate(task.getTitle(), 15) + "</html>");
        nameLabel.setFont(PremiumTheme.FONT_SMALL);
        nameLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        nameCell.add(nameLabel, BorderLayout.CENTER);

        row.add(nameCell);

        // Day cells
        LocalDate taskDate = task.getDueDate();
        for (int i = 0; i < 7; i++) {
            LocalDate currentDay = weekStart.plusDays(i);
            JPanel dayCell = new JPanel(new BorderLayout());
            dayCell.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

            if (currentDay.equals(taskDate)) {
                // Task bar on this day
                dayCell.setBackground(PremiumTheme.getStickyColorByPriority(task.getPriority()));

                String statusIcon = switch (task.getStatus()) {
                    case COMPLETED -> "Done";
                    case IN_PROGRESS -> "WIP";
                    default -> "TODO";
                };

                JLabel statusLabel = new JLabel(statusIcon, SwingConstants.CENTER);
                statusLabel.setFont(new Font("Arial", Font.BOLD, 10));
                dayCell.add(statusLabel, BorderLayout.CENTER);
            } else {
                dayCell.setBackground(Color.WHITE);
            }

            row.add(dayCell);
        }

        return row;
    }

    private List<Task> getWeekTasks() {
        LocalDate weekEnd = weekStart.plusDays(6);

        return taskService.getAllTasks().stream()
                .filter(t -> !t.getDueDate().isBefore(weekStart) && !t.getDueDate().isAfter(weekEnd))
                .sorted(Comparator.comparing(Task::getDueDate))
                .toList();
    }

    private void changeWeek(int delta) {
        weekStart = weekStart.plusWeeks(delta);
        year = weekStart.getYear();
        month = weekStart.getMonthValue();
        refresh();
    }

    private String truncate(String text, int maxLen) {
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 3) + "...";
    }

    public void refresh() {
        createWeeklyView();
    }
}
