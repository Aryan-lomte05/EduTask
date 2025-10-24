package com.edutask.ui.calendar;

import com.edutask.model.*;
import com.edutask.service.TaskService;
import com.edutask.ui.themes.PremiumTheme;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class WeeklyGanttView extends JPanel {
    private TaskService taskService;
    private CalendarViewPanel parent;
    private LocalDate weekStart;

    public WeeklyGanttView(TaskService taskService, CalendarViewPanel parent) {
        this.taskService = taskService;
        this.parent = parent;

        LocalDate today = LocalDate.now();
        this.weekStart = today.minusDays(today.getDayOfWeek().getValue() % 7);

        setLayout(new BorderLayout());
        setOpaque(false);
        setBackground(Color.WHITE);

        createWeeklyView();
    }

    public void setWeek(int year, int month, int weekNumber) {
        LocalDate firstOfMonth = LocalDate.of(year, month, 1);
        this.weekStart = firstOfMonth.plusWeeks(weekNumber - 1);

        while (weekStart.getDayOfWeek() != DayOfWeek.SUNDAY) {
            weekStart = weekStart.minusDays(1);
        }
        refresh();
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    private void createWeeklyView() {
        removeAll();
        add(createHeader(), BorderLayout.NORTH);
        add(createTimetable(), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(10, 10));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        JButton prevBtn = new JButton("< Previous Week");
        JButton nextBtn = new JButton("Next Week >");
        JButton todayBtn = new JButton("This Week");

        PremiumTheme.styleButton(prevBtn);
        PremiumTheme.styleButton(nextBtn);
        PremiumTheme.styleButton(todayBtn);

        prevBtn.addActionListener(e -> changeWeek(-1));
        nextBtn.addActionListener(e -> changeWeek(1));
        todayBtn.addActionListener(e -> goToCurrentWeek());

        LocalDate weekEnd = weekStart.plusDays(6);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
        String weekRange = String.format("Week: %s - %s, %d",
                weekStart.format(formatter),
                weekEnd.format(formatter),
                weekStart.getYear());

        JLabel titleLabel = new JLabel(weekRange, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(PremiumTheme.TEXT_PRIMARY);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(prevBtn);
        leftPanel.add(todayBtn);

        header.add(leftPanel, BorderLayout.WEST);
        header.add(titleLabel, BorderLayout.CENTER);
        header.add(nextBtn, BorderLayout.EAST);

        return header;
    }

    private JScrollPane createTimetable() {
        // Get tasks for the week
        Map<LocalDate, List<Task>> tasksByDay = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            List<Task> dayTasks = taskService.getAllTasks().stream()
                    .filter(t -> t.getDueDate().equals(day))
                    .toList();
            tasksByDay.put(day, dayTasks);
        }

        // Column names
        String[] columnNames = new String[8];
        columnNames[0] = "Time";

        String[] dayNames = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd");

        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            columnNames[i + 1] = String.format("<html><b>%s</b><br>%s</html>",
                    dayNames[i], day.format(dayFormatter));
        }

        // Create table with custom styling
        DefaultTableModel model = new DefaultTableModel(columnNames, 24) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Fill time column
        for (int hour = 0; hour < 24; hour++) {
            model.setValueAt(String.format("%02d:00", hour), hour, 0);
        }

        // Fill task data (show in morning slots 8-10)
        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            List<Task> dayTasks = tasksByDay.get(day);

            if (!dayTasks.isEmpty()) {
                StringBuilder taskText = new StringBuilder("<html>");
                for (Task task : dayTasks) {
                    String status = switch (task.getStatus()) {
                        case COMPLETED -> "[V]";
                        case IN_PROGRESS -> "[~]";
                        default -> "[ ]";
                    };
                    taskText.append(status).append(" ").append(truncate(task.getTitle(), 15)).append("<br>");
                }
                taskText.append("</html>");

                // Show tasks in 8-10 AM slots
                model.setValueAt(taskText.toString(), 8, i + 1);
            }
        }

        JTable table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 11));
        table.setRowHeight(60);
        table.setGridColor(new Color(200, 200, 200));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        // Custom renderer
        table.setDefaultRenderer(Object.class, new WeeklyTableRenderer(weekStart));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        for (int i = 1; i <= 7; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(150);
        }

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 12));
        header.setBackground(new Color(100, 150, 255));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 50));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        return scrollPane;
    }

    private void changeWeek(int delta) {
        weekStart = weekStart.plusWeeks(delta);
        refresh();
    }

    private void goToCurrentWeek() {
        LocalDate today = LocalDate.now();
        weekStart = today.minusDays(today.getDayOfWeek().getValue() % 7);
        refresh();
    }

    private String truncate(String text, int maxLen) {
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 2) + "..";
    }

    public void refresh() {
        createWeeklyView();
    }

    // Custom renderer
    private class WeeklyTableRenderer extends DefaultTableCellRenderer {
        private LocalDate weekStart;

        public WeeklyTableRenderer(LocalDate weekStart) {
            this.weekStart = weekStart;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);

            if (column == 0) {
                // Time column - styled
                c.setBackground(new Color(240, 240, 240));
                c.setFont(new Font("Arial", Font.BOLD, 11));
                setHorizontalAlignment(CENTER);
                setForeground(new Color(80, 80, 80));
            } else {
                // Day columns
                LocalDate cellDate = weekStart.plusDays(column - 1);

                if (value != null && value.toString().contains("<html>")) {
                    // Has tasks
                    c.setBackground(PremiumTheme.STICKY_YELLOW);
                    setVerticalAlignment(TOP);
                    setHorizontalAlignment(LEFT);
                } else {
                    // Empty
                    c.setBackground(Color.WHITE);
                }

                // Highlight today
                if (cellDate.equals(LocalDate.now())) {
                    c.setBackground(new Color(255, 255, 200));
                }

                // Weekend styling
                DayOfWeek day = cellDate.getDayOfWeek();
                if (day == DayOfWeek.SUNDAY || day == DayOfWeek.SATURDAY) {
                    if (value == null || !value.toString().contains("<html>")) {
                        c.setBackground(new Color(245, 245, 245));
                    }
                }

                // Current hour highlight
                LocalTime now = LocalTime.now();
                if (row == now.getHour() && cellDate.equals(LocalDate.now())) {
                    c.setBackground(new Color(255, 240, 200));
                }
            }

            return c;
        }
    }
}
