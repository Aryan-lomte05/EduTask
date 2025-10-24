package com.edutask.ui.calendar;

import com.edutask.service.TaskService;
import com.edutask.ui.MainFrame;
import com.edutask.ui.themes.PremiumTheme;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class CalendarViewPanel extends JPanel {
    private TaskService taskService;
    private MainFrame mainFrame;
    private CardLayout cardLayout;
    private JPanel viewContainer;

    // Different calendar views
    private YearlyCalendarView yearlyView;
    private MonthlyCalendarView monthlyView;
    private WeeklyGanttView weeklyView;

    private JLabel currentViewLabel;
    private JPanel navigationBar;

    public CalendarViewPanel(TaskService taskService, MainFrame mainFrame) {
        this.taskService = taskService;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout());
        setOpaque(false);
        setBackground(PremiumTheme.CORK_BACKGROUND);

        initializeViews();
        createNavigationBar();

        // Start with yearly view
        cardLayout.show(viewContainer, "YEARLY");
        updateLabel("YEARLY", "");
    }

    private void initializeViews() {
        cardLayout = new CardLayout();
        viewContainer = new JPanel(cardLayout);
        viewContainer.setOpaque(false);

        yearlyView = new YearlyCalendarView(taskService, this);
        monthlyView = new MonthlyCalendarView(taskService, this);
        weeklyView = new WeeklyGanttView(taskService, this);

        viewContainer.add(yearlyView, "YEARLY");
        viewContainer.add(monthlyView, "MONTHLY");
        viewContainer.add(weeklyView, "WEEKLY");

        add(viewContainer, BorderLayout.CENTER);
    }

    private void createNavigationBar() {
        navigationBar = new JPanel(new BorderLayout());
        navigationBar.setOpaque(false);
        navigationBar.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        // Left: View buttons
        JPanel viewButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        viewButtons.setOpaque(false);

        JButton yearBtn = createNavButton("[Year]", "YEARLY");
        JButton monthBtn = createNavButton("[Month]", "MONTHLY");
        JButton weekBtn = createNavButton("[Week]", "WEEKLY");
        JButton homeBtn = createHomeButton();

        viewButtons.add(yearBtn);
        viewButtons.add(monthBtn);
        viewButtons.add(weekBtn);
        viewButtons.add(new JSeparator(SwingConstants.VERTICAL));
        viewButtons.add(homeBtn);

        // Center: Current view label with info
        currentViewLabel = new JLabel("Yearly View - 2025", SwingConstants.CENTER);
        currentViewLabel.setFont(new Font("Arial", Font.BOLD, 18));
        currentViewLabel.setForeground(PremiumTheme.TEXT_PRIMARY);

        // Right: Instructions
        JLabel instructionLabel = new JLabel("Click to drill down →", SwingConstants.RIGHT);
        instructionLabel.setFont(PremiumTheme.FONT_SMALL);
        instructionLabel.setForeground(PremiumTheme.TEXT_SECONDARY);

        navigationBar.add(viewButtons, BorderLayout.WEST);
        navigationBar.add(currentViewLabel, BorderLayout.CENTER);
        navigationBar.add(instructionLabel, BorderLayout.EAST);

        add(navigationBar, BorderLayout.NORTH);
    }

    private JButton createNavButton(String text, String view) {
        JButton btn = new JButton(text);
        PremiumTheme.styleButton(btn);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.addActionListener(e -> {
            switchToView(view);
            // Add fade animation
            animateTransition();
        });
        return btn;
    }

    private JButton createHomeButton() {
        JButton homeBtn = new JButton("< Back to Today");
        homeBtn.setFont(new Font("Arial", Font.BOLD, 13));
        homeBtn.setBackground(new Color(100, 180, 255));
        homeBtn.setForeground(Color.WHITE);
        homeBtn.setFocusPainted(false);
        homeBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 150, 230), 2),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        homeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        homeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                homeBtn.setBackground(new Color(70, 150, 230));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                homeBtn.setBackground(new Color(100, 180, 255));
            }
        });

        homeBtn.addActionListener(e -> returnToHome(LocalDate.now()));

        return homeBtn;
    }

    public void switchToView(String viewName) {
        cardLayout.show(viewContainer, viewName);
        updateLabel(viewName, "");
    }

    public void switchToMonth(int year, int month) {
        monthlyView.setYearMonth(year, month);
        cardLayout.show(viewContainer, "MONTHLY");

        String monthName = java.time.Month.of(month).name();
        updateLabel("MONTHLY", monthName + " " + year);
        animateTransition();
    }

    public void switchToWeek(int year, int month, int weekNumber) {
        weeklyView.setWeek(year, month, weekNumber);
        cardLayout.show(viewContainer, "WEEKLY");

        LocalDate weekStart = weeklyView.getWeekStart();
        LocalDate weekEnd = weekStart.plusDays(6);
        String dateRange = String.format("%s - %s",
                weekStart.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd")),
                weekEnd.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")));

        updateLabel("WEEKLY", dateRange);
        animateTransition();
    }

    public void returnToHome(LocalDate date) {
        // Close calendar window
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }

        // Show selected date in main window
        mainFrame.showDayTasks(date);

        // Show confirmation message
        JOptionPane.showMessageDialog(mainFrame,
                "Showing tasks for: " + date.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")),
                "Returned to Home",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateLabel(String viewName, String detail) {
        String label = switch (viewName) {
            case "YEARLY" -> "Yearly Overview - " + LocalDate.now().getYear() +
                    " (Click any month to view details)";
            case "MONTHLY" -> "Monthly View - " + detail + " (Click any day to view tasks)";
            case "WEEKLY" -> "Weekly Schedule - " + detail + " (See all week's tasks)";
            default -> "Calendar View";
        };
        currentViewLabel.setText(label);
    }

    private void animateTransition() {
        // Simple fade animation
        viewContainer.setVisible(false);

        Timer timer = new Timer(50, e -> {
            viewContainer.setVisible(true);
            viewContainer.revalidate();
            viewContainer.repaint();
        });
        timer.setRepeats(false);
        timer.start();
    }

    public void refresh() {
        yearlyView.refresh();
        monthlyView.refresh();
        weeklyView.refresh();
    }
}
