package com.edutask.ui.calendar;

import com.edutask.model.*;
import com.edutask.service.TaskService;
import com.edutask.ui.themes.PremiumTheme;
import javax.swing.*;
import java.awt.*;
import java.time.*;
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
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton prevBtn = new JButton("<");
        JButton nextBtn = new JButton(">");
        PremiumTheme.styleButton(prevBtn);
        PremiumTheme.styleButton(nextBtn);

        prevBtn.addActionListener(e -> changeMonth(-1));
        nextBtn.addActionListener(e -> changeMonth(1));

        String monthName = Month.of(month).name();
        JLabel titleLabel = new JLabel(monthName + " " + year, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

        header.add(prevBtn, BorderLayout.WEST);
        header.add(titleLabel, BorderLayout.CENTER);
        header.add(nextBtn, BorderLayout.EAST);

        // Calendar grid
        JPanel calendarGrid = new JPanel(new GridLayout(0, 7, 2, 2));
        calendarGrid.setOpaque(false);
        calendarGrid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Day headers
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : days) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            dayLabel.setFont(new Font("Arial", Font.BOLD, 12));
            dayLabel.setForeground(PremiumTheme.TEXT_PRIMARY);
            calendarGrid.add(dayLabel);
        }

        // Day cells
        LocalDate firstDay = LocalDate.of(year, month, 1);
        int startDayOfWeek = firstDay.getDayOfWeek().getValue() % 7; // Sunday = 0
        int daysInMonth = firstDay.lengthOfMonth();

        // Empty cells before month starts
        for (int i = 0; i < startDayOfWeek; i++) {
            calendarGrid.add(new JLabel(""));
        }

        // Day cells
        for (int day = 1; day <= daysInMonth; day++) {
            calendarGrid.add(createDayCell(day));
        }

        add(header, BorderLayout.NORTH);
        add(calendarGrid, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JPanel createDayCell(int day) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBackground(Color.WHITE);
        cell.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        cell.setPreferredSize(new Dimension(80, 80));
        cell.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Day number
        JLabel dayLabel = new JLabel(String.valueOf(day));
        dayLabel.setFont(new Font("Arial", Font.BOLD, 14));
        dayLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));

        // Highlight today
        LocalDate cellDate = LocalDate.of(year, month, day);
        if (cellDate.equals(LocalDate.now())) {
            cell.setBackground(new Color(255, 255, 200));
        }

        // Task indicators (colored dots)
        JPanel dotsPanel = createTaskDotsPanel(cellDate);

        cell.add(dayLabel, BorderLayout.NORTH);
        cell.add(dotsPanel, BorderLayout.CENTER);

        // Click to view day
        final int dayNum = day;
        cell.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                parent.switchToDay(year, month, dayNum);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!cellDate.equals(LocalDate.now())) {
                    cell.setBackground(new Color(240, 240, 240));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!cellDate.equals(LocalDate.now())) {
                    cell.setBackground(Color.WHITE);
                } else {
                    cell.setBackground(new Color(255, 255, 200));
                }
            }
        });

        return cell;
    }

    private JPanel createTaskDotsPanel(LocalDate date) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
        panel.setOpaque(false);

        List<Task> dayTasks = taskService.getAllTasks().stream()
                .filter(t -> t.getDueDate().equals(date))
                .toList();

        int maxDots = 5;
        int count = 0;
        for (Task task : dayTasks) {
            if (count >= maxDots) {
                JLabel moreLabel = new JLabel("+" + (dayTasks.size() - maxDots));
                moreLabel.setFont(new Font("Arial", Font.PLAIN, 9));
                panel.add(moreLabel);
                break;
            }

            JPanel dot = new JPanel();
            dot.setPreferredSize(new Dimension(8, 8));
            dot.setBackground(task.getStatus() == Status.COMPLETED ?
                    new Color(50, 200, 50) : PremiumTheme.getStickyColorByPriority(task.getPriority()));
            dot.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
            panel.add(dot);
            count++;
        }

        return panel;
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
