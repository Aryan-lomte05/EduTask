//package com.edutask.ui.calendar;
//
//import com.edutask.model.*;
//import com.edutask.service.TaskService;
//import com.edutask.ui.themes.PremiumTheme;
//import javax.swing.*;
//import javax.swing.table.*;
//import java.awt.*;
//import java.time.*;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.List;
//
//public class WeeklyGanttView extends JPanel {
//    private TaskService taskService;
//    private CalendarViewPanel parent;
//    private LocalDate weekStart;
//
//    public WeeklyGanttView(TaskService taskService, CalendarViewPanel parent) {
//        this.taskService = taskService;
//        this.parent = parent;
//
//        LocalDate today = LocalDate.now();
//        this.weekStart = today.minusDays(today.getDayOfWeek().getValue() % 7);
//
//        setLayout(new BorderLayout());
//        setOpaque(false);
//        setBackground(Color.WHITE);
//
//        createWeeklyView();
//    }
//
//    public void setWeek(int year, int month, int weekNumber) {
//        LocalDate firstOfMonth = LocalDate.of(year, month, 1);
//        this.weekStart = firstOfMonth.plusWeeks(weekNumber - 1);
//
//        while (weekStart.getDayOfWeek() != DayOfWeek.SUNDAY) {
//            weekStart = weekStart.minusDays(1);
//        }
//        refresh();
//    }
//
//    public LocalDate getWeekStart() {
//        return weekStart;
//    }
//
//    private void createWeeklyView() {
//        removeAll();
//        add(createHeader(), BorderLayout.NORTH);
//        add(createTimetable(), BorderLayout.CENTER);
//        revalidate();
//        repaint();
//    }
//
//    private JPanel createHeader() {
//        JPanel header = new JPanel(new BorderLayout(10, 10));
//        header.setOpaque(false);
//        header.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
//
//        JButton prevBtn = new JButton("< Previous Week");
//        JButton nextBtn = new JButton("Next Week >");
//        JButton todayBtn = new JButton("This Week");
//
//        PremiumTheme.styleButton(prevBtn);
//        PremiumTheme.styleButton(nextBtn);
//        PremiumTheme.styleButton(todayBtn);
//
//        prevBtn.addActionListener(e -> changeWeek(-1));
//        nextBtn.addActionListener(e -> changeWeek(1));
//        todayBtn.addActionListener(e -> goToCurrentWeek());
//
//        LocalDate weekEnd = weekStart.plusDays(6);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
//        String weekRange = String.format("Week: %s - %s, %d",
//                weekStart.format(formatter),
//                weekEnd.format(formatter),
//                weekStart.getYear());
//
//        JLabel titleLabel = new JLabel(weekRange, SwingConstants.CENTER);
//        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
//        titleLabel.setForeground(PremiumTheme.TEXT_PRIMARY);
//
//        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
//        leftPanel.setOpaque(false);
//        leftPanel.add(prevBtn);
//        leftPanel.add(todayBtn);
//
//        header.add(leftPanel, BorderLayout.WEST);
//        header.add(titleLabel, BorderLayout.CENTER);
//        header.add(nextBtn, BorderLayout.EAST);
//
//        return header;
//    }
//
//    private JScrollPane createTimetable() {
//        // Get tasks for the week
//        Map<LocalDate, List<Task>> tasksByDay = new HashMap<>();
//        for (int i = 0; i < 7; i++) {
//            LocalDate day = weekStart.plusDays(i);
//            List<Task> dayTasks = taskService.getAllTasks().stream()
//                    .filter(t -> t.getDueDate().equals(day))
//                    .toList();
//            tasksByDay.put(day, dayTasks);
//        }
//
//        // Column names
//        String[] columnNames = new String[8];
//        columnNames[0] = "Time";
//
//        String[] dayNames = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
//        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd");
//
//        for (int i = 0; i < 7; i++) {
//            LocalDate day = weekStart.plusDays(i);
//            columnNames[i + 1] = String.format("<html><b>%s</b><br>%s</html>",
//                    dayNames[i], day.format(dayFormatter));
//        }
//
//        // Create table with custom styling
//        DefaultTableModel model = new DefaultTableModel(columnNames, 24) {
//            @Override
//            public boolean isCellEditable(int row, int column) {
//                return false;
//            }
//        };
//
//        // Fill time column
//        for (int hour = 0; hour < 24; hour++) {
//            model.setValueAt(String.format("%02d:00", hour), hour, 0);
//        }
//
//        // Fill task data (show in morning slots 8-10)
//        for (int i = 0; i < 7; i++) {
//            LocalDate day = weekStart.plusDays(i);
//            List<Task> dayTasks = tasksByDay.get(day);
//
//            if (!dayTasks.isEmpty()) {
//                StringBuilder taskText = new StringBuilder("<html>");
//                for (Task task : dayTasks) {
//                    String status = switch (task.getStatus()) {
//                        case COMPLETED -> "[V]";
//                        case IN_PROGRESS -> "[~]";
//                        default -> "[ ]";
//                    };
//                    taskText.append(status).append(" ").append(truncate(task.getTitle(), 15)).append("<br>");
//                }
//                taskText.append("</html>");
//
//                // Show tasks in 8-10 AM slots
//                model.setValueAt(taskText.toString(), 8, i + 1);
//            }
//        }
//
//        JTable table = new JTable(model);
//        table.setFont(new Font("Arial", Font.PLAIN, 11));
//        table.setRowHeight(60);
//        table.setGridColor(new Color(200, 200, 200));
//        table.setShowGrid(true);
//        table.setIntercellSpacing(new Dimension(1, 1));
//
//        // Custom renderer
//        table.setDefaultRenderer(Object.class, new WeeklyTableRenderer(weekStart));
//
//        // Column widths
//        table.getColumnModel().getColumn(0).setPreferredWidth(70);
//        for (int i = 1; i <= 7; i++) {
//            table.getColumnModel().getColumn(i).setPreferredWidth(150);
//        }
//
//        // Header styling
//        JTableHeader header = table.getTableHeader();
//        header.setFont(new Font("Arial", Font.BOLD, 12));
//        header.setBackground(new Color(100, 150, 255));
//        header.setForeground(Color.WHITE);
//        header.setPreferredSize(new Dimension(0, 50));
//
//        JScrollPane scrollPane = new JScrollPane(table);
//        scrollPane.setOpaque(false);
//        scrollPane.getViewport().setOpaque(false);
//        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
//
//        return scrollPane;
//    }
//
//    private void changeWeek(int delta) {
//        weekStart = weekStart.plusWeeks(delta);
//        refresh();
//    }
//
//    private void goToCurrentWeek() {
//        LocalDate today = LocalDate.now();
//        weekStart = today.minusDays(today.getDayOfWeek().getValue() % 7);
//        refresh();
//    }
//
//    private String truncate(String text, int maxLen) {
//        if (text.length() <= maxLen) return text;
//        return text.substring(0, maxLen - 2) + "..";
//    }
//
//    public void refresh() {
//        createWeeklyView();
//    }
//
//    // Custom renderer
//    private class WeeklyTableRenderer extends DefaultTableCellRenderer {
//        private LocalDate weekStart;
//
//        public WeeklyTableRenderer(LocalDate weekStart) {
//            this.weekStart = weekStart;
//        }
//
//        @Override
//        public Component getTableCellRendererComponent(JTable table, Object value,
//                                                       boolean isSelected, boolean hasFocus,
//                                                       int row, int column) {
//            Component c = super.getTableCellRendererComponent(table, value, isSelected,
//                    hasFocus, row, column);
//
//            if (column == 0) {
//                // Time column - styled
//                c.setBackground(new Color(240, 240, 240));
//                c.setFont(new Font("Arial", Font.BOLD, 11));
//                setHorizontalAlignment(CENTER);
//                setForeground(new Color(80, 80, 80));
//            } else {
//                // Day columns
//                LocalDate cellDate = weekStart.plusDays(column - 1);
//
//                if (value != null && value.toString().contains("<html>")) {
//                    // Has tasks
//                    c.setBackground(PremiumTheme.STICKY_YELLOW);
//                    setVerticalAlignment(TOP);
//                    setHorizontalAlignment(LEFT);
//                } else {
//                    // Empty
//                    c.setBackground(Color.WHITE);
//                }
//
//                // Highlight today
//                if (cellDate.equals(LocalDate.now())) {
//                    c.setBackground(new Color(255, 255, 200));
//                }
//
//                // Weekend styling
//                DayOfWeek day = cellDate.getDayOfWeek();
//                if (day == DayOfWeek.SUNDAY || day == DayOfWeek.SATURDAY) {
//                    if (value == null || !value.toString().contains("<html>")) {
//                        c.setBackground(new Color(245, 245, 245));
//                    }
//                }
//
//                // Current hour highlight
//                LocalTime now = LocalTime.now();
//                if (row == now.getHour() && cellDate.equals(LocalDate.now())) {
//                    c.setBackground(new Color(255, 240, 200));
//                }
//            }
//
//            return c;
//        }
//    }
//}
package com.edutask.ui.calendar;

import com.edutask.model.*;
import com.edutask.service.TaskService;
import com.edutask.ui.MainFrame;
import com.edutask.ui.themes.PremiumTheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class WeeklyGanttView extends JPanel {
    private final TaskService taskService;
    private final MainFrame mainFrame;
    private LocalDate weekStart;
    private JPanel gridPanel;

    // IMPROVED: Better spacing and sizing
    private static final int HOUR_HEIGHT = 80;
    private static final int DAY_WIDTH = 160;
    private static final int TIME_LABEL_WIDTH = 70;
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    public WeeklyGanttView(TaskService taskService, MainFrame mainFrame) {
        this(taskService, mainFrame, LocalDate.now());
    }

    public WeeklyGanttView(TaskService taskService, MainFrame mainFrame, LocalDate weekStart) {
        this.taskService = taskService;
        this.mainFrame = mainFrame;
        this.weekStart = weekStart.with(java.time.DayOfWeek.MONDAY);

        setLayout(new BorderLayout());
        setBackground(new Color(250, 250, 252));

        buildUI();
    }

    private void buildUI() {
        removeAll();
        add(createHeader(), BorderLayout.NORTH);
        add(createGanttGrid(), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(230, 230, 240));
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 3, 0, PremiumTheme.CORK_DARK),
                new EmptyBorder(12, 15, 12, 15)
        ));

        LocalDate weekEnd = weekStart.plusDays(6);
        String title = String.format("Week of %s - %s",
                weekStart.format(DateTimeFormatter.ofPattern("MMM dd")),
                weekEnd.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        );

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(PremiumTheme.TEXT_PRIMARY);

        JLabel instructionLabel = new JLabel("Click any task to view details");
        instructionLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        instructionLabel.setForeground(PremiumTheme.TEXT_SECONDARY);

        header.add(titleLabel, BorderLayout.WEST);
        header.add(instructionLabel, BorderLayout.EAST);
        return header;
    }

    private JScrollPane createGanttGrid() {
        gridPanel = new JPanel(null);
        gridPanel.setBackground(Color.WHITE);

        int totalWidth = TIME_LABEL_WIDTH + (7 * DAY_WIDTH) + 20;
        int totalHeight = 24 * HOUR_HEIGHT + 50;
        gridPanel.setPreferredSize(new Dimension(totalWidth, totalHeight));

        drawGridBackground();
        drawTimeSlots();
        drawDayColumns();
        plotTasks();

        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.getVerticalScrollBar().setUnitIncrement(HOUR_HEIGHT);
        scroll.getHorizontalScrollBar().setUnitIncrement(DAY_WIDTH);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        // Auto-scroll to 8 AM
        SwingUtilities.invokeLater(() -> {
            scroll.getVerticalScrollBar().setValue(8 * HOUR_HEIGHT);
        });

        return scroll;
    }

    private void drawGridBackground() {
        // Alternating hour backgrounds for readability
        for (int hour = 0; hour < 24; hour++) {
            if (hour % 2 == 0) {
                JPanel bg = new JPanel();
                bg.setBackground(new Color(248, 248, 250));
                bg.setBounds(TIME_LABEL_WIDTH, 50 + hour * HOUR_HEIGHT, 7 * DAY_WIDTH, HOUR_HEIGHT);
                gridPanel.add(bg);
            }
        }
    }

    private void drawTimeSlots() {
        for (int hour = 0; hour < 24; hour++) {
            int y = 50 + hour * HOUR_HEIGHT;

            // Time label (left side)
            String timeText = String.format("%02d:00", hour);
            JLabel timeLabel = new JLabel(timeText, SwingConstants.RIGHT);
            timeLabel.setFont(new Font("Arial", Font.BOLD, 12));
            timeLabel.setForeground(new Color(100, 100, 120));
            timeLabel.setBounds(5, y + 5, TIME_LABEL_WIDTH - 10, 20);
            gridPanel.add(timeLabel);

            // Horizontal grid line
            JPanel line = new JPanel();
            line.setBackground(new Color(200, 200, 210));
            line.setBounds(TIME_LABEL_WIDTH, y, 7 * DAY_WIDTH, 1);
            gridPanel.add(line);
        }
    }

    private void drawDayColumns() {
        for (int day = 0; day < 7; day++) {
            int x = TIME_LABEL_WIDTH + day * DAY_WIDTH;
            LocalDate date = weekStart.plusDays(day);

            // Day header
            JPanel dayHeader = new JPanel(new BorderLayout(5, 0));
            dayHeader.setBackground(new Color(220, 220, 235));
            dayHeader.setBorder(new CompoundBorder(
                    new MatteBorder(0, 1, 2, day == 6 ? 1 : 0, new Color(180, 180, 200)),
                    new EmptyBorder(8, 8, 8, 8)
            ));
            dayHeader.setBounds(x, 0, DAY_WIDTH, 50);

            // Day name
            JLabel dayName = new JLabel(date.format(DateTimeFormatter.ofPattern("EEE")));
            dayName.setFont(new Font("Arial", Font.BOLD, 13));
            dayName.setForeground(PremiumTheme.TEXT_PRIMARY);

            // Date number
            JLabel dayNum = new JLabel(date.format(DateTimeFormatter.ofPattern("dd")));
            dayNum.setFont(new Font("Arial", Font.PLAIN, 12));
            dayNum.setForeground(PremiumTheme.TEXT_SECONDARY);

            // Highlight today
            if (date.equals(LocalDate.now())) {
                dayHeader.setBackground(new Color(180, 200, 255));
                dayName.setForeground(new Color(0, 50, 150));
            }

            dayHeader.add(dayName, BorderLayout.WEST);
            dayHeader.add(dayNum, BorderLayout.EAST);
            gridPanel.add(dayHeader);

            // Vertical separator
            JPanel vLine = new JPanel();
            vLine.setBackground(new Color(200, 200, 210));
            vLine.setBounds(x, 50, 1, 24 * HOUR_HEIGHT);
            gridPanel.add(vLine);
        }
    }

    private void plotTasks() {
        List<Task> tasks = taskService.getAllTasks();

        // FIXED: Group tasks by day and stack them properly
        Map<LocalDate, List<Task>> tasksByDate = new HashMap<>();

        for (Task task : tasks) {
            LocalDate taskDate = task.getDueDate();
            long dayOffset = java.time.temporal.ChronoUnit.DAYS.between(weekStart, taskDate);

            if (dayOffset >= 0 && dayOffset <= 6) {
                tasksByDate.computeIfAbsent(taskDate, k -> new ArrayList<>()).add(task);
            }
        }

        // Plot each day's tasks
        for (Map.Entry<LocalDate, List<Task>> entry : tasksByDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<Task> dayTasks = entry.getValue();

            long dayOffset = java.time.temporal.ChronoUnit.DAYS.between(weekStart, date);
            int dayIndex = (int) dayOffset;

            // FIXED: Stack tasks vertically if multiple at same time
            int taskIndex = 0;
            for (Task task : dayTasks) {
                int x = TIME_LABEL_WIDTH + dayIndex * DAY_WIDTH + 5;

                // Time-based Y position
                int hour = 12; // Default noon
                int minute = 0;
                int y = 50 + (hour * HOUR_HEIGHT) + (minute * HOUR_HEIGHT / 60);

                // FIXED: Stack overlapping tasks
                int stackOffset = taskIndex * 60;

                JPanel taskBlock = createTaskBlock(task);
                taskBlock.setBounds(x, y + stackOffset, DAY_WIDTH - 12, 55);
                gridPanel.add(taskBlock);
                gridPanel.setComponentZOrder(taskBlock, 0); // Bring to front

                taskIndex++;
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel createTaskBlock(Task task) {
        JPanel block = new JPanel();
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setBackground(PremiumTheme.getStickyColorByPriority(task.getPriority()));
        block.setBorder(new CompoundBorder(
                new LineBorder(PremiumTheme.CORK_DARK, 2, true),
                new EmptyBorder(6, 8, 6, 8)
        ));
        block.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Title
        JLabel titleLabel = new JLabel(truncate(task.getTitle(), 20));
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleLabel.setForeground(PremiumTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Subject/Tag
        String subtitle = "";
        if (task instanceof StudyTask st) {
            subtitle = st.getSubject();
        } else if (task instanceof PersonalTask pt) {
            subtitle = pt.getTag();
        }

        if (!subtitle.isEmpty()) {
            JLabel subLabel = new JLabel(truncate(subtitle, 18));
            subLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            subLabel.setForeground(PremiumTheme.TEXT_SECONDARY);
            subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            block.add(subLabel);
        }

        // ✅ FIXED: Use actual task time (with null safety)
        LocalTime taskTime = task.getDueTime();
        if (taskTime == null) taskTime = LocalTime.of(12, 0);

        JLabel timeLabel = new JLabel(taskTime.format(TIME_FMT));
        timeLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        timeLabel.setForeground(new Color(80, 80, 100));
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        block.add(titleLabel);
        block.add(Box.createVerticalStrut(3));
        block.add(timeLabel);

        // Hover effect + click handler (existing code)
        block.addMouseListener(new java.awt.event.MouseAdapter() {
            private Border originalBorder = block.getBorder();

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                block.setBorder(new CompoundBorder(
                        new LineBorder(new Color(255, 150, 0), 3, true),
                        new EmptyBorder(5, 7, 5, 7)
                ));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                block.setBorder(originalBorder);
            }

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (mainFrame != null) {
                    mainFrame.showDayTasks(task.getDueDate());
                    Window window = SwingUtilities.getWindowAncestor(WeeklyGanttView.this);
                    if (window != null) window.dispose();
                }
            }
        });

        return block;
    }


    public void setWeek(LocalDate newStart) {
        this.weekStart = newStart.with(java.time.DayOfWeek.MONDAY);
        buildUI();
    }

    public void setWeek(int year, int month, int weekNumber) {
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        LocalDate weekStart = firstDayOfMonth.plusWeeks(weekNumber - 1);
        setWeek(weekStart);
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public void refresh() {
        buildUI();
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen - 2) + "..";
    }
}
