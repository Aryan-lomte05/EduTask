package com.edutask.ui.calendar;

import com.edutask.model.*;
import com.edutask.service.TaskService;
import com.edutask.ui.themes.PremiumTheme;
import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.util.*;
import java.util.List;

public class MonthlyCalendarView extends JPanel {
    private TaskService taskService;
    private CalendarViewPanel parent;
    private int year;
    private int month;

    public MonthlyCalendarView(TaskService taskService, CalendarViewPanel parent) {
        this.taskService = taskService;
        this.parent = parent;

        LocalDate now = LocalDate.now();
        this.year = now.getYear();
        this.month = now.getMonthValue();

        setLayout(new BorderLayout());
        setOpaque(false);

        createCalendar();
    }

    public void setYearMonth(int year, int month) {
        this.year = year;
        this.month = month;
        refresh();
    }

    private void createCalendar() {
        removeAll();

        // Header with month/year and navigation
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        JButton prevBtn = new JButton("◀ Previous");
        JButton nextBtn = new JButton("Next ▶");
        PremiumTheme.styleButton(prevBtn);
        PremiumTheme.styleButton(nextBtn);

        prevBtn.setFont(new Font("Arial", Font.BOLD, 12));
        nextBtn.setFont(new Font("Arial", Font.BOLD, 12));

        prevBtn.addActionListener(e -> changeMonth(-1));
        nextBtn.addActionListener(e -> changeMonth(1));

        // Month name and year
        String monthName = Month.of(month).name();
        JLabel titleLabel = new JLabel(monthName + " " + year, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(PremiumTheme.TEXT_PRIMARY);

        // Back to year view button
        JButton backToYearBtn = new JButton("Back to Yearly");
        PremiumTheme.styleButton(backToYearBtn);
        backToYearBtn.addActionListener(e -> parent.switchToView("YEARLY"));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(backToYearBtn, BorderLayout.EAST);

        header.add(prevBtn, BorderLayout.WEST);
        header.add(titlePanel, BorderLayout.CENTER);
        header.add(nextBtn, BorderLayout.EAST);

        // Calendar grid
        JPanel calendarGrid = new JPanel(new GridLayout(0, 7, 3, 3));
        calendarGrid.setOpaque(false);
        calendarGrid.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Day of week headers
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        Color[] dayColors = {
                new Color(255, 100, 100),  // Sunday - red
                new Color(100, 150, 255),  // Monday - blue
                new Color(100, 150, 255),
                new Color(100, 150, 255),
                new Color(100, 150, 255),
                new Color(100, 150, 255),
                new Color(255, 200, 100)   // Saturday - orange
        };

        for (int i = 0; i < days.length; i++) {
            JPanel dayHeaderPanel = new JPanel(new BorderLayout());
            dayHeaderPanel.setBackground(dayColors[i]);
            dayHeaderPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.DARK_GRAY, 1),
                    BorderFactory.createEmptyBorder(8, 5, 8, 5)
            ));

            JLabel dayLabel = new JLabel(days[i], SwingConstants.CENTER);
            dayLabel.setFont(new Font("Arial", Font.BOLD, 13));
            dayLabel.setForeground(Color.WHITE);
            dayHeaderPanel.add(dayLabel, BorderLayout.CENTER);

            calendarGrid.add(dayHeaderPanel);
        }

        // Calculate calendar layout
        LocalDate firstDay = LocalDate.of(year, month, 1);
        int startDayOfWeek = firstDay.getDayOfWeek().getValue() % 7; // Sunday = 0
        int daysInMonth = firstDay.lengthOfMonth();

        // Empty cells before month starts
        for (int i = 0; i < startDayOfWeek; i++) {
            JPanel emptyCell = new JPanel();
            emptyCell.setBackground(new Color(230, 230, 230));
            emptyCell.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            calendarGrid.add(emptyCell);
        }

        // Day cells with tasks
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = LocalDate.of(year, month, day);
            calendarGrid.add(createDayCell(day, date));
        }

        // Empty cells after month ends
        int totalCells = startDayOfWeek + daysInMonth;
        int remainingCells = (7 - (totalCells % 7)) % 7;
        for (int i = 0; i < remainingCells; i++) {
            JPanel emptyCell = new JPanel();
            emptyCell.setBackground(new Color(230, 230, 230));
            emptyCell.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            calendarGrid.add(emptyCell);
        }

        add(header, BorderLayout.NORTH);
        add(calendarGrid, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JPanel createDayCell(int day, LocalDate cellDate) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBackground(Color.WHITE);
        cell.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        cell.setPreferredSize(new Dimension(100, 100));
        cell.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Day number
        JLabel dayLabel = new JLabel(String.valueOf(day), SwingConstants.LEFT);
        dayLabel.setFont(new Font("Arial", Font.BOLD, 16));
        dayLabel.setBorder(BorderFactory.createEmptyBorder(5, 8, 0, 0));

        // Highlight today
        if (cellDate.equals(LocalDate.now())) {
            cell.setBackground(new Color(255, 255, 180));
            cell.setBorder(BorderFactory.createLineBorder(new Color(255, 200, 0), 3));
            dayLabel.setForeground(new Color(200, 100, 0));
        }

        // Weekend styling
        DayOfWeek dayOfWeek = cellDate.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SUNDAY) {
            dayLabel.setForeground(new Color(200, 50, 50));
        } else if (dayOfWeek == DayOfWeek.SATURDAY) {
            dayLabel.setForeground(new Color(200, 100, 0));
        }

        // Task indicators (colored dots)
        JPanel dotsPanel = createTaskDotsPanel(cellDate);

        // Task count label
        int taskCount = getTaskCount(cellDate);
        if (taskCount > 0) {
            JLabel countLabel = new JLabel(taskCount + " task" + (taskCount > 1 ? "s" : ""),
                    SwingConstants.CENTER);
            countLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            countLabel.setForeground(Color.DARK_GRAY);

            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.setOpaque(false);
            bottomPanel.add(countLabel, BorderLayout.SOUTH);

            cell.add(bottomPanel, BorderLayout.SOUTH);
        }

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(dayLabel, BorderLayout.NORTH);
        topPanel.add(dotsPanel, BorderLayout.CENTER);

        cell.add(topPanel, BorderLayout.CENTER);

        // Click handler - Returns to home page with selected date
        final LocalDate selectedDate = cellDate;
        cell.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // Return to home page showing this day's tasks
                parent.returnToHome(selectedDate);
            }

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!cellDate.equals(LocalDate.now())) {
                    cell.setBackground(new Color(245, 245, 245));
                    cell.setBorder(BorderFactory.createLineBorder(new Color(100, 150, 255), 2));
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!cellDate.equals(LocalDate.now())) {
                    cell.setBackground(Color.WHITE);
                    cell.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                } else {
                    cell.setBackground(new Color(255, 255, 180));
                    cell.setBorder(BorderFactory.createLineBorder(new Color(255, 200, 0), 3));
                }
            }
        });

        return cell;
    }

    private JPanel createTaskDotsPanel(LocalDate date) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        List<Task> dayTasks = taskService.getAllTasks().stream()
                .filter(t -> t.getDueDate().equals(date))
                .limit(6)  // Show max 6 dots
                .toList();

        int maxDots = 6;
        int count = 0;

        for (Task task : dayTasks) {
            if (count >= maxDots) {
                JLabel moreLabel = new JLabel("+more");
                moreLabel.setFont(new Font("Arial", Font.BOLD, 9));
                moreLabel.setForeground(Color.DARK_GRAY);
                panel.add(moreLabel);
                break;
            }

            // Create colored dot for each task
            JPanel dot = new JPanel();
            dot.setPreferredSize(new Dimension(10, 10));

            // Color based on status
            if (task.getStatus() == Status.COMPLETED) {
                dot.setBackground(new Color(50, 200, 50));  // Green for completed
            } else if (task.getStatus() == Status.IN_PROGRESS) {
                dot.setBackground(new Color(255, 180, 0));  // Orange for in-progress
            } else {
                dot.setBackground(PremiumTheme.getStickyColorByPriority(task.getPriority()));
            }

            dot.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
            dot.setOpaque(true);

            // Tooltip with task name
            dot.setToolTipText(task.getTitle());

            panel.add(dot);
            count++;
        }

        return panel;
    }

    private int getTaskCount(LocalDate date) {
        return (int) taskService.getAllTasks().stream()
                .filter(t -> t.getDueDate().equals(date))
                .count();
    }

    private void changeMonth(int delta) {
        month += delta;
        if (month > 12) {
            month = 1;
            year++;
        } else if (month < 1) {
            month = 12;
            year--;
        }
        refresh();
    }

    public void refresh() {
        createCalendar();
    }
}
